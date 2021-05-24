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

    lateinit var providerMetadata: OIDCProviderMetadata
        private set

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(OIDCProviderMetadataSupplier::class.java)
    }

    @EventListener(ContextRefreshedEvent::class)
    fun updateProviderMetadata() {
        LOGGER.info("Updating OIDC provider metadata...")

        // The OpenID provider issuer URL
        val issuer = Issuer(oidcProperties.serverBaseUrl)

        // Will resolve the OpenID provider metadata automatically
        val request = OIDCProviderConfigurationRequest(issuer)

        // Make HTTP request
        val httpRequest = request.toHTTPRequest()
        val httpResponse = httpRequest.send()

        // Parse OpenID provider metadata
        val opMetadata = OIDCProviderMetadata.parse(httpResponse.contentAsJSONObject)

        if (opMetadata.issuer != issuer) throw IllegalStateException("Incorrect OIDC issuer information")

        providerMetadata = opMetadata

        LOGGER.info("Updated OIDC provider metadata: {}", providerMetadata)
    }
}
