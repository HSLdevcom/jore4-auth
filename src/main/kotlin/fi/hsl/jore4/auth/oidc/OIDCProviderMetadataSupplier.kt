package fi.hsl.jore4.auth.oidc

import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import com.nimbusds.oauth2.sdk.id.Issuer
import com.nimbusds.openid.connect.sdk.op.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
open class OIDCProviderMetadataSupplier(
    private val oidcProperties: OIDCProperties
) {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(OIDCProviderMetadataSupplier::class.java)
    }

    lateinit var providerMetadata: OIDCProviderMetadata
        private set

    @EventListener(ContextRefreshedEvent::class)
    fun updateProviderMetadata() {
        LOGGER.info("Updating OIDC provider metadata...")

        // the OpenID provider issuer URL
        val issuer = Issuer(oidcProperties.serverBaseUrl)

        // resolve the OpenID provider metadata
        val request = OIDCProviderConfigurationRequest(issuer)
        val response = request.toHTTPRequest().send()

        // parse the metadata
        val opMetadata = OIDCProviderMetadata.parse(response.contentAsJSONObject)

        if (opMetadata.issuer != issuer) throw IllegalStateException("Invalid OIDC issuer")

        providerMetadata = opMetadata

        LOGGER.info("Updated OIDC provider metadata: {}", providerMetadata)
    }
}
