package fi.hsl.jore4.auth.oidc

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyType
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.SigningKeyResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.Key
import java.security.KeyException

/**
 * Resolves the public key that is used to verify the JWT tokens issued by the OIDC provider and provides
 * the key to the used JWT parser.
 */
@Component
open class PublicKeyResolver(
    private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier
) : SigningKeyResolver {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(PublicKeyResolver::class.java)
    }

    @Volatile
    private var jwkSet = JWKSet()

    override fun resolveSigningKey(header: JwsHeader, claims: Claims): Key {
        return getPublicKey(header)
    }

    override fun resolveSigningKey(header: JwsHeader, p1: ByteArray?): Key {
        return getPublicKey(header)
    }

    fun resolveSigningKey(header: JwsHeader, plaintext: String): Key {
        return getPublicKey(header)
    }

    /**
     * Resolve the public key that is referenced in the given JWS {@header}.
     *
     * If the key cannot be found, refresh the key set from the OIDC provider and retries the key lookup.
     *
     * Note that only RSA keys are currently supported.
     */
    private fun getPublicKey(header: JwsHeader): Key {
        val keyId = header[JwsHeader.KEY_ID] as String? ?: throw KeyException("Could not find key id")

        var key = jwkSet.getKeyByKeyId(keyId)

        if (key == null) {
            synchronized(this) {
                // Try once more to get the key inside the synchronized block. Possibly the key set has already
                // been refreshed by the time the lock is opened for us.
                key = jwkSet.getKeyByKeyId(keyId)

                if (key == null) {
                    LOGGER.info("Refreshing public key set...")

                    jwkSet = JWKSet.load(oidcProviderMetadataSupplier.providerMetadata.jwkSetURI.toURL())

                    LOGGER.info("Public key set refreshed, ${jwkSet.keys.size} keys available")

                    key = jwkSet.getKeyByKeyId(keyId)
                }
            }
        }

        // if we didn't find a key
        if (key == null) throw KeyException("Could not find key for id $keyId")

        // only RSA supported for now
        if (!key.keyType.equals(KeyType.RSA)) {
            throw KeyException("Keys of type ${key.keyType} not supported")
        }

        return key.toRSAKey().toRSAPublicKey()
    }
}
