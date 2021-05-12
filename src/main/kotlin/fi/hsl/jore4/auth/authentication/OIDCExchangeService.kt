package fi.hsl.jore4.auth.authentication

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        private val authenticationService: AccessTokenAuthenticationService,
        private val restTemplate: RestTemplate
) {

    open fun exchangeTokens(code: String, state: String, session: HttpSession, clientRedirectUrl: String?): ResponseEntity<Any> {

        val exchangeUri = UriComponentsBuilder.fromUriString(oidcProperties.tokenUri)
                .queryParam("grant_type", "authorization_code")
                .queryParam("code", code)
                .queryParam("redirect_uri", OIDCUtil.buildRedirectUri(oidcProperties.redirectUri, clientRedirectUrl))
                .queryParam("client_id", oidcProperties.clientId)
                .queryParam("client_secret", oidcProperties.clientSecret)
                .build()
                .encode()
                .toUri()

        verifyState(state, session)

        val typeRef = object : ParameterizedTypeReference<Map<String, String>>() {}
        val response = this.restTemplate.exchange(exchangeUri, HttpMethod.POST, null, typeRef)

        val responseBody = response.body ?: throw AuthenticationException("Token response body is missing")
        val accessToken = responseBody[TOKEN_RESPONSE_ACCESS_TOKEN_KEY]?.trim()
                ?: throw AuthenticationException("Access token is missing")
        val refreshToken = responseBody[TOKEN_RESPONSE_REFRESH_TOKEN_KEY]?.trim()
                ?: throw AuthenticationException("Refresh token is missing")

        authenticationService.loginUsingAccessToken(accessToken)

        session.setAttribute(SessionKeys.ACCESS_TOKEN_KEY, accessToken)
        session.setAttribute(SessionKeys.REFRESH_TOKEN_KEY, refreshToken)

        LOG.info("Redirecting back to profile page {}", oidcProperties.profilePage)

        val headers = HttpHeaders().apply {
            location = URI.create(clientRedirectUrl ?: oidcProperties.profilePage)
        }

        return ResponseEntity(headers, HttpStatus.FOUND)
    }

    private fun verifyState(state: String, session: HttpSession) {

        if (state != session.getAttribute(SessionKeys.OIDC_STATE_KEY)) {
            throw AuthenticationException("Invalid OIDC state, did you change the browser while logging in?")
        }
    }

    companion object {
        private const val TOKEN_RESPONSE_ACCESS_TOKEN_KEY = "access_token"
        private const val TOKEN_RESPONSE_REFRESH_TOKEN_KEY = "refresh_token"

        val LOG: Logger = LoggerFactory.getLogger(OIDCExchangeService::class.java)
    }
}
