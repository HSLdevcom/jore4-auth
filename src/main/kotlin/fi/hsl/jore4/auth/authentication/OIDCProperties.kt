package fi.hsl.jore4.auth.authentication

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("oidc")
open class OIDCProperties {
    lateinit var clientId: String

    lateinit var clientSecret: String

    lateinit var serverBaseUrl: String
    lateinit var clientBaseUrl: String
}
