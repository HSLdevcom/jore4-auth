package fi.hsl.jore4.auth.apipublic.v1

import fi.hsl.jore4.auth.account.AccountService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/v1.0")
open class AccountApiController(
        private val accountService: AccountService
) : AccountApi {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(AccountApiController::class.java)
    }

    override fun getAccount(): ResponseEntity<Map<String, Any>> {
        LOGGER.info("Fetching account information for currently logged in user")

        val account = accountService.getAccount()
        LOGGER.info("Returning account information: {}", account)

        return ResponseEntity.ok(account)
    }
}