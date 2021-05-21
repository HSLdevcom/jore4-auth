package fi.hsl.jore4.auth.authentication

import com.nimbusds.oauth2.sdk.token.AccessToken
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Service

@Service
open class AccessTokenAuthenticationService(
        private val jwtTokenParser: JwtTokenParser
) {
    open fun loginUsingAccessToken(accessToken: AccessToken): Authentication {
        try {
            LOGGER.debug("Logging in using access token...")
            val claims = jwtTokenParser.parseAndVerify(accessToken.toString())
            val authentication: Authentication = PreAuthenticatedAuthenticationToken(1, accessToken)

            SecurityContextHolder.getContext().authentication = authentication

            LOGGER.debug("Successfully logged in.")
            return authentication
        } catch (ex: Exception) {
            LOGGER.debug("Login failed.")

            logout()

            throw ex
        }
    }

    private fun logout() {
        LOGGER.debug("Logging out...")

        SecurityContextHolder.getContext().authentication = null

        LOGGER.debug("Logged out.")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AccessTokenAuthenticationService::class.java)
    }
}