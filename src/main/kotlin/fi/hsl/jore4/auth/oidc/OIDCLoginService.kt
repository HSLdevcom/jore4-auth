package fi.hsl.jore4.auth.oidc

import com.nimbusds.oauth2.sdk.ResponseType
import com.nimbusds.oauth2.sdk.Scope
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.openid.connect.sdk.AuthenticationRequest
import fi.hsl.jore4.auth.apipublic.v1.OIDCCodeExchangeApiController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import javax.servlet.http.HttpSession

@Service
open class OIDCLoginService(
    private val oidcProperties: OIDCProperties,
    private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier,
    private val oidcCodeExchangeApiController: OIDCCodeExchangeApiController,
    @Value("\${logoutpage.url}") private val logoutPageUrl: String
) {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(OIDCLoginService::class.java)

        private const val OIDC_RESPONSE_TYPE = "code"
        private val OIDC_SCOPES = arrayOf("openid", "profile", "externalpermissions.query")
    }

    open fun createLoginUri(session: HttpSession, clientRedirectUrl: String?): URI {
        LOGGER.debug("Creating login URI...")

        // create the exchange endpoint callback URI, to which the user will be redirected after authentication
        val callbackUri = oidcCodeExchangeApiController.createCallbackUri(clientRedirectUrl)

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

        session.setAttribute(SessionKeys.OIDC_STATE_KEY, state)

        LOGGER.debug("Created login URI {}", authRequestUri)

        return authRequestUri
    }

    open fun createLogoutUri(): URI {
        LOGGER.debug("Creating logout URI...")

        val logoutUri = UriComponentsBuilder.fromUri(oidcProviderMetadataSupplier.providerMetadata.endSessionEndpointURI)
            .queryParam("client_id", oidcProperties.clientId)
            .queryParam("post_logout_redirect_uri", logoutPageUrl)
            .build()
            .encode()
            .toUri()

        LOGGER.debug("Created logout URI {}", logoutUri)

        return logoutUri
    }
}
