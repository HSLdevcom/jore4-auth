package fi.hsl.jore4.auth.oidc

import com.nimbusds.oauth2.sdk.token.AccessToken
import fi.hsl.jore4.auth.oidc.SessionKeys.Companion.ACCESS_TOKEN_KEY
import okhttp3.*
import okhttp3.Response
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder.getRequestAttributes
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.IOException
import java.lang.Exception

/**
 * Interceptor to add a request header with the user's access token to the requests to be sent.
 */
@Component
class OIDCAuthInterceptor(
    private val verificationService: TokenVerificationService
) : Interceptor {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OIDCAuthInterceptor::class.java)

        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val AUTHORIZATION_BEARER_PREFIX = "Bearer "
    }

    /**
     * Add the user's access token as an authorization header to the request to be sent.
     *
     * In case we get a 401 (unauthorized) response, we check the verify the access token and try to
     * refresh it in case it has expired. After a successful refresh, we re-send the request.
     *
     * If the user does not have a session or if the session does not contain an access token,
     * the request is sent unmodified.
     */
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val session = (getRequestAttributes() as ServletRequestAttributes).request.getSession(false)
        val accessToken = session?.getAttribute(ACCESS_TOKEN_KEY) as AccessToken?

        return if (accessToken != null) {
            val response = proceedWithAccessToken(chain, accessToken)
            return if (response.code() == HttpStatus.UNAUTHORIZED.value()) {
                try {
                    // in case of a 401, let's check if our token has expired and refresh if needed.
                    val updatedAccessToken = verificationService.verifyOrRefreshTokens(session)
                    if (updatedAccessToken != null) {
                        response.close()
                        // retry with the new access token
                        proceedWithAccessToken(chain, updatedAccessToken)
                    } else response
                }
                catch (ex: Exception) {
                    response
                }
            } else response
        } else {
            LOGGER.debug("Could not resolve access token, sending request without authorization header")
            chain.proceed(chain.request())
        }
    }

    /**
     * Proceed with the given interceptor {@param chain} after the given {@param accessToken}
     * has been added as an authorization header.
     */
    private fun proceedWithAccessToken(chain: Interceptor.Chain, accessToken: AccessToken): Response =
            chain.proceed(chain.request().newBuilder()
                    .addHeader(AUTHORIZATION_HEADER, "$AUTHORIZATION_BEARER_PREFIX$accessToken")
                    .build())
}