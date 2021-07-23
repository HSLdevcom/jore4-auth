package fi.hsl.jore4.auth.hasura

import fi.hsl.jore4.auth.apipublic.v1.model.SessionApiDTO
import fi.hsl.jore4.auth.userInfo.UserInfoService
import fi.hsl.jore4.auth.web.UnauthorizedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest

/**
 * Hasura authorization.
 */
@Service
open class HasuraAuthService(
    private val userInfoService: UserInfoService
) {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(HasuraAuthService::class.java)

        const val REQUESTED_ROLE_HEADER = "x-hasura-role"

        private const val SESSION_EXPIRATION_TIME_SEC = 30  // Our response is valid for this time
        const val CACHE_CONTROL_SPEC = "max-age=$SESSION_EXPIRATION_TIME_SEC"
    }

    /**
     * Retrieve the Hasura session information for the given request and the logged in user if the request is
     * authorized.
     *
     * The request will be authorized if the specified value of the header {@code REQUESTED_ROLE_HEADER}
     * is found in the currently logged in user's user info permissions.
     *
     * @returns the Hasura session information if the request is authorized.
     *
     * @throws UnauthorizedException if the request was not authorized.
     */
    open fun getSession(request: HttpServletRequest): SessionApiDTO {
        LOGGER.debug("Fetching Hasura session data")

        val requestedRole = request.getHeader(REQUESTED_ROLE_HEADER)
            ?: throw UnauthorizedException("Requested role not specified in request headers")

        val userInfo = userInfoService.getUserInfo()
        LOGGER.debug("Got user info {}", userInfo)

        if (!userInfo.permissions.contains(requestedRole)) {
            throw UnauthorizedException("Requested role not present in user permissions")
        }

        // The requested role was found among the user permissions, so grant the role in the response
        val session = SessionApiDTO().apply {
            xHasuraUserId(userInfo.id)
            xHasuraRole(requestedRole)
            cacheControl = CACHE_CONTROL_SPEC
        }

        LOGGER.debug("Fetched Hasura session data {}", session)
        return session
    }
}
