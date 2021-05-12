package fi.hsl.jore4.auth.apipublic.v1

import fi.hsl.jore4.auth.authentication.OIDCExchangeService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpSession

@RestController
@RequestMapping("/api/v1.0/oidc")
class OIDCExchangeApiController(
        private val exchangeService: OIDCExchangeService
) {

    @ApiOperation(
            "This endpoint is invoked by the OIDC authorization server and will exchange the given authorization code for an " +
                    "access and a refresh token. After a successful code exchange, this endpoint will eventually redirect to our " +
                    "profile page."
    )
    @ApiResponses(
            ApiResponse(code = 302, message = "Redirects to our profile page."),
            ApiResponse(code = 401, message = "The authenticity of the request cannot be verified.")
    )
    @ResponseStatus(HttpStatus.FOUND)
    @RequestMapping(method = [RequestMethod.GET], value = ["/exchange"])
    fun exchangeTokens(
            @ApiParam("The authorization code passed on by the authorization server.")
            @RequestParam
            code: String,
            @ApiParam("The state to verify the requests authenticity (the state has been generated by us before invoking the auth server's login endpoint).")
            @RequestParam
            state: String,
            @RequestParam(value = "clientRedirectUrl", required = false) clientRedirectUrl: String?,
            session: HttpSession
    ): ResponseEntity<Any> {
        return exchangeService.exchangeTokens(code, state, session, clientRedirectUrl)
    }
}