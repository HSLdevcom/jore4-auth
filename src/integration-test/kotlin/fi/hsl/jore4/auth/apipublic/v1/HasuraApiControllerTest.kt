package fi.hsl.jore4.auth.apipublic.v1

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.nimbusds.oauth2.sdk.token.RefreshToken
import fi.hsl.jore4.auth.Constants
import fi.hsl.jore4.auth.IntegrationTestContext
import fi.hsl.jore4.auth.MockOIDCProvider
import fi.hsl.jore4.auth.TestTags
import fi.hsl.jore4.auth.hasura.HasuraAuthService
import fi.hsl.jore4.auth.oidc.SessionKeys
import fi.hsl.jore4.auth.oidc.UserTokenSet
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [IntegrationTestContext::class])
@AutoConfigureMockMvc
@Tag(TestTags.INTEGRATION_TEST)
class HasuraApiControllerTest(
    @Autowired private val mockMvc: MockMvc
) {
    private val requestBuilder = HasuraApiRequestBuilder(mockMvc, Constants.API_PATH_PREFIX)

    private val accessToken = MockOIDCProvider.createJwtAccessToken()
    private val roUserInfoContent = MockOIDCProvider.createUserInfoResponseContent(
        Constants.RO_USER_SUB, Constants.RO_USER_FIRST_NAME, Constants.RO_USER_LAST_NAME, Constants.RO_USER_FULL_NAME,
        Pair(Constants.RO_USER_EXT_PERMISSION_ID, Constants.RO_USER_EXT_PERMISSION_EXT_ID)
    )
    private val rwUserInfoContent = MockOIDCProvider.createUserInfoResponseContent(
        Constants.RW_USER_SUB, Constants.RW_USER_FIRST_NAME, Constants.RW_USER_LAST_NAME, Constants.RW_USER_FULL_NAME,
        Pair(Constants.RW_USER_EXT_PERMISSION_1_ID, Constants.RW_USER_EXT_PERMISSION_1_EXT_ID),
        Pair(Constants.RW_USER_EXT_PERMISSION_2_ID, Constants.RW_USER_EXT_PERMISSION_2_EXT_ID)
    )

    @BeforeEach
    fun setup() {
        MockOIDCProvider.reset()
    }

    @Nested
    @DisplayName("Should respond 401 Unauthorized")
    inner class ShouldRespondUnauthorized {

        @Test
        @DisplayName("If no session is present")
        fun ifNoSessionIsPresent() {
            with(MockOIDCProvider) {
                returnUserInfo(roUserInfoContent, accessToken)
            }

            requestBuilder.webhook(session = null, requestedRole = Constants.RO_USER_EXT_PERMISSION_EXT_ID)
                .andExpect(status().isUnauthorized)
                .andExpect(content().string(""))
        }

        @Test
        @DisplayName("If no access token is present")
        fun ifNoAccessTokenIsPresent() {
            with(MockOIDCProvider) {
                returnUserInfo(roUserInfoContent, accessToken)
            }

            requestBuilder.webhook(MockHttpSession(), Constants.RO_USER_EXT_PERMISSION_EXT_ID)
                .andExpect(status().isUnauthorized)
                .andExpect(content().string(""))
        }

        @Test
        @DisplayName("If user does not have requested permission")
        fun ifUserDoesNotHaveRequestedPermission() {
            with(MockOIDCProvider) {
                returnUserInfo(roUserInfoContent, accessToken)
            }

            val session = MockHttpSession()
            session.setAttribute(
                SessionKeys.USER_TOKEN_SET_KEY,
                UserTokenSet(BearerAccessToken(accessToken), RefreshToken(Constants.OIDC_REFRESH_TOKEN))
            )

            requestBuilder.webhook(session, "userDoesNotHaveThisPermission")
                .andExpect(status().isUnauthorized)
                .andExpect(content().string(""))
        }
    }

    @Nested
    @DisplayName("Should reply with the Hasura session")
    inner class ShouldWithHasuraSession {
        @Test
        @DisplayName("If user has requested permission as only permission")
        fun ifUserHasRequestedPermissionAsOnlyPermission() {
            with(MockOIDCProvider) {
                returnUserInfo(roUserInfoContent, accessToken)
            }

            val session = MockHttpSession()
            session.setAttribute(
                SessionKeys.USER_TOKEN_SET_KEY,
                UserTokenSet(BearerAccessToken(accessToken), RefreshToken(Constants.OIDC_REFRESH_TOKEN))
            )

            requestBuilder.webhook(session, Constants.RO_USER_EXT_PERMISSION_EXT_ID)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.X-Hasura-User-Id").value(Constants.RO_USER_SUB))
                .andExpect(jsonPath("$.X-Hasura-Role").value(Constants.RO_USER_EXT_PERMISSION_EXT_ID))
                .andExpect(jsonPath("$.Cache-Control").value(HasuraAuthService.CACHE_CONTROL_SPEC))
        }

        @Test
        @DisplayName("If user has requested permission as one of more permissions")
        fun ifUserHasRequestedPermissionAsOneOfMorePermissions() {
            with(MockOIDCProvider) {
                returnUserInfo(rwUserInfoContent, accessToken)
            }

            val session = MockHttpSession()
            session.setAttribute(
                SessionKeys.USER_TOKEN_SET_KEY,
                UserTokenSet(BearerAccessToken(accessToken), RefreshToken(Constants.OIDC_REFRESH_TOKEN))
            )

            requestBuilder.webhook(session, Constants.RW_USER_EXT_PERMISSION_2_EXT_ID)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.X-Hasura-User-Id").value(Constants.RW_USER_SUB))
                .andExpect(jsonPath("$.X-Hasura-Role").value(Constants.RW_USER_EXT_PERMISSION_2_EXT_ID))
                .andExpect(jsonPath("$.Cache-Control").value(HasuraAuthService.CACHE_CONTROL_SPEC))
        }
    }
}
