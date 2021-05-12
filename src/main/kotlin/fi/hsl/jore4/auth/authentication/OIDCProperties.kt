package fi.hsl.jore4.auth.authentication

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("oidc")
open class OIDCProperties {
    lateinit var clientId: String

    lateinit var clientType: String

    lateinit var clientSecret: String

    lateinit var integrationTokenVerificationKeyUri: String

    lateinit var redirectUri: String

    lateinit var loginUri: String

    lateinit var logoutUri: String

    lateinit var tokenUri: String

    lateinit var userTokenVerificationKeyUri: String

    lateinit var profilePage: String

    lateinit var frontPage: String
}