package fi.hsl.jore4.auth.apipublic.v1

import fi.hsl.jore4.auth.account.AccountService
import fi.hsl.jore4.auth.apipublic.v1.model.AccountApiDTO
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

    override fun getActiveAccount(): ResponseEntity<AccountApiDTO> {
        LOGGER.info("Fetching account information for currently logged in user")

        val accountDTO: AccountApiDTO = accountService.getActiveUserAccount()
        LOGGER.info("Returning account information: {}", accountDTO)

        return ResponseEntity.ok(accountDTO)
    }
}