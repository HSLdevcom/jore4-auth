package fi.hsl.jore4.auth.apipublic.v1

import fi.hsl.jore4.auth.apipublic.v1.model.UserInfoApiDTO
import fi.hsl.jore4.auth.userInfo.UserInfoService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Provides access to user info data.
 */
@RestController
@RequestMapping("\${api.path.prefix}/public/v1")
open class UserInfoApiController(
    private val userInfoService: UserInfoService,
) : UserInfoApi {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(UserInfoApiController::class.java)
    }

    /**
     * Retrieve the currently logged in user's user info.
     */
    override fun getUserInfo(): ResponseEntity<UserInfoApiDTO> {
        LOGGER.info("Fetching user info of currently logged in user")

        val userInfo = userInfoService.getUserInfo()
        LOGGER.info("Returning user info: {}", userInfo)

        return ResponseEntity.ok(userInfo)
    }
}
