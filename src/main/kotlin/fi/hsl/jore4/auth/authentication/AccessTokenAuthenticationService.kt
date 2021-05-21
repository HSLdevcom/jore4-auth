package fi.hsl.jore4.auth.authentication

import com.nimbusds.oauth2.sdk.AuthorizationGrant
import com.nimbusds.oauth2.sdk.RefreshTokenGrant
import com.nimbusds.oauth2.sdk.TokenRequest
import com.nimbusds.oauth2.sdk.TokenResponse
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.token.AccessToken
import com.nimbusds.oauth2.sdk.token.RefreshToken
import fi.hsl.jore4.auth.authentication.SessionKeys.Companion.ACCESS_TOKEN_KEY
import fi.hsl.jore4.auth.authentication.SessionKeys.Companion.REFRESH_TOKEN_KEY
import fi.hsl.jore4.auth.web.UnauthorizedException
import io.jsonwebtoken.*
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Service
import javax.servlet.http.HttpSession

@Service
open class AccessTokenAuthenticationService(
        private val publicKeyResolver: PublicKeyResolver,
        private val oidcProperties: OIDCProperties,
        private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier
) {
    open fun loginWithAccessToken(accessToken: AccessToken): Authentication {
        try {
            LOGGER.debug("Logging in using access token...")
            val claims = parseAndVerifyAccessToken(accessToken)
            val authentication: Authentication = PreAuthenticatedAuthenticationToken(1, accessToken)

            SecurityContextHolder.getContext().authentication = authentication

            LOGGER.debug("Successfully logged in.")
            return authentication
        } catch (ex: Exception) {
            LOGGER.debug("Login failed.")

            logout()

            throw ex
        }
    }

    private fun logout() {
        LOGGER.debug("Logging out...")

        SecurityContextHolder.getContext().authentication = null

        LOGGER.debug("Logged out.")
    }

    open fun verifyOrRefreshTokens(session: HttpSession): AccessToken? {
        try {
            val accessToken = session.getAttribute(ACCESS_TOKEN_KEY) as AccessToken
            try {
                parseAndVerifyAccessToken(accessToken)
            }
            catch (expiredEx: ExpiredJwtException) {
                val newAccessToken = refreshTokens(session)
                loginWithAccessToken(newAccessToken)
                return newAccessToken
            }
        } catch (ex: java.lang.Exception) {
            logout()
            session.removeAttribute(ACCESS_TOKEN_KEY)
            session.removeAttribute(REFRESH_TOKEN_KEY)
        }
        return null
    }

    private fun parseAndVerifyAccessToken(accessToken: AccessToken): Jws<Claims> {
        try {
            return Jwts.parser()
                .setSigningKeyResolver(publicKeyResolver)
                .requireAudience(oidcProperties.clientId)
                .parseClaimsJws(accessToken.toString())
        } catch (e: UnsupportedJwtException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", e)
            throw BadCredentialsException("Invalid JWT", e)
        } catch (e: MalformedJwtException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", e)
            throw BadCredentialsException("Invalid JWT", e)
        } catch (e: SignatureException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", e)
            throw BadCredentialsException("Invalid JWT", e)
        } catch (e: IllegalArgumentException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", e)
            throw BadCredentialsException("Invalid JWT", e)
        } catch (e: RuntimeException) {
            LOGGER.warn("Exception decoding JWT token.", e)
            throw BadCredentialsException("Unknown JWT exception", e)
        }
    }

    private fun refreshTokens(session: HttpSession): AccessToken {
        val refreshTokenStr = session.getAttribute(REFRESH_TOKEN_KEY)
            ?: throw UnauthorizedException("No refresh token found in session, cannot refresh access token")

        // Construct the grant from the saved refresh token
        val refreshToken = RefreshToken(refreshTokenStr.toString())
        val refreshTokenGrant: AuthorizationGrant = RefreshTokenGrant(refreshToken)

        // The credentials to authenticate the client at the token endpoint
        val clientID = ClientID(oidcProperties.clientId)
        val clientSecret = Secret(oidcProperties.clientSecret)
        val clientAuth: ClientAuthentication = ClientSecretBasic(clientID, clientSecret)

        // Make the token request
        val request = TokenRequest(oidcProviderMetadataSupplier.providerMetadata.tokenEndpointURI, clientAuth, refreshTokenGrant)

        val response = TokenResponse.parse(request.toHTTPRequest().send())

        if (!response.indicatesSuccess()) {
            // We got an error response...
            val errorResponse = response.toErrorResponse()
            throw UnauthorizedException("Access token refresh failed: $errorResponse")
        }

        val successResponse = response.toSuccessResponse()

        // Get the access token, the refresh token may be updated
        val newAccessToken = successResponse.tokens.accessToken
        val newRefreshToken = successResponse.tokens.refreshToken

        session.setAttribute(ACCESS_TOKEN_KEY, newAccessToken)
        session.setAttribute(REFRESH_TOKEN_KEY, newRefreshToken)

        return newAccessToken
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AccessTokenAuthenticationService::class.java)
    }
}