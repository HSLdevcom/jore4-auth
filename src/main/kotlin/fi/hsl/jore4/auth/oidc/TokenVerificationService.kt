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

@Service
open class TokenVerificationService(
        private val publicKeyResolver: PublicKeyResolver,
        private val oidcProperties: OIDCProperties,
        private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(TokenVerificationService::class.java)
    }

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

    open fun parseAndVerifyAccessToken(accessToken: AccessToken): Jws<Claims> {
        try {
            return Jwts.parser()
                .setSigningKeyResolver(publicKeyResolver)
                .requireAudience(oidcProperties.clientId)
                .parseClaimsJws(accessToken.toString())
        } catch (e: UnsupportedJwtException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", e)
            throw UnauthorizedException("Invalid JWT")
        } catch (e: MalformedJwtException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", e)
            throw UnauthorizedException("Invalid JWT")
        } catch (e: SignatureException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", e)
            throw UnauthorizedException("Invalid JWT")
        } catch (e: IllegalArgumentException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", e)
            throw UnauthorizedException("Invalid JWT")
        } catch (e: RuntimeException) {
            LOGGER.warn("Exception decoding JWT token.", e)
            throw UnauthorizedException("Unknown JWT exception")
        }
    }

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