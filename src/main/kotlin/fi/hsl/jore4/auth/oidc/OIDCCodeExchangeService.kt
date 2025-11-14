package fi.hsl.jore4.auth.oidc

import com.nimbusds.oauth2.sdk.AuthorizationCode
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant
import com.nimbusds.oauth2.sdk.Scope
import com.nimbusds.oauth2.sdk.TokenRequest
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser
import jakarta.servlet.http.HttpSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import javax.naming.AuthenticationException

/**
 * Provides the functionality for exchanging an OIDC authorization code for tokens.
 */
@Service
open class OIDCCodeExchangeService(
    private val oidcProperties: OIDCProperties,
    private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier,
    private val verificationService: TokenVerificationService,
    @Value("\${loginpage.url}") private val loginPageUrl: String
) {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(OIDCCodeExchangeService::class.java)
    }

    /**
     * Exchange the given authorization {@param code} for an access and refresh token.
     *
     * Before the code exchange takes place, the given {@param state} is verified using the session
     * data.
     *
     * After a successful exchange, the access is verified and both the access and refresh tokens
     * are stored in the user's session. The function then returns a redirect response entity to the
     * configured login page URL, if no redirect URl is passed.
     */
    open fun exchangeCodeAndRedirect(
        code: AuthorizationCode,
        state: State,
        session: HttpSession,
        callbackUri: URI
    ): URI {
        LOGGER.debug("Exchanging code for tokens...")

        // verify that we're being called as a response to our request to authenticate
        if (state != session.getAttribute(SessionKeys.OIDC_STATE_KEY)) {
            throw AuthenticationException("Invalid OIDC state, did you change the browser while logging in?")
        }

        session.removeAttribute(SessionKeys.OIDC_STATE_KEY)

        // create the token request based on the auth code
        val request =
            TokenRequest(
                oidcProviderMetadataSupplier.providerMetadata.tokenEndpointURI,
                ClientSecretBasic(ClientID(oidcProperties.clientId), Secret(oidcProperties.clientSecret)),
                AuthorizationCodeGrant(code, callbackUri),
                Scope("openid")
            )
        val response = OIDCTokenResponseParser.parse(request.toHTTPRequest().send())

        if (!response.indicatesSuccess()) {
            throw AuthenticationException("Could not exchange code for token")
        }

        LOGGER.debug("Received user tokens.")

        val successResponse = response.toSuccessResponse() as OIDCTokenResponse

        // get the access token and refresh token
        val accessToken = successResponse.oidcTokens.accessToken
        val refreshToken = successResponse.oidcTokens.refreshToken

        // verify token authenticity and validity if not using Entra, as it uses an unverifiable internal token
        // See https://learn.microsoft.com/en-us/entra/identity-platform/access-tokens#validate-tokens
        if (!oidcProperties.providerBaseUrl.startsWith("https://login.microsoftonline.com/")) {
            verificationService.parseAndVerifyAccessToken(accessToken)
        }

        session.setAttribute(SessionKeys.USER_TOKEN_SET_KEY, UserTokenSet(accessToken, refreshToken))

        // redirect the user to the login page URL
        val redirectUri = URI.create(loginPageUrl)

        LOGGER.debug("Created redirect URI: {}", redirectUri)

        return redirectUri
    }
}
