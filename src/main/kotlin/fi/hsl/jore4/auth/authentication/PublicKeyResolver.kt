package fi.hsl.jore4.auth.authentication

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.SigningKeyResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.Key

/**
 * This component resolves the public key that is used to verify
 * the JWT tokens issued by HSLID and provides the resolved key
 * to the used JWT parser.
 */
@Component
open class PublicKeyResolver(private val jwtTokenVerificationKeyService: JwtTokenVerificationKeyService) : SigningKeyResolver {

    override fun resolveSigningKey(header: JwsHeader<out JwsHeader<*>>, claims: Claims): Key {
        return getPublicKey(header)
    }

    override fun resolveSigningKey(header: JwsHeader<out JwsHeader<*>>, plaintext: String): Key {
        return getPublicKey(header)
    }

    private fun getPublicKey(header: JwsHeader<out JwsHeader<*>>): Key {
        val keyId = header[JwsHeader.KEY_ID] as String
        val keyType = OIDCKeyType.findByKeyId(keyId) ?: throw NullPointerException(
                String.format("No key type found with key id: %s", keyId)
        )

        return jwtTokenVerificationKeyService.getVerificationKey(keyType)
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(PublicKeyResolver::class.java)
    }
}