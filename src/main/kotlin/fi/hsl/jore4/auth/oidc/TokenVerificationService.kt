package fi.hsl.jore4.auth.oidc

import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.token.AccessToken
import fi.hsl.jore4.auth.web.UnauthorizedException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Parses and verifies the user's tokens.
 */
@Service
open class TokenVerificationService(
    publicKeyLocator: PublicKeyLocator,
    private val oidcProperties: OIDCProperties,
    private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(TokenVerificationService::class.java)
    }

    private val jwtsParser =
        Jwts
            .parser()
            .keyLocator(publicKeyLocator)
            .requireIssuer(oidcProperties.providerBaseUrl)
            .requireAudience(oidcProperties.clientId)

    /**
     * Verify the access token found in the given {@param userTokenSet}.
     *
     * In case it has expired, refresh the token set.
     *
     * @return The new token set if the token set was refreshed, null otherwise.
     */
    open fun verifyOrRefreshTokens(userTokenSet: UserTokenSet): UserTokenSet? =
        try {
            parseAndVerifyAccessToken(userTokenSet.accessToken)
            null
        } catch (expiredEx: ExpiredJwtException) {
            val newTokenSet =
                userTokenSet.refresh(
                    oidcProviderMetadataSupplier.providerMetadata.tokenEndpointURI,
                    ClientID(oidcProperties.clientId),
                    Secret(oidcProperties.clientSecret)
                )
            // retry to verify the new access token if not using Entra, as it uses an unverifiable internal token
            // See https://learn.microsoft.com/en-us/entra/identity-platform/access-tokens#validate-tokens
            if (!oidcProperties.providerBaseUrl.startsWith("https://login.microsoftonline.com/")) {
                parseAndVerifyAccessToken(newTokenSet.accessToken)
            }
            newTokenSet
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
            return jwtsParser.build().parseSignedClaims(accessToken.toString())
        } catch (ex: UnsupportedJwtException) {
            LOGGER.warn("Authorization attempt with unsupported JWT token.", ex)
            throw UnauthorizedException("Authorization attempt with unsupported JWT token")
        } catch (ex: MalformedJwtException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", ex)
            throw UnauthorizedException("Authorization attempt with malformed JWT token")
        } catch (ex: SignatureException) {
            LOGGER.warn("Authorization attempt with JWT token with wrong signature.", ex)
            throw UnauthorizedException("Authorization attempt with JWT token with wrong signature")
        } catch (ex: ExpiredJwtException) {
            LOGGER.warn("Authorization attempt with expired JWT token.", ex)
            // re-throw on purpose to allow caller to react on it
            throw ex
        } catch (ex: IllegalArgumentException) {
            LOGGER.warn("Authorization attempt with JWT token as an illegal argument.", ex)
            throw UnauthorizedException("Authorization attempt with JWT token as an illegal argument")
        } catch (ex: Exception) {
            LOGGER.warn("Exception decoding JWT token.", ex)
            throw UnauthorizedException("Unknown JWT exception")
        }
    }
}
