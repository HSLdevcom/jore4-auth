package fi.hsl.jore4.auth.oidc

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Grouping of OIDC-related spring properties.
 */
@Configuration
@ConfigurationProperties("oidc")
open class OIDCProperties {
    lateinit var clientId: String
    lateinit var clientSecret: String
    lateinit var serverBaseUrl: String
}
