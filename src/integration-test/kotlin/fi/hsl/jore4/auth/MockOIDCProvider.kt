package fi.hsl.jore4.auth

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock

/**
 * Singleton to create and manage the WireMock instance used as the OIDC provider in tests.
 */
object MockOIDCProvider {
    private const val OIDC_DISCOVERY_PATH = "/.well-known/openid-configuration"

    private var wireMockServer: WireMockServer

    init {
        wireMockServer = create()
        returnDiscoveryContent()
    }

    /**
     * Do nothing. Just reference this object to have the init block called.
     */
    fun touch() {
    }

    fun reset() {
        wireMockServer.stop()
        wireMockServer = create()
    }

    /**
     * This method creates a new {@code WireMockServer} object,
     * configures the created server and starts it.
     *
     * @return The created WireMock OIDC provider.
     */
    private fun create(): WireMockServer {
        val wireMockServer = WireMockServer(Constants.OIDC_PROVIDER_PORT)
        wireMockServer.start()
        WireMock.configureFor(wireMockServer.port())
        return wireMockServer
    }

    private fun returnDiscoveryContent() {
        WireMock
            .givenThat(WireMock.get(WireMock.urlEqualTo(OIDC_DISCOVERY_PATH))
            .willReturn(WireMock.okForContentType("application/json", Constants.OIDC_PROVIDER_DISCOVERY_CONTENT)))
    }
}
