package fi.hsl.jore4.auth.apipublic.v1

import com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.nimbusds.oauth2.sdk.token.RefreshToken
import fi.hsl.jore4.auth.Constants
import fi.hsl.jore4.auth.Constants.OIDC_PROVIDER_TOKEN_ENDPOINT_PATH
import fi.hsl.jore4.auth.Constants.OIDC_PROVIDER_USERINFO_ENDPOINT_PATH
import fi.hsl.jore4.auth.Constants.OIDC_PROVIDER_USERINFO_RESPONSE
import fi.hsl.jore4.auth.Constants.OIDC_REFRESH_TOKEN
import fi.hsl.jore4.auth.IntegrationTestContext
import fi.hsl.jore4.auth.MockOIDCProvider
import fi.hsl.jore4.auth.MockOIDCProvider.createJwtAccessToken
import fi.hsl.jore4.auth.TestTags
import fi.hsl.jore4.auth.oidc.SessionKeys
import fi.hsl.jore4.auth.oidc.UserTokenSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content

/**
 * NB: This test class formally tests {@code UserInfoApiController} functionality. However, in reality most of the
 * features tested originate from the {@code OIDCAuthInterceptor}.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [IntegrationTestContext::class])
@AutoConfigureMockMvc
@Tag(TestTags.INTEGRATION_TEST)
class UserInfoApiControllerTest(
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
    @DisplayName("Verify that the control flow is correct")
    inner class VerifyThatControlFlowIsCorrect {

        @Nested
        @DisplayName("Should not invoke token endpoint or retry")
        inner class ShouldNotInvokeTokenEndpointOrRetry {

            @Test
            @DisplayName("If no session is present")
            fun ifNoSessionIsPresent() {
                with(MockOIDCProvider) {
                    returnUserInfo()
                }

                requestBuilder.getUserInfo()

                verify(0, anyRequestedFor(urlEqualTo(OIDC_PROVIDER_TOKEN_ENDPOINT_PATH)))
                verify(1, getRequestedFor(urlEqualTo(OIDC_PROVIDER_USERINFO_ENDPOINT_PATH)))
            }

            @Test
            @DisplayName("If no access token is present")
            fun ifNoAccessTokenIsPresent() {
                with(MockOIDCProvider) {
                    returnUserInfo()
                }

                requestBuilder.getUserInfo(MockHttpSession())

                verify(0, anyRequestedFor(urlEqualTo(OIDC_PROVIDER_TOKEN_ENDPOINT_PATH)))
                verify(1, getRequestedFor(urlEqualTo(OIDC_PROVIDER_USERINFO_ENDPOINT_PATH)))
            }

            @Test
            @DisplayName("If user info endpoint responds 401 Unauthorized and access token is invalid")
            fun ifUserInfoRespondsUnauthorizedAccessTokenHasExpired() {
                with(MockOIDCProvider) {
                    returnUserInfo()
                    returnTokensForCorrectRefreshToken("aNewAccessToken", "aNewRefreshToken")
                }

                requestBuilder.getUserInfo(sessionWithUserTokens("invalidAccessToken", OIDC_REFRESH_TOKEN))

                verify(0, anyRequestedFor(urlEqualTo(OIDC_PROVIDER_TOKEN_ENDPOINT_PATH)))
                verify(1, getRequestedFor(urlEqualTo(OIDC_PROVIDER_USERINFO_ENDPOINT_PATH)))
            }
        }

        @Nested
        @DisplayName("Should invoke token endpoint but not retry")
        inner class ShouldInvokeTokenEndpointButNotRetry {

            @Test
            @DisplayName("If access token has expired and new access token is invalid")
            fun ifAccessTokenHasExpiredAndNewAccessTokenIsInvalid() {
                with(MockOIDCProvider) {
                    returnUserInfo()
                    returnTokensForCorrectRefreshToken("aNewInvalidAccessToken", "aNewRefreshToken")
                }

                requestBuilder.getUserInfo(
                    sessionWithUserTokens(
                        createJwtAccessToken(
                            issuedAt = System.currentTimeMillis() - (60 * 1000),
                            expiresAt = System.currentTimeMillis() - (30 * 1000)
                        ), OIDC_REFRESH_TOKEN
                    )
                )

                verify(1, postRequestedFor(urlEqualTo(OIDC_PROVIDER_TOKEN_ENDPOINT_PATH)))
                verify(1, getRequestedFor(urlEqualTo(OIDC_PROVIDER_USERINFO_ENDPOINT_PATH)))
            }
        }

        @Nested
        @DisplayName("Should invoke token endpoint and retry")
        inner class ShouldInvokeTokenEndpointAndRetry {

            @Test
            @DisplayName("If access token has expired and new access token is valid")
            fun ifAccessTokenHasExpiredAndNewAccessTokenIsValid() {
                with(MockOIDCProvider) {
                    returnUserInfo()
                    returnTokensForCorrectRefreshToken(createJwtAccessToken(), "aNewRefreshToken")
                }

                requestBuilder.getUserInfo(
                    sessionWithUserTokens(
                        createJwtAccessToken(
                            issuedAt = System.currentTimeMillis() - (60 * 1000),
                            expiresAt = System.currentTimeMillis() - (30 * 1000)
                        ), OIDC_REFRESH_TOKEN
                    )
                )

                verify(
                    1, postRequestedFor(urlEqualTo(OIDC_PROVIDER_TOKEN_ENDPOINT_PATH))
                        .withRequestBody(
                            containing(
                                MockOIDCProvider.urlEncodedParameter(
                                    "grant_type",
                                    "refresh_token"
                                )
                            )
                        )
                        .withRequestBody(
                            containing(
                                MockOIDCProvider.urlEncodedParameter(
                                    "refresh_token",
                                    OIDC_REFRESH_TOKEN
                                )
                            )
                        )
                )

                verify(2, getRequestedFor(urlEqualTo(OIDC_PROVIDER_USERINFO_ENDPOINT_PATH)))
            }
        }
    }

    @Nested
    @DisplayName("Verify that the correct response is sent")
    inner class VerifyThatCorrectResponseIsSent {

        @Nested
        @DisplayName("Should respond 401 Unauthorized")
        inner class ShouldRespondUnauthorized {

            @Test
            @DisplayName("If user info endpoint responds 401 Unauthorized and access token is invalid")
            fun ifUserInfoRespondsUnauthorizedAccessTokenHasExpired() {
                with(MockOIDCProvider) {
                    returnUserInfo()
                    returnTokensForCorrectRefreshToken("aNewAccessToken", "aNewRefreshToken")
                }

                requestBuilder.getUserInfo(sessionWithUserTokens("invalidAccessToken", OIDC_REFRESH_TOKEN))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                    .andExpect(MockMvcResultMatchers.content().string(""))
            }

            @Test
            @DisplayName("If access token has expired and new access token is invalid")
            fun ifAccessTokenHasExpiredAndNewAccessTokenIsInvalid() {
                with(MockOIDCProvider) {
                    returnUserInfo()
                    returnTokensForCorrectRefreshToken("aNewInvalidAccessToken", "aNewRefreshToken")
                }

                requestBuilder.getUserInfo(
                    sessionWithUserTokens(
                        createJwtAccessToken(
                            issuedAt = System.currentTimeMillis() - (60 * 1000),
                            expiresAt = System.currentTimeMillis() - (30 * 1000)
                        ), OIDC_REFRESH_TOKEN
                    )
                )
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                    .andExpect(MockMvcResultMatchers.content().string(""))
            }
        }

        @Nested
        @DisplayName("Should return user info content")
        inner class ShouldReturnUserInfoContent {

            @Test
            @DisplayName("If valid access token is present")
            fun ifValidAccessTokenIsPresent() {
                val accessToken = createJwtAccessToken()

                with(MockOIDCProvider) {
                    returnUserInfo(accessToken)
                }

                requestBuilder.getUserInfo(sessionWithUserTokens(accessToken))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(content().json(OIDC_PROVIDER_USERINFO_RESPONSE))
            }

            @Test
            @DisplayName("If access token has expired and new access token is valid")
            fun ifAccessTokenHasExpiredAndNewAccessTokenIsValid() {
                val newAccessToken = createJwtAccessToken()

                with(MockOIDCProvider) {
                    returnUserInfo(newAccessToken)
                    returnTokensForCorrectRefreshToken(newAccessToken, "aNewRefreshToken")
                }

                requestBuilder.getUserInfo(
                    sessionWithUserTokens(
                        createJwtAccessToken(
                            issuedAt = System.currentTimeMillis() - (60 * 1000),
                            expiresAt = System.currentTimeMillis() - (30 * 1000)
                        )
                    )
                )
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(content().json(OIDC_PROVIDER_USERINFO_RESPONSE))
            }
        }
    }

    @Nested
    @DisplayName("Verify that the tokens are updated correctly")
    inner class VerifyThatTokensAreUpdatedCorrectly {

        @Nested
        @DisplayName("Should not store tokens in session")
        inner class ShouldNotStoreTokensInSession {

            @Test
            @DisplayName("If no token set is present")
            fun ifNoTokenSetIsPresent() {
                with(MockOIDCProvider) {
                    returnUserInfo()
                }

                val session = MockHttpSession()

                assertNull(session.getAttribute(SessionKeys.USER_TOKEN_SET_KEY))

                requestBuilder.getUserInfo(session)

                assertNull(session.getAttribute(SessionKeys.USER_TOKEN_SET_KEY))
            }

            @Test
            @DisplayName("If user info endpoint responds 401 Unauthorized and access token is invalid")
            fun ifUserInfoRespondsUnauthorizedAccessTokenHasExpired() {
                with(MockOIDCProvider) {
                    returnUserInfo()
                    returnTokensForCorrectRefreshToken("aNewAccessToken", "aNewRefreshToken")
                }

                val origAccessToken = "invalidAccessToken"
                val origRefreshToken = OIDC_REFRESH_TOKEN
                val session = sessionWithUserTokens(origAccessToken, origRefreshToken)

                requestBuilder.getUserInfo(session)

                val userTokenSet = session.getAttribute(SessionKeys.USER_TOKEN_SET_KEY) as UserTokenSet?
                assertNotNull(userTokenSet)

                assertEquals(origAccessToken, userTokenSet!!.accessToken.toString())
                assertEquals(origRefreshToken, userTokenSet.refreshToken.toString())
            }

            @Test
            @DisplayName("If access token has expired and new access token is invalid")
            fun ifAccessTokenHasExpiredAndNewAccessTokenIsInvalid() {
                with(MockOIDCProvider) {
                    returnUserInfo()
                    returnTokensForCorrectRefreshToken("aNewInvalidAccessToken", "aNewRefreshToken")
                }

                val origAccessToken = createJwtAccessToken(
                    issuedAt = System.currentTimeMillis() - (60 * 1000),
                    expiresAt = System.currentTimeMillis() - (30 * 1000)
                )
                val origRefreshToken = OIDC_REFRESH_TOKEN
                val session = sessionWithUserTokens(origAccessToken, origRefreshToken)

                requestBuilder.getUserInfo(session)

                val userTokenSet = session.getAttribute(SessionKeys.USER_TOKEN_SET_KEY) as UserTokenSet?
                assertNotNull(userTokenSet)

                assertEquals(origAccessToken, userTokenSet!!.accessToken.toString())
                assertEquals(origRefreshToken, userTokenSet.refreshToken.toString())
            }
        }

        @Nested
        @DisplayName("Should update tokens in session")
        inner class ShouldUpdateTokensInSession {
            @Test
            @DisplayName("If access token has expired and new access token is valid")
            fun ifAccessTokenHasExpiredAndNewAccessTokenIsValid() {
                val newAccessToken = createJwtAccessToken()
                val newRefreshToken = "aNewRefreshToken"

                with(MockOIDCProvider) {
                    returnUserInfo()
                    returnTokensForCorrectRefreshToken(newAccessToken, newRefreshToken)
                }

                val origAccessToken = createJwtAccessToken(
                    issuedAt = System.currentTimeMillis() - (60 * 1000),
                    expiresAt = System.currentTimeMillis() - (30 * 1000)
                )
                val origRefreshToken = OIDC_REFRESH_TOKEN
                val session = sessionWithUserTokens(origAccessToken, origRefreshToken)

                assertNotEquals(newAccessToken, origAccessToken)
                assertNotEquals(newRefreshToken, origRefreshToken)

                requestBuilder.getUserInfo(session)

                val userTokenSet = session.getAttribute(SessionKeys.USER_TOKEN_SET_KEY) as UserTokenSet?
                assertNotNull(userTokenSet)

                assertEquals(newAccessToken, userTokenSet!!.accessToken.toString())
                assertEquals(newRefreshToken, userTokenSet.refreshToken.toString())
            }
        }
    }

    private fun sessionWithUserTokens(
        accessToken: String = createJwtAccessToken(),
        refreshToken: String = OIDC_REFRESH_TOKEN
    ): MockHttpSession {
        val session = MockHttpSession()
        session.setAttribute(
            SessionKeys.USER_TOKEN_SET_KEY,
            UserTokenSet(BearerAccessToken(accessToken), RefreshToken(refreshToken))
        )
        return session
    }
}
