package fi.hsl.jore4.auth.account

import fi.hsl.jore4.auth.apipublic.v1.model.AccountApiDTO
import fi.hsl.jore4.auth.authentication.OIDCAuthInterceptor
import fi.hsl.jore4.auth.authentication.OIDCProviderMetadataSupplier
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
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

    open fun getActiveUserAccount(): AccountApiDTO {

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

        val responseString = response.body()!!.string()
        val jsonObject = JSONObject(responseString)

        val account = AccountApiDTO()
        account.firstName = jsonObject.getString("given_name")
        account.lastName = jsonObject.getString("family_name")
        return account
    }
}
