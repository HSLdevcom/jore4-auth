package fi.hsl.jore4.auth.apipublic.v1

import fi.hsl.jore4.auth.apipublic.v1.model.SessionApiDTO
import fi.hsl.jore4.auth.hasura.HasuraAuthService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Hasura-specific endpoints.
 */
@RestController
@RequestMapping("\${api.path.prefix}/public/v1")
open class HasuraApiController(
    private val hasuraAuthService: HasuraAuthService,
    private val request: HttpServletRequest
) : HasuraApi {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(HasuraApiController::class.java)
    }

    override fun webhook(): ResponseEntity<SessionApiDTO> {
        LOGGER.info("Fetching Hasura session")

        val hasuraSession = hasuraAuthService.getSession(request)
        LOGGER.info("Fetched Hasura session: {}", hasuraSession)

        return ResponseEntity.ok(hasuraSession)
    }
}
