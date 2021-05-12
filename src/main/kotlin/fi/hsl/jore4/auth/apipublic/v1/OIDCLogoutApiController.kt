package fi.hsl.jore4.auth.apipublic.v1

import fi.hsl.jore4.auth.common.ApiUtil.createRedirect
import fi.hsl.jore4.auth.oidc.OIDCLoginService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

/**
 * Endpoint to log the user out.
 */
@RestController
@RequestMapping("\${api.path.prefix}/public/v1")
open class OIDCLogoutApiController(
    private val loginService: OIDCLoginService,
    private val request: HttpServletRequest
) : LogoutApi {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(OIDCLogoutApiController::class.java)
    }

    /**
     * Log the user out by invalidating her session and redirecting her to the OIDC end session endpoint.
     *
     * TODO: Invalidate session only if logging out was not cancelled at the OIDC end session endpoint.
     */
    override fun logout(): ResponseEntity<Void> {
        LOGGER.info("Invalidating session")

        request.session?.invalidate()

        LOGGER.info("Creating logout URI")

        val logoutUri = loginService.createLogoutUri()

        LOGGER.info("Redirecting to OIDC logout: {}", logoutUri)

        return createRedirect(logoutUri)
    }
}
