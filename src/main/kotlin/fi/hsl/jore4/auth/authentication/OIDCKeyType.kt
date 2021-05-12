package fi.hsl.jore4.auth.authentication

/**
 * Identifies different OIDC key types which are used by the different
 * users of this system.
 */
enum class OIDCKeyType(val namePostfix: String, val keyId: String) {

    /**
     * Key for integration services. Key size 4096 bits and validity 10 years.
     */
    INTEGRATION("", "oai"),

    /**
     * Key for user access tokens. Key size 2048 bits and validity 2 years.
     */
    USER_ACCESS_TOKEN("_authz", "uat");


    companion object {

        fun findByKeyId(keyId: String): OIDCKeyType? {
            for (keyType in OIDCKeyType.values()) {
                if (keyType.keyId == keyId) {
                    return keyType
                }
            }

            return null
        }
    }
}