package fi.hsl.jore4.auth.oidc

import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.net.URI
import javax.naming.AuthenticationException
import javax.servlet.http.HttpSession

/**
 * Provides the functionality for exchanging an OIDC authorization code for tokens.
 */
@Service
open class OIDCTokenExchangeService(
    private val oidcProperties: OIDCProperties,
    private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier,
    private val verificationService: TokenVerificationService,
    @Value("\${loginpage.url}") private val loginPageUrl: String
) {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(OIDCTokenExchangeService::class.java)
    }

    /**
     * Exchange the given authorization {@param code} for an access and refresh token.
     *
     * Before the code exchange takes place, the given {@param state} is verified using the session
     * data.
     *
     * After an successful exchange, the access is verified and both the access and refresh tokens
     * are stored in the user's session. The function then returns a redirect response entity to the
     * {@param clientRedirectUrl} or the configured login page URL, if no redirect URl is passed.
     */
    open fun exchangeTokens(code: AuthorizationCode, state: State, session: HttpSession,
                            callbackUri: URI, clientRedirectUrl: String?
    ): ResponseEntity<Any> {

        // verify that we're being called as a response to our request to authenticate
        verifyState(state, session)

        // create the token request based on the auth code
        val request = TokenRequest(
            oidcProviderMetadataSupplier.providerMetadata.tokenEndpointURI,
            ClientSecretBasic(ClientID(oidcProperties.clientId), Secret(oidcProperties.clientSecret)),
            AuthorizationCodeGrant(code, callbackUri)
        )
        val response = OIDCTokenResponseParser.parse(request.toHTTPRequest().send())

        if (!response.indicatesSuccess()) {
            throw AuthenticationException("Could not exchange code for token")
        }

        val successResponse = response.toSuccessResponse() as OIDCTokenResponse

        // get the access token and refresh token
        val accessToken = successResponse.oidcTokens.accessToken
        val refreshToken = successResponse.oidcTokens.refreshToken

        // verify token authenticity and validity
        verificationService.parseAndVerifyAccessToken(accessToken)

        session.setAttribute(SessionKeys.ACCESS_TOKEN_KEY, accessToken)
        session.setAttribute(SessionKeys.REFRESH_TOKEN_KEY, refreshToken)

        // use the login page URL if no explicit redirect URL was passed
        val redirectUri = URI.create(clientRedirectUrl ?: loginPageUrl)

        val headers = HttpHeaders().apply {
            location = redirectUri
        }

        LOGGER.info("Redirecting back to {}", redirectUri)

        return ResponseEntity(headers, HttpStatus.FOUND)
    }

    /**
     * Verify the given {@param state} by comparing it to the state saved in the {@param session}.
     */
    private fun verifyState(state: State, session: HttpSession) {
        if (state != session.getAttribute(SessionKeys.OIDC_STATE_KEY)) {
            throw AuthenticationException("Invalid OIDC state, did you change the browser while logging in?")
        }

        session.removeAttribute(SessionKeys.OIDC_STATE_KEY)
    }
}
