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

        const val AUTHORIZATION_HEADER = "Authorization"
        const val AUTHORIZATION_BEARER_PREFIX = "Bearer "
    }

    /**
     * Proceed with the given interceptor {@param chain} after the given {@param accessToken}
     * has been added as an authorization header.
     */
    private fun proceedWithAccessToken(chain: Interceptor.Chain, accessToken: AccessToken): Response =
        chain.proceed(
            chain.request().newBuilder()
                .addHeader(AUTHORIZATION_HEADER, "$AUTHORIZATION_BEARER_PREFIX$accessToken")
                .build()
        )

    /**
     * Try to refresh the given {@param userTokenSet} and send a new request using the passed
     * {@param sendNewRequest} if refreshing succeeded.
     */
    private fun refreshTokensAndTryAgain(
        userTokenSet: UserTokenSet,
        sendNewRequest: (UserTokenSet) -> Response
    ): Response? {
        LOGGER.debug("Refreshing tokens")

        return try {
            // let's check if our token has expired and refresh if needed.
            val newTokenSet = verificationService.verifyOrRefreshTokens(userTokenSet)
            if (newTokenSet != null) {
                LOGGER.debug("Re-sending request with updated access token")
                // if the token set was updated
                sendNewRequest(newTokenSet)
            } else null  // access token was not updated
        } catch (ex: Exception) {
            null
        }
    }

    /**
     * Proceed with the request in the given {@param chain} using the given {@param userTokenSet}.
     * Use {@param storeUserTokenSet} to store a new token set in case it is updated along the way.
     */
    private fun proceedWithTokenSet(
        chain: Interceptor.Chain, userTokenSet: UserTokenSet,
        storeUserTokenSet: (UserTokenSet) -> Unit
    ): Response {
        LOGGER.debug("Sending request with access token")

        val response = proceedWithAccessToken(chain, userTokenSet.accessToken)

        return if (response.code() == HttpStatus.UNAUTHORIZED.value()) {
            // in case of a 401, let's check if our token has expired and refresh if needed.
            refreshTokensAndTryAgain(
                userTokenSet,
                sendNewRequest = { newTokenSet ->
                    storeUserTokenSet(newTokenSet)
                    response.close()
                    // retry with the new access token
                    proceedWithAccessToken(chain, newTokenSet.accessToken)
                }
            ) ?: response  // return original response if the token set was not refreshed (successfully)
        } else response  // return original response if the reply was NOT 401 unauthorized
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

        return if (userTokenSet == null) {
            LOGGER.debug("Could not resolve user tokens, sending request without authorization header")
            chain.proceed(chain.request())
        } else proceedWithTokenSet(
            chain, userTokenSet,
            storeUserTokenSet = { newTokenSet -> session.setAttribute(SessionKeys.USER_TOKEN_SET_KEY, newTokenSet) }
        )
    }
}
