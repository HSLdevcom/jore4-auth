package fi.hsl.jore4.auth.authentication

import fi.hsl.jore4.auth.authentication.SessionKeys.Companion.ACCESS_TOKEN_KEY
import fi.hsl.jore4.auth.authentication.SessionKeys.Companion.REFRESH_TOKEN_KEY
import fi.hsl.jore4.auth.web.UnauthorizedException
import okhttp3.*
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.context.request.RequestContextHolder.getRequestAttributes
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.IOException

class OIDCInterceptor(private val tokenUrl: String) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val authHeader = getAuthorizationHeader()
        return if (authHeader != null) {
            val response = proceed(chain, authHeader)
            return if (response.code() == HttpStatus.UNAUTHORIZED.value()) renewTokenAndRetry(chain) else response
        } else {
            LOGGER.debug("Could not resolve access token.")
            chain.proceed(chain.request())
        }
    }

    private fun renewTokenAndRetry(chain: Interceptor.Chain): Response {
        val session = (getRequestAttributes() as ServletRequestAttributes).request.getSession(false)
        val refreshToken = session.getAttribute(REFRESH_TOKEN_KEY)
                ?: throw UnauthorizedException("No refresh token found in session, cannot refresh access token")

        val response = OkHttpClient.Builder().build().newCall(Request.Builder()
                .addHeader("Accept", "application/json;charset=UTF-8")
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=refresh_token&refresh_token=$refreshToken"))
                .url(tokenUrl)
                .build())
                .execute()

        LOGGER.debug("Token renewal endpoint responded with code {}", response.code())
        return if (response.isSuccessful) {
            val accessToken = getAccessTokenFromRenewalResponse(response)
            session.setAttribute(ACCESS_TOKEN_KEY, accessToken)

            proceed(chain, "$AUTHORIZATION_BEARER_PREFIX$accessToken")
        } else throw UnauthorizedException("Access token refresh failed with error code ${response.code()}")
    }

    private fun getAccessTokenFromRenewalResponse(response: Response): String {
        val jsonString = response.body()!!.string()
        val jsonObject = JSONObject(jsonString)
        return jsonObject.getString("access_token")
                ?: throw IllegalStateException("Token renewal was successful but response did not contain new access token")
    }

    private fun getAuthorizationHeader(): String? {
        val attributes = getRequestAttributes() as ServletRequestAttributes? ?: return null
        val session = attributes.request.getSession(false)
        val accessToken = session?.getAttribute(ACCESS_TOKEN_KEY)
        return if (accessToken != null) {
            LOGGER.debug("Found access token from session: {}", accessToken)
            "$AUTHORIZATION_BEARER_PREFIX$accessToken"
        } else throw UnauthorizedException("No access token found")
    }

    private fun proceed(chain: Interceptor.Chain, authHeader: String): Response =
            chain.proceed(chain.request().newBuilder()
                    .addHeader(AUTHORIZATION_HEADER, authHeader)
                    .build())

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OIDCInterceptor::class.java)

        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val AUTHORIZATION_BEARER_PREFIX = "Bearer "
    }
}