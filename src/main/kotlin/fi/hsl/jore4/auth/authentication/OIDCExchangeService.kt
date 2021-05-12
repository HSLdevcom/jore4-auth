package fi.hsl.jore4.auth.authentication

import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser
import fi.hsl.jore4.auth.account.AccountService
import fi.hsl.jore4.auth.apipublic.v1.OIDCExchangeApiController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import javax.naming.AuthenticationException
import javax.servlet.http.HttpSession

@Service
open class OIDCExchangeService(
        private val oidcProperties: OIDCProperties,
        private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier,
        private val authenticationService: AccessTokenAuthenticationService,
        private val restTemplate: RestTemplate,
        @Value("\${frontend.url}") private val frontendUrl: String
) {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(OIDCExchangeService::class.java)

        private const val TOKEN_RESPONSE_ACCESS_TOKEN_KEY = "access_token"
        private const val TOKEN_RESPONSE_REFRESH_TOKEN_KEY = "refresh_token"
    }

    open fun exchangeTokens(code: AuthorizationCode, state: State, session: HttpSession, clientRedirectUrl: String?): ResponseEntity<Any> {

        verifyState(state, session)

        // Construct the code grant from the code obtained from the authz endpoint
        // and the original callback URI used at the authz endpoint
        val callback = OIDCExchangeApiController.createCallbackUrl(oidcProperties.clientBaseUrl, clientRedirectUrl)
        val codeGrant: AuthorizationGrant = AuthorizationCodeGrant(code, callback)

        // The credentials to authenticate the client at the token endpoint
        val clientID = ClientID(oidcProperties.clientId)
        val clientSecret = Secret(oidcProperties.clientSecret)
        val clientAuth: ClientAuthentication = ClientSecretBasic(clientID, clientSecret)

        // The token endpoint
        val tokenEndpoint = oidcProviderMetadataSupplier.providerMetadata.tokenEndpointURI

        // Make the token request
        val request = TokenRequest(tokenEndpoint, clientAuth, codeGrant)

        val tokenResponse = OIDCTokenResponseParser.parse(request.toHTTPRequest().send())

        if (!tokenResponse.indicatesSuccess()) {
            // We got an error response...
            val errorResponse: TokenErrorResponse = tokenResponse.toErrorResponse()

            throw AuthenticationException("Could not exchange code for token " + errorResponse.toString())
        }

        val successResponse = tokenResponse.toSuccessResponse() as OIDCTokenResponse

        // Get the ID and access token, the server may also return a refresh token
        val idToken = successResponse.oidcTokens.idToken
        val accessToken = successResponse.oidcTokens.accessToken
        val refreshToken = successResponse.oidcTokens.refreshToken

        authenticationService.loginUsingAccessToken(accessToken)

        session.setAttribute(SessionKeys.ACCESS_TOKEN_KEY, accessToken)
        session.setAttribute(SessionKeys.REFRESH_TOKEN_KEY, refreshToken)

        val redirectUri = URI.create(clientRedirectUrl ?: frontendUrl)

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
