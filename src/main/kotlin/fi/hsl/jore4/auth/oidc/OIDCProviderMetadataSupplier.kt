package fi.hsl.jore4.auth.oidc

import com.nimbusds.oauth2.sdk.id.Issuer
import com.nimbusds.openid.connect.sdk.op.OIDCProviderConfigurationRequest
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Updates the OIDC provider metadata on context refresh events and provides it to other components.
 */
@Component
open class OIDCProviderMetadataSupplier(
    private val oidcProperties: OIDCProperties
) {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(OIDCProviderMetadataSupplier::class.java)
    }

    lateinit var providerMetadata: OIDCProviderMetadata
        private set

    /**
     * Update the OIDC provider metadata on context refresh events.
     *
     * This will also update the metadata on application start.
     *
     * The metadata is downloaded from the OIDC discovery endpoint.
     */
    @EventListener(ContextRefreshedEvent::class)
    fun updateProviderMetadata() {
        LOGGER.info("Updating OIDC provider metadata...")

        // the OpenID provider issuer URL. Use provider base url if it is not defined
        val issuer = Issuer(oidcProperties.issuer.ifEmpty { oidcProperties.providerBaseUrl })

        // fetch the OpenID provider metadata from the discovery endpoint
        val request = OIDCProviderConfigurationRequest(issuer)
        val response = request.toHTTPRequest().send()

        // parse the metadata
        val opMetadata = OIDCProviderMetadata.parse(response.bodyAsJSONObject)

        if (opMetadata.issuer != issuer) throw IllegalStateException("Invalid OIDC issuer")

        providerMetadata = opMetadata

        LOGGER.info("Updated OIDC provider metadata: {}", providerMetadata)
    }
}
