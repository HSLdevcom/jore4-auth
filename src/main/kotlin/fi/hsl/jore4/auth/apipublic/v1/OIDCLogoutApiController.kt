package fi.hsl.jore4.auth.apipublic.v1

import fi.hsl.jore4.auth.authentication.OIDCProperties
import fi.hsl.jore4.auth.authentication.OIDCProviderMetadataSupplier
import fi.hsl.jore4.auth.common.ApiRedirectUtil.Companion.createRedirect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder.fromUri
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/public/v1.0")
open class OIDCLogoutApiController(
        private val oidcProperties: OIDCProperties,
        private val oidcProviderMetadataSupplier: OIDCProviderMetadataSupplier,
        private val request: HttpServletRequest
) : LogoutApi {

    override fun logout(): ResponseEntity<Void> {
        val logoutUri = fromUri(oidcProviderMetadataSupplier.providerMetadata.endSessionEndpointURI)
                .queryParam("client_id", oidcProperties.clientId)
                .build()
                .encode()
                .toUri()

        LOG.info("Redirecting to OIDC logout: {}", logoutUri)

        request.session?.invalidate()

        return createRedirect(logoutUri)
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(OIDCLogoutApiController::class.java)
    }
}