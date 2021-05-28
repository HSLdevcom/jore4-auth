package fi.hsl.jore4.auth.userInfo

import com.fasterxml.jackson.databind.ObjectMapper
import fi.hsl.jore4.auth.oidc.OIDCAuthInterceptor
import fi.hsl.jore4.auth.oidc.OIDCProviderMetadataSupplier
import fi.hsl.jore4.auth.web.UnauthorizedException
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Provides access to user info data.
 */
@Service
open class UserInfoService(
    private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier,
    private val oidcAuthInterceptor: OIDCAuthInterceptor
) {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(UserInfoService::class.java)
    }

    /**
     * Retrieve the currently logged in user's user info.
     *
     * Send a OIDC user-info request to the OIDC auth server and forwards the response to our caller.
     */
    open fun getUserInfo(): Map<String, Any> {
        LOGGER.info("Fetching user info...")

        val response = OkHttpClient.Builder()
            .addInterceptor(oidcAuthInterceptor)  // use interceptor to add authorization information to request
            .build()
            .newCall(Request.Builder()
                .addHeader("Accept", "application/json;charset=UTF-8")
                .get()
                .url(oidcProviderMetadataSupplier.providerMetadata.userInfoEndpointURI.toURL())
                .build())
            .execute()

        // If the response was not successful, we'll assume the user was not authorized to make the call. This
        // way we don't leak any information in case of a more detailed response.
        if (!response.isSuccessful) {
            throw UnauthorizedException("Could not retrieve user info")
        }

        val result: Map<String, Any> =
            ObjectMapper().readValue(response.body()!!.string(), HashMap<String, Any>().javaClass)

        LOGGER.info("Fetched user info.")

        return result
    }
}
