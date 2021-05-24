package fi.hsl.jore4.auth.account

import com.fasterxml.jackson.databind.ObjectMapper
import fi.hsl.jore4.auth.authentication.OIDCAuthInterceptor
import fi.hsl.jore4.auth.authentication.OIDCProviderMetadataSupplier
import fi.hsl.jore4.auth.web.UnauthorizedException
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
open class AccountService(
    private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier,
    private val oidcAuthInterceptor: OIDCAuthInterceptor
) {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(AccountService::class.java)
    }

    open fun getAccount(): Map<String, Any> {

        val response = OkHttpClient.Builder()
            .addInterceptor(oidcAuthInterceptor)
            .build()
            .newCall(
                Request.Builder()
                .addHeader("Accept", "application/json;charset=UTF-8")
                .get()
                .url(oidcProviderMetadataSupplier.providerMetadata.userInfoEndpointURI.toURL())
                .build())
                .execute()

        if (!response.isSuccessful) {
            throw UnauthorizedException("Could not retrieve account data")
        }

        val result: Map<String, Any> = ObjectMapper().readValue(response.body()!!.string(), HashMap<String, Any>().javaClass)

        return result
    }
}
