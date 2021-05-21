package fi.hsl.jore4.auth.apipublic.v1

import com.nimbusds.oauth2.sdk.ResponseType
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.openid.connect.sdk.AuthenticationRequest
import fi.hsl.jore4.auth.authentication.OIDCProperties
import fi.hsl.jore4.auth.authentication.SessionKeys
import fi.hsl.jore4.auth.common.ApiRedirectUtil.Companion.createRedirect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.openid.connect.sdk.*;
import com.nimbusds.oauth2.sdk.id.*;
import fi.hsl.jore4.auth.authentication.OIDCProviderMetadataSupplier

@RestController
@RequestMapping("/api/public/v1.0")
open class OIDCLoginApiController(
        private val oidcProperties: OIDCProperties,
        private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier,
        private val request: HttpServletRequest
) : LoginApi {

    override fun login(clientRedirectUrl: String?, locale: String?): ResponseEntity<Void> {
        val clientID = ClientID(oidcProperties.clientId)

        // The client callback URL
        val callback = OIDCExchangeApiController.createCallbackUrl(oidcProperties.clientBaseUrl, clientRedirectUrl)

        // Generate random state string to securely pair the callback to this request
        val state = State()

        // Generate nonce for the ID token
        val nonce = Nonce()

        // Compose the OpenID authentication request (for the code flow)
        val authRequestUri = AuthenticationRequest.Builder(
            ResponseType("code"),
            Scope("openid", "profile"),
            clientID,
            callback
        )
            .endpointURI(oidcProviderMetadataSupplier.providerMetadata.authorizationEndpointURI)
            .state(state)
            .nonce(nonce)
            .build()
            .toURI()

        LOG.info("Redirecting to OIDC login: {}", authRequestUri)

        request.session?.setAttribute(SessionKeys.OIDC_STATE_KEY, state)
                ?: throw IllegalArgumentException("Session not found")

        return createRedirect(authRequestUri)
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(OIDCLoginApiController::class.java)
    }
}