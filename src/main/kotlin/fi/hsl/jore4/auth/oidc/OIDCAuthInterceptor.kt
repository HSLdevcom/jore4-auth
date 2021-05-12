package fi.hsl.jore4.auth.oidc

import com.nimbusds.oauth2.sdk.token.AccessToken
import okhttp3.Interceptor
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
        val userTokenSet = session?.getAttribute(SessionKeys.USER_TOKEN_SET_KEY) as UserTokenSet?

        if (userTokenSet == null) {
            LOGGER.debug("Could not resolve user tokens, sending request without authorization header")
            return chain.proceed(chain.request())
        }

        val response = proceedWithAccessToken(chain, userTokenSet.accessToken)
        return if (response.code() == HttpStatus.UNAUTHORIZED.value()) {
            try {
                // in case of a 401, let's check if our token has expired and refresh if needed.
                val newTokenSet = verificationService.verifyOrRefreshTokens(userTokenSet)
                if (newTokenSet != null) {
                    session.setAttribute(SessionKeys.USER_TOKEN_SET_KEY, newTokenSet)
                    response.close()
                    // retry with the new access token
                    proceedWithAccessToken(chain, newTokenSet.accessToken)
                } else response  // return original response if access token was not updated
            }
            catch (ex: Exception) {
                response  // return original response if an exception occurred
            }
        } else response  // return original response if the reply was NOT 401 unauthorized
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
