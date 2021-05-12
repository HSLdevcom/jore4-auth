package fi.hsl.jore4.auth.apipublic.v1

import fi.hsl.jore4.auth.common.ApiUtil.createRedirect
import fi.hsl.jore4.auth.oidc.OIDCLoginService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import fi.hsl.jore4.auth.web.UnauthorizedException

/**
 * Endpoint to log the user in.
 */
@RestController
@RequestMapping("\${api.path.prefix}/public/v1")
open class OIDCLoginApiController(
    private val oidcLoginService: OIDCLoginService,
    private val request: HttpServletRequest
) : LoginApi {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(OIDCLoginApiController::class.java)
    }

    /**
     * Log the user in by redirecting her to the OIDC authorization endpoint.
     */
    override fun login(): ResponseEntity<Void> {
        LOGGER.info("Creating login URI")

        if (request.session == null) {
            throw UnauthorizedException("Session not found")
        }

        val loginUri = oidcLoginService.createLoginUri(request.session)

        LOGGER.info("Redirecting to OIDC login: {}", loginUri)

        return createRedirect(loginUri)
    }
}
