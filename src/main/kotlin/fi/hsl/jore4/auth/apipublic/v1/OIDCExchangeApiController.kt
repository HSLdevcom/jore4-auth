package fi.hsl.jore4.auth.apipublic.v1

import com.nimbusds.oauth2.sdk.AuthorizationCode
import com.nimbusds.oauth2.sdk.id.State
import fi.hsl.jore4.auth.authentication.OIDCExchangeService
import fi.hsl.jore4.auth.authentication.OIDCProperties
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.URLEncoder
import javax.servlet.http.HttpSession

@RestController
class OIDCExchangeApiController(
        private val exchangeService: OIDCExchangeService
) {
    companion object {
        const final val EXCHANGE_ENDPOINT_PATH = "/api/v1.0/oidc/exchange"
        const final val CLIENT_REDIRECT_URL_QUERY_PARAM = "clientRedirectUrl"

        fun createCallbackUrl(clientBaseUrl: String, clientRedirectUrl: String?): URI {
            var builder = UriComponentsBuilder
                .fromUriString(clientBaseUrl)
                .path(EXCHANGE_ENDPOINT_PATH)

            if (clientRedirectUrl != null) {
                builder = builder.queryParam(CLIENT_REDIRECT_URL_QUERY_PARAM, URLEncoder.encode(clientRedirectUrl, "UTF-8"))
            }

            return builder.build()
                .encode()
                .toUri()
        }
    }

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
    @RequestMapping(method = [RequestMethod.GET], value = [EXCHANGE_ENDPOINT_PATH])
    fun exchangeTokens(
            @ApiParam("The authorization code passed on by the authorization server.")
            @RequestParam
            code: String,
            @ApiParam("The state to verify the requests authenticity (the state has been generated by us before invoking the auth server's login endpoint).")
            @RequestParam
            state: String,
            @RequestParam(value = CLIENT_REDIRECT_URL_QUERY_PARAM, required = false) clientRedirectUrl: String?,
            session: HttpSession
    ): ResponseEntity<Any> {
        return exchangeService.exchangeTokens(AuthorizationCode(code), State(state), session, clientRedirectUrl)
    }
}