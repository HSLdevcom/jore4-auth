package fi.hsl.jore4.auth.apipublic.v1

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.State
import fi.hsl.jore4.auth.Constants
import fi.hsl.jore4.auth.Constants.LOGIN_PAGE_URL
import fi.hsl.jore4.auth.Constants.OIDC_PROVIDER_TOKEN_ENDPOINT_PATH
import fi.hsl.jore4.auth.Constants.OIDC_REFRESH_TOKEN
import fi.hsl.jore4.auth.IntegrationTestContext
import fi.hsl.jore4.auth.MockOIDCProvider
import fi.hsl.jore4.auth.TestTags
import fi.hsl.jore4.auth.oidc.SessionKeys
import fi.hsl.jore4.auth.oidc.UserTokenSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [IntegrationTestContext::class])
@AutoConfigureMockMvc
@Tag(TestTags.INTEGRATION_TEST)
class OIDCCodeExchangeApiControllerTest(
    @Autowired private val mockMvc: MockMvc
) {
    private val requestBuilder = OIDCApiRequestBuilder(mockMvc, Constants.API_PATH_PREFIX)

    @BeforeEach
    fun setup() {
        with (MockOIDCProvider) {
            reset()
            returnJwksContent()
        }
    }

    @Nested
    @DisplayName("Should respond 401 Unauthorized")
    inner class ShouldRespondUnauthorized {
        @Test
        @DisplayName("If invoked without session")
        fun ifInvokedWithoutSession() {
            requestBuilder.exchangeCode(Constants.OIDC_AUTHORIZATION_CODE, Constants.OIDC_STATE)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andExpect(MockMvcResultMatchers.content().string(""))
        }

        @Test
        @DisplayName("If invoked with wrong state")
        fun ifInvokedWithWrongState() {
            requestBuilder.exchangeCode(Constants.OIDC_AUTHORIZATION_CODE, "abcdefghijklmnop", sessionWithState())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andExpect(MockMvcResultMatchers.content().string(""))
        }

        @Test
        @DisplayName("If access token signing key cannot be found")
        fun ifAccessTokenSigningKeyCannotBeFound() {
            with(MockOIDCProvider) {
                returnTokensForCorrectCodeAndCredentials(createJwtAccessToken(keyId = "doesnotexistreallynot"))
            }

            requestBuilder.exchangeCode(Constants.OIDC_AUTHORIZATION_CODE, Constants.OIDC_STATE, sessionWithState())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andExpect(MockMvcResultMatchers.content().string(""))
        }

        @Test
        @DisplayName("If access token issuer is incorrect")
        fun ifAccessTokenIssuerIsIncorrect() {
            with(MockOIDCProvider) {
                returnTokensForCorrectCodeAndCredentials(createJwtAccessToken(issuer = "notmereallynot"))
            }

            requestBuilder.exchangeCode(Constants.OIDC_AUTHORIZATION_CODE, Constants.OIDC_STATE, sessionWithState())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andExpect(MockMvcResultMatchers.content().string(""))
        }

        @Test
        @DisplayName("If access token audience is incorrect")
        fun ifAccessTokenAudienceIsIncorrect() {
            with(MockOIDCProvider) {
                returnTokensForCorrectCodeAndCredentials(createJwtAccessToken(audience = "notyoureallynot"))
            }

            requestBuilder.exchangeCode(Constants.OIDC_AUTHORIZATION_CODE, Constants.OIDC_STATE, sessionWithState())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andExpect(MockMvcResultMatchers.content().string(""))
        }

        @Test
        @DisplayName("If access token has expired")
        fun ifAccessTokenHasExpired() {
            with(MockOIDCProvider) {
                returnTokensForCorrectCodeAndCredentials(
                    createJwtAccessToken(
                        issuedAt = System.currentTimeMillis() - (60 * 1000),
                        expiresAt = System.currentTimeMillis() - (30 * 1000)
                    )
                )
            }

            requestBuilder.exchangeCode(Constants.OIDC_AUTHORIZATION_CODE, Constants.OIDC_STATE, sessionWithState())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andExpect(MockMvcResultMatchers.content().string(""))
        }
    }

    @Test
    @DisplayName("Should invoke token endpoint")
    fun shouldInvokeTokenEndpoint() {
        val someAuthCode = "randomCodeMayNotBeValid"

        requestBuilder.exchangeCode(someAuthCode, Constants.OIDC_STATE, sessionWithState())

        verify(
            1, postRequestedFor(urlEqualTo(OIDC_PROVIDER_TOKEN_ENDPOINT_PATH))
                .withHeader("Authorization", WireMock.equalTo(
                    ClientSecretBasic(ClientID(Constants.OIDC_CLIENT_ID), Secret(Constants.OIDC_CLIENT_SECRET))
                        .toHTTPAuthorizationHeader()
                ))
                .withRequestBody(
                    containing(
                        MockOIDCProvider.urlEncodedParameter(
                            "grant_type",
                            "authorization_code"
                        )
                    )
                )
                .withRequestBody(
                    containing(
                        MockOIDCProvider.urlEncodedParameter(
                            "code",
                            someAuthCode
                        )
                    )
                )
                .withRequestBody(
                    containing(
                        MockOIDCProvider.urlEncodedParameter(
                            "redirect_uri",
                            Constants.CODE_EXCHANGE_CALLBACK_URI
                        )
                    )
                )
        )
    }

    @Test
    @DisplayName("Should redirect the user to the login page")
    fun shouldRedirectUserToLoginPage() {
        with (MockOIDCProvider) {
            returnTokensForCorrectCodeAndCredentials(createJwtAccessToken())
        }

        requestBuilder.exchangeCode(Constants.OIDC_AUTHORIZATION_CODE, Constants.OIDC_STATE, sessionWithState())
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
            .andExpect(
                MockMvcResultMatchers.header().string(
                    "Location",
                    LOGIN_PAGE_URL
                )
            )
    }

    @Test
    @DisplayName("Should store the user token set in the session")
    fun shouldStoreTheUserTokenSetInSession() {
        val accessToken = MockOIDCProvider.createJwtAccessToken()
        MockOIDCProvider.returnTokensForCorrectCodeAndCredentials(accessToken)

        val session = sessionWithState()
        assertNull(session.getAttribute(SessionKeys.USER_TOKEN_SET_KEY))

        requestBuilder.exchangeCode(Constants.OIDC_AUTHORIZATION_CODE, Constants.OIDC_STATE, session)

        val userTokenSetObject = session.getAttribute(SessionKeys.USER_TOKEN_SET_KEY)
        assertNotNull(userTokenSetObject)

        val userTokenSet = userTokenSetObject as UserTokenSet
        assertEquals(accessToken, userTokenSet.accessToken.toString())
        assertEquals(OIDC_REFRESH_TOKEN, userTokenSet.refreshToken.toString())
    }

    private fun sessionWithState(state: String = Constants.OIDC_STATE): MockHttpSession {
        val session = MockHttpSession()
        session.setAttribute(SessionKeys.OIDC_STATE_KEY, State(state))
        return session
    }
}
