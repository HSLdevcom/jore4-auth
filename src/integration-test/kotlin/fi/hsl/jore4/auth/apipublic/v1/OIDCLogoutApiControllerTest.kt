package fi.hsl.jore4.auth.apipublic.v1

import com.nimbusds.oauth2.sdk.token.AccessToken
import com.nimbusds.oauth2.sdk.token.RefreshToken
import fi.hsl.jore4.auth.Constants
import fi.hsl.jore4.auth.IntegrationTestContext
import fi.hsl.jore4.auth.Matchers.matchesUriComponents
import fi.hsl.jore4.auth.MockOIDCProvider
import fi.hsl.jore4.auth.TestTags
import fi.hsl.jore4.auth.oidc.SessionKeys
import fi.hsl.jore4.auth.oidc.UserTokenSet
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.lang.IllegalStateException

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [IntegrationTestContext::class])
@AutoConfigureMockMvc
@Tag(TestTags.INTEGRATION_TEST)
class OIDCLogoutApiControllerTest(
    @Autowired private val mockMvc: MockMvc
) {
    private val requestBuilder = OIDCApiRequestBuilder(mockMvc, Constants.API_PATH_PREFIX)

    @BeforeEach
    fun setup() {
        MockOIDCProvider.reset()
    }

    @Test
    @DisplayName("Should redirect the user to the OIDC end session endpoint")
    fun shouldRedirectUserToOIDCEndSessionEndpoint() {
        requestBuilder.logout()
            .andExpect(status().is3xxRedirection)
            .andExpect(header().string(
                "Location",
                matchesUriComponents(
                    `is`(Constants.OIDC_PROVIDER_SCHEME),
                    `is`(Constants.OIDC_PROVIDER_HOST),
                    `is`(Constants.OIDC_PROVIDER_PORT),
                    `is`(Constants.OIDC_PROVIDER_END_SESSION_ENDPOINT_PATH),
                    Pair("post_logout_redirect_uri", `is`(Constants.LOGOUT_PAGE_URL))
                )
            ))
    }

    @Test
    @DisplayName("Should invalidate user session")
    fun shouldInvalidateUserSession() {
        val session = MockHttpSession()

        session.setAttribute(SessionKeys.USER_TOKEN_SET_KEY,
            UserTokenSet(mock(AccessToken::class.java), mock(RefreshToken::class.java)))
        session.setAttribute("random_session_data", "something")

        requestBuilder.logout(session)

        assertTrue(session.isInvalid)
        assertThrows<IllegalStateException> { session.getAttribute(SessionKeys.USER_TOKEN_SET_KEY) }
        assertThrows<IllegalStateException> { session.getAttribute("random_session_data") }
    }
}
