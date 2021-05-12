package fi.hsl.jore4.auth.authentication

import io.jsonwebtoken.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Component

@Component
open class JwtTokenParser(
        private val publicKeyResolver: PublicKeyResolver,
        private val verificationKeyService: JwtTokenVerificationKeyService
) {

    open fun parseAndVerify(jwtToken: String): Jws<Claims> {
        return parseAndVerify(jwtToken, "aaa")
    }

    open fun parseAndVerify(jwtToken: String, requiredAudience: String): Jws<Claims> {
        return try {
            parseClaims(jwtToken, requiredAudience)

        } catch (e: UnsupportedJwtException) {
            LOGGER.warn("First authorization attempt with malformed JWT token. Refreshing verification key and trying again", e)
            refreshVerificationKeysAndParseClaims(jwtToken, requiredAudience)

        } catch (e: MalformedJwtException) {
            LOGGER.warn("First authorization attempt with malformed JWT token. Refreshing verification key and trying again", e)
            refreshVerificationKeysAndParseClaims(jwtToken, requiredAudience)
        } catch (e: SignatureException) {
            LOGGER.warn("First authorization attempt with malformed JWT token. Refreshing verification key and trying again", e)
            refreshVerificationKeysAndParseClaims(jwtToken, requiredAudience)
        }
    }

    private fun parseClaims(jwtToken: String,
                            requiredAudience: String): Jws<Claims> {
        try {
            return Jwts.parser()
                    .setSigningKeyResolver(publicKeyResolver)
                    .requireAudience(requiredAudience)
                    .parseClaimsJws(jwtToken)
        } catch (e: ExpiredJwtException) {
            LOGGER.info("Authorization attempt with an expired JWT token.")
            throw BadCredentialsException("Expired JWT", e)

        } catch (e: IllegalArgumentException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", e)
            throw BadCredentialsException("Invalid JWT", e)
        } catch (e: RuntimeException) {
            LOGGER.warn("Exception decoding JWT token.", e)
            throw BadCredentialsException("Unknown JWT exception", e)
        }
    }

    private fun refreshVerificationKeysAndParseClaims(jwtToken: String,
                                                      requiredAudience: String): Jws<Claims> {
        try {
            verificationKeyService.refreshVerificationKey(OIDCKeyType.INTEGRATION)
            verificationKeyService.refreshVerificationKey(OIDCKeyType.USER_ACCESS_TOKEN)
            return parseClaims(jwtToken, requiredAudience)
        } catch (e: UnsupportedJwtException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", e)
            throw BadCredentialsException("Invalid JWT", e)

        } catch (e: MalformedJwtException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", e)
            throw BadCredentialsException("Invalid JWT", e)
        } catch (e: SignatureException) {
            LOGGER.warn("Authorization attempt with malformed JWT token.", e)
            throw BadCredentialsException("Invalid JWT", e)
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(JwtTokenParser::class.java)
    }
}