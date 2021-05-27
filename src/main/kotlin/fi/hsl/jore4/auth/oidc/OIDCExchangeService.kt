package fi.hsl.jore4.auth.oidc

import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser
import fi.hsl.jore4.auth.apipublic.v1.OIDCExchangeApiController
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

@Service
open class OIDCExchangeService(
    private val oidcProperties: OIDCProperties,
    private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier,
    private val verificationService: TokenVerificationService,
    @Value("\${loginpage.url}") private val loginPageUrl: String
) {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(OIDCExchangeService::class.java)
    }

    open fun exchangeTokens(code: AuthorizationCode, state: State, session: HttpSession, clientRedirectUrl: String?): ResponseEntity<Any> {

        // verify that we're being called as a response to our request to authenticate
        verifyState(state, session)

        // create the exchange endpoint callback URI, to which the user will be redirected after authentication
        val callback = OIDCExchangeApiController.createCallbackUri(oidcProperties.clientBaseUrl, clientRedirectUrl)

        // create the token request based on the auth code
        val request = TokenRequest(
            oidcProviderMetadataSupplier.providerMetadata.tokenEndpointURI,
            ClientSecretBasic(ClientID(oidcProperties.clientId), Secret(oidcProperties.clientSecret)),
            AuthorizationCodeGrant(code, callback)
        )
        val response = OIDCTokenResponseParser.parse(request.toHTTPRequest().send())

        if (!response.indicatesSuccess()) {
            throw AuthenticationException("Could not exchange code for token");
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

    private fun verifyState(state: State, session: HttpSession) {
        if (state != session.getAttribute(SessionKeys.OIDC_STATE_KEY)) {
            throw AuthenticationException("Invalid OIDC state, did you change the browser while logging in?")
        }
    }
}
