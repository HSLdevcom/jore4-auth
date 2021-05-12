package fi.hsl.jore4.auth.account

import fi.hsl.jore4.auth.apipublic.v1.model.AccountApiDTO
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate

@Service
open class AccountService {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(AccountService::class.java)
    }

    open fun getActiveUserAccount(accessToken: String): AccountApiDTO {

        val response = OkHttpClient.Builder().build().newCall(
            Request.Builder()
            .addHeader("Accept", "application/json;charset=UTF-8")
                .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .url("https://hslid-dev.t5.fi/openid/userinfo")
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
