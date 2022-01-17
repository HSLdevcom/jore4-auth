package fi.hsl.jore4.auth.userInfo

import com.fasterxml.jackson.databind.ObjectMapper
import fi.hsl.jore4.auth.apipublic.v1.model.UserInfoApiDTO
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
    oidcAuthInterceptor: OIDCAuthInterceptor
) {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(UserInfoService::class.java)

        private const val ID_CLAIM = "sub"
        private const val FULL_NAME_CLAIM = "name"
        private const val GIVEN_NAME_CLAIM = "given_name"
        private const val FAMILY_NAME_CLAIM = "family_name"
        private const val EXT_PERMISSIONS_CLAIM = "https://oneportal.trivore.com/claims/active_external_permissions"
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(oidcAuthInterceptor)  // use interceptor to add authorization information to request
        .build()

    /**
     * Retrieve the currently logged in user's user info.
     *
     * Send a OIDC user-info request to the OIDC provider and forwards the response to our caller.
     */
    open fun getUserInfo(): UserInfoApiDTO {
        LOGGER.debug("Fetching user info...")

        val response = httpClient
            .newCall(Request.Builder()
                .addHeader("Accept", "application/json;charset=UTF-8")
                .get()
                .url(oidcProviderMetadataSupplier.providerMetadata.userInfoEndpointURI.toURL())
                .build())
            .execute()

        // If the response was not successful, we'll assume the user was not authorized to make the call. Reasons
        // for the user not being authorized may be
        // - the user never logged in, i.e. is not authenticated and does not have a token set in her session
        // - the token set is invalid or expired and could not be refreshed
        //
        // We treat these cases the same way, so we don't leak any information in case of a more detailed response.
        //
        // Note that this condition usually does not depict an error.
        if (!response.isSuccessful) {
            response.close()
            throw UnauthorizedException("Could not retrieve user info")
        }

        val userInfo: SafeUserInfoApiDTO  // use SafeUserInfoApiDTO to ensure toString() won't leak sensitive data

        try {
            val result: Map<String, Any> =
                ObjectMapper().readValue(response.body()!!.string(), HashMap<String, Any>().javaClass)

            userInfo = SafeUserInfoApiDTO().apply {
                id = result[ID_CLAIM].toString()
                fullName = result[FULL_NAME_CLAIM]?.toString()
                givenName = result[GIVEN_NAME_CLAIM]?.toString()
                familyName = result[FAMILY_NAME_CLAIM]?.toString()

                // we can suppress the unchecked cast warning, since we're only interested in whether an exception
                // is thrown in case of a cast failure (which will result in us throwing an UnauthorizedException)
                @Suppress("UNCHECKED_CAST")
                permissions = (result[EXT_PERMISSIONS_CLAIM] as Map<String, ArrayList<Map<String, String>>>)["active"]!!
                    .map { extPermission -> checkNotNull(extPermission["permissionExternalId"]) }
                    .distinct()
            }
        }
        catch (ex: Exception) {
            response.close()
            // if any part of parsing or interpreting the user info claims goes wrong, respond with 401 Unauthorized
            throw UnauthorizedException("Could not parse user info")
        }

        LOGGER.debug("Fetched user info {}", userInfo)
        return userInfo
    }
}
