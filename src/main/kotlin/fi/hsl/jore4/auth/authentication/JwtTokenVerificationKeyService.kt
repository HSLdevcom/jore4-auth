package fi.hsl.jore4.auth.authentication

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

/**
 * This service retrieves the public key that is used to
 * verify the JWT authentication tokens issued by HSLID.
 */
@Service
open class JwtTokenVerificationKeyService(
        private val restTemplate: RestTemplate,
        private val properties: OIDCProperties
) {

    private val jwtTokenVerificationKeys: MutableMap<OIDCKeyType, PublicKey> = mutableMapOf()

    /**
     * Returns the "cached" public key that is used to verify the JWT Authentication tokens
     * issued by HSLID.
     */
    open fun getVerificationKey(keyType: OIDCKeyType): PublicKey {
        LOGGER.debug("Returning the cached access token verification public key.")

        var jwtTokenVerificationKey = jwtTokenVerificationKeys[keyType]
        if (jwtTokenVerificationKey == null) {
            jwtTokenVerificationKey = refreshVerificationKey(keyType)
        }

        return jwtTokenVerificationKey
    }

    /**
     * Refreshes the public key that is used to verify the JWT authentication tokens
     * issued by HSLID.
     * @return The returned public key.
     */
    open fun refreshVerificationKey(keyType: OIDCKeyType): PublicKey {
        LOGGER.debug("Refreshing the access token verification public key")

        val keyQueryUrl = getKeyQueryUrl(keyType)
        val keyString = restTemplate.exchange(
                keyQueryUrl,
                HttpMethod.GET, null,
                String::class.java
        ).body

        val decoder = Base64.getDecoder()
        val spec = X509EncodedKeySpec(decoder.decode(keyString))
        val keyFactory = KeyFactory.getInstance("RSA")
        val jwtTokenVerificationKey = keyFactory.generatePublic(spec)

        jwtTokenVerificationKeys[keyType] = jwtTokenVerificationKey

        LOGGER.debug("Access token verification public key was refreshed successfully.")
        return jwtTokenVerificationKey ?: throw NullPointerException("The access token verification key was null")
    }

    private fun getKeyQueryUrl(keyType: OIDCKeyType): String {
        return "aaa"
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(JwtTokenVerificationKeyService::class.java)
    }
}