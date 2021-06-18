package fi.hsl.jore4.auth

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.http.MediaType
import java.net.URLEncoder
import java.util.*

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
                .willReturn(WireMock.okForContentType(MediaType.APPLICATION_JSON_VALUE, Constants.OIDC_PROVIDER_DISCOVERY_CONTENT)))
    }

    fun returnTokensForCorrectCodeAndCredentials(accessToken: String) {
        WireMock
            .givenThat(WireMock.post(WireMock.urlEqualTo(Constants.OIDC_PROVIDER_TOKEN_ENDPOINT_PATH))
                .atPriority(1)
                .withHeader("Authorization", equalTo(
                    ClientSecretBasic(ClientID(Constants.OIDC_CLIENT_ID), Secret(Constants.OIDC_CLIENT_SECRET))
                        .toHTTPAuthorizationHeader()
                ))
                .withRequestBody(containing(urlEncodedParameter("grant_type", "authorization_code")))
                .withRequestBody(containing(urlEncodedParameter("code", Constants.OIDC_AUTHORIZATION_CODE)))
                .withRequestBody(containing(urlEncodedParameter("redirect_uri", Constants.CODE_EXCHANGE_CALLBACK_URI)))
                .willReturn(WireMock.okForContentType(MediaType.APPLICATION_JSON_VALUE,
                    String.format(Constants.OIDC_PROVIDER_TOKEN_RESPONSE_TEMPLATE, accessToken, Constants.OIDC_REFRESH_TOKEN)))
            )

        WireMock
            .givenThat(WireMock.post(WireMock.urlEqualTo(Constants.OIDC_PROVIDER_TOKEN_ENDPOINT_PATH))
                .atPriority(2)
                .willReturn(WireMock.unauthorized())
            )
    }

    fun returnJwksContent() {
        WireMock
            .givenThat(WireMock.get(WireMock.urlEqualTo(Constants.OIDC_PROVIDER_JWKS_URI_PATH))
                .willReturn(WireMock.okForContentType(MediaType.APPLICATION_JSON_VALUE, Constants.OIDC_PROVIDER_JWKS_RESPONSE)))
    }

    fun urlEncodedParameter(param: String, value: String) =
        "$param=${URLEncoder.encode(value, "UTF-8")}"

    fun createJwtAccessToken(
        issuedAt: Long = System.currentTimeMillis(),
        expiresAt: Long = issuedAt + (60 * 60 * 1000), // 1h
        issuer: String = Constants.OIDC_PROVIDER_BASE_URL,
        keyId: String = Constants.OIDC_PROVIDER_SIGNING_KEY_ID,
        audience: String = Constants.OIDC_CLIENT_ID
    ): String {

        val signatureAlgorithm = SignatureAlgorithm.RS256
        val signingKey = JWK.parse(Constants.OIDC_PROVIDER_SIGNING_KEY)
            .toRSAKey()
            .toRSAPrivateKey()

        return Jwts.builder()
            .setIssuedAt(Date(issuedAt))
            .setExpiration(Date(expiresAt))
            .setIssuer(issuer)
            .setHeaderParam("kid", keyId)
            .claim("aud", audience)
            .signWith(signatureAlgorithm, signingKey)
            .compact()
    }
}
