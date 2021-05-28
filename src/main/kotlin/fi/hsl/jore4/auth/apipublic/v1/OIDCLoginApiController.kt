package fi.hsl.jore4.auth.apipublic.v1

import com.nimbusds.oauth2.sdk.ResponseType
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.openid.connect.sdk.AuthenticationRequest
import fi.hsl.jore4.auth.oidc.OIDCProperties
import fi.hsl.jore4.auth.oidc.SessionKeys
import fi.hsl.jore4.auth.common.ApiUtil.Companion.createRedirect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.id.*
import fi.hsl.jore4.auth.oidc.OIDCProviderMetadataSupplier
import fi.hsl.jore4.auth.web.UnauthorizedException

@RestController
@RequestMapping("\${api.path.prefix}/public/v1.0")
open class OIDCLoginApiController(
        private val oidcProperties: OIDCProperties,
        private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier,
        private val oidcExchangeApiController: OIDCExchangeApiController,
        private val request: HttpServletRequest
) : LoginApi {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(OIDCLoginApiController::class.java)

        private const val OIDC_RESPONSE_TYPE = "code"
        private val OIDC_SCOPES = arrayOf("openid", "profile", "externalpermissions.query")
    }

    override fun login(clientRedirectUrl: String?, locale: String?): ResponseEntity<Void> {
        // create the exchange endpoint callback URI, to which the user will be redirected after authentication
        val callbackUri = oidcExchangeApiController.createCallbackUri(clientRedirectUrl)

        // generate a random state to verify the callback request
        val state = State()

        // generate the redirect URI at which the user will authenticate
        val authRequestUri = AuthenticationRequest.Builder(
            ResponseType(OIDC_RESPONSE_TYPE),
            Scope(*OIDC_SCOPES),
            ClientID(oidcProperties.clientId),
            callbackUri
        )
            .endpointURI(oidcProviderMetadataSupplier.providerMetadata.authorizationEndpointURI)
            .state(state)
            .build()
            .toURI()

        request.session?.setAttribute(SessionKeys.OIDC_STATE_KEY, state)
                ?: throw UnauthorizedException("Session not found")

        LOGGER.info("Redirecting to OIDC login: {}", authRequestUri)

        return createRedirect(authRequestUri)
    }
}