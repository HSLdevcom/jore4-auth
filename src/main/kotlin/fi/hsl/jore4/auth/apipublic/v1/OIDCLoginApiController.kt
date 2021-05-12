package fi.hsl.jore4.auth.apipublic.v1

import fi.hsl.jore4.auth.authentication.OIDCProperties
import fi.hsl.jore4.auth.authentication.OIDCUtil
import fi.hsl.jore4.auth.authentication.SessionKeys
import fi.hsl.jore4.auth.common.ApiRedirectUtil.Companion.createRedirect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder.fromUriString
import java.net.URI
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/public/v1.0")
open class OIDCLoginApiController(
        private val oidcProperties: OIDCProperties,
        private val request: HttpServletRequest
) : LoginApi {

    override fun login(clientRedirectUrl: String?, locale: String?): ResponseEntity<Void> {
        val state = UUID.randomUUID().toString()
        val urlBuilder = fromUriString(oidcProperties.loginUri)
                .queryParam("response_type", "code")
                .queryParam("client_id", oidcProperties.clientId)
                .queryParam("redirect_uri", OIDCUtil.buildRedirectUri(oidcProperties.redirectUri, clientRedirectUrl))
                .queryParam("state", state)
                .queryParam("type", oidcProperties.clientType)
                .queryParam("scope", "profile")//"externalpermissions.query")

        if (locale != null) {
            urlBuilder.queryParam("locale", locale)
        }
        val loginUri: URI = urlBuilder
                .build()
                .encode()
                .toUri()

        LOG.info("Redirecting to OIDC login: {}", loginUri)

        request.session?.setAttribute(SessionKeys.OIDC_STATE_KEY, state)
                ?: throw IllegalArgumentException("Session not found")
        return createRedirect(loginUri)
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(OIDCLoginApiController::class.java)
    }
}