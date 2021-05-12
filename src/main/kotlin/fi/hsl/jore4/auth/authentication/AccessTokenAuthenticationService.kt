package fi.hsl.jore4.auth.authentication

import com.nimbusds.oauth2.sdk.token.AccessToken
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class AccessTokenAuthenticationService(
        private val jwtTokenParser: JwtTokenParser
) {
    open fun loginUsingAccessToken(accessToken: AccessToken): Authentication {
        try {
            LOG.debug("Logging in using access token...")
            //val claims = jwtTokenParser.parseAndVerify(accessToken)
            val authentication: Authentication = PreAuthenticatedAuthenticationToken(1, accessToken)

            SecurityContextHolder.getContext().authentication = authentication

            LOG.debug("Successfully logged in.")
            return authentication
        } catch (ex: Exception) {
            LOG.debug("Login failed.")

            logout()

            throw ex
        }
    }

    private fun logout() {
        LOG.debug("Logging out...")

        SecurityContextHolder.getContext().authentication = null

        LOG.debug("Logged out.")
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AccessTokenAuthenticationService::class.java)
    }
}