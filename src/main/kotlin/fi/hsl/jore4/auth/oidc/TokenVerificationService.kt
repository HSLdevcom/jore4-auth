package fi.hsl.jore4.auth.oidc

import com.nimbusds.oauth2.sdk.RefreshTokenGrant
import com.nimbusds.oauth2.sdk.TokenRequest
import com.nimbusds.oauth2.sdk.TokenResponse
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.token.AccessToken
import com.nimbusds.oauth2.sdk.token.RefreshToken
import fi.hsl.jore4.auth.oidc.SessionKeys.Companion.ACCESS_TOKEN_KEY
import fi.hsl.jore4.auth.oidc.SessionKeys.Companion.REFRESH_TOKEN_KEY
import fi.hsl.jore4.auth.web.UnauthorizedException
import io.jsonwebtoken.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.naming.AuthenticationException
import javax.servlet.http.HttpSession

/**
 * Parses, verifies, and refreshes access tokens.
 */
@Service
open class TokenVerificationService(
        private val publicKeyResolver: PublicKeyResolver,
        private val oidcProperties: OIDCProperties,
        private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(TokenVerificationService::class.java)
    }

    /**
     * Verify the access token found in the given {@param session}.
     *
     * In case it has expired, refresh the access (and potentially refresh) token, store them in the {@param session}
     * and return the updated access token.
     */
    open fun verifyOrRefreshTokens(session: HttpSession): AccessToken? {
        try {
            val accessToken = (session.getAttribute(ACCESS_TOKEN_KEY)
                ?: throw UnauthorizedException("No access token found in session"))
                    as AccessToken
            try {
                parseAndVerifyAccessToken(accessToken)
            } catch (expiredEx: ExpiredJwtException) {
                val newAccessToken = refreshTokens(session)
                parseAndVerifyAccessToken(newAccessToken)
                return newAccessToken
            }
        } catch (ex: Exception) {
            session.removeAttribute(ACCESS_TOKEN_KEY)
            session.removeAttribute(REFRESH_TOKEN_KEY)
        }
        return null
    }

    /**
     * Verify the {@param accessToken} and parses its claims.
     *
     * The issuer audience is also verified (must match our OIDC client ID).
     *
     * An {@exception UnauthorizedException} is thrown in case of an invalid token, _except_ if the token
     * has expired. In this case, the caught {@exception ExpiredJwtException} is re-thrown.
     */
    open fun parseAndVerifyAccessToken(accessToken: AccessToken): Jws<Claims> {
        try {
            return Jwts.parser()
                .setSigningKeyResolver(publicKeyResolver)
                .requireAudience(oidcProperties.clientId)
                .parseClaimsJws(accessToken.toString())
        } catch (ex: UnsupportedJwtException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", ex)
            throw UnauthorizedException("Invalid JWT")
        } catch (ex: MalformedJwtException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", ex)
            throw UnauthorizedException("Invalid JWT")
        } catch (ex: SignatureException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", ex)
            throw UnauthorizedException("Invalid JWT")
        } catch (ex: ExpiredJwtException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", ex)
            // re-throw on purpose to allow caller to react on it
            throw ex
        } catch (ex: IllegalArgumentException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", ex)
            throw UnauthorizedException("Invalid JWT")
        } catch (ex: RuntimeException) {
            LOGGER.warn("Exception decoding JWT token.", ex)
            throw UnauthorizedException("Unknown JWT exception")
        }
    }

    /**
     * Refresh the access and refresh tokens found in the {@param session}.
     *
     * The updated tokens are again stored in the {@param session} and the new access token is returned.
     */
    private fun refreshTokens(session: HttpSession): AccessToken {
        val refreshToken = (session.getAttribute(REFRESH_TOKEN_KEY)
            ?: throw UnauthorizedException("No refresh token found in session, cannot refresh access token"))
                as RefreshToken

        // create the token request based on the refresh token
        val request = TokenRequest(
            oidcProviderMetadataSupplier.providerMetadata.tokenEndpointURI,
            ClientSecretBasic(ClientID(oidcProperties.clientId), Secret(oidcProperties.clientSecret)),
            RefreshTokenGrant(refreshToken)
        )

        val response = TokenResponse.parse(request.toHTTPRequest().send())

        if (!response.indicatesSuccess()) {
            throw AuthenticationException("Token refresh failed")
        }

        val successResponse = response.toSuccessResponse()

        val newAccessToken = successResponse.tokens.accessToken
        val newRefreshToken = successResponse.tokens.refreshToken

        // store the new access token (and the potentially new refresh token)
        session.setAttribute(ACCESS_TOKEN_KEY, newAccessToken)
        session.setAttribute(REFRESH_TOKEN_KEY, newRefreshToken)

        return newAccessToken
    }
}