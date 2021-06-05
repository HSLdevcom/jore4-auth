package fi.hsl.jore4.auth.apipublic.v1

import fi.hsl.jore4.auth.Constants
import fi.hsl.jore4.auth.IntegrationTestContext
import fi.hsl.jore4.auth.Matchers.matchesUriComponents
import fi.hsl.jore4.auth.MockOIDCProvider
import fi.hsl.jore4.auth.TestTags
import fi.hsl.jore4.auth.oidc.OIDCLoginService
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.emptyOrNullString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [IntegrationTestContext::class])
@AutoConfigureMockMvc
@Tag(TestTags.INTEGRATION_TEST)
class OIDCLoginApiControllerTest(
    @Autowired private val mockMvc: MockMvc
) {
    private val requestBuilder = OIDCApiRequestBuilder(mockMvc, Constants.API_PATH_PREFIX)

    @BeforeEach
    fun setup() {
        MockOIDCProvider.reset()
    }

    @Test
    @DisplayName("Should redirect the user to the OIDC authentication endpoint")
    fun shouldRedirectUserToOIDCAuthEndpoint() {
        requestBuilder.login()
            .andExpect(status().is3xxRedirection)
            .andExpect(header().string(
                "Location",
                matchesUriComponents(
                    `is`(Constants.OIDC_PROVIDER_SCHEME),
                    `is`(Constants.OIDC_PROVIDER_HOST),
                    `is`(Constants.OIDC_PROVIDER_PORT),
                    `is`(Constants.OIDC_PROVIDER_AUTHORIZATION_ENDPOINT_PATH),
                    Pair("response_type", `is`(OIDCLoginService.OIDC_RESPONSE_TYPE)),
                    Pair("redirect_uri", `is`(Constants.CODE_EXCHANGE_CALLBACK_URI)),
                    Pair("state", `is`(not(emptyOrNullString()))),
                    Pair("client_id", `is`(Constants.OIDC_CLIENT_ID)),
                    Pair("scope", `is`(OIDCLoginService.OIDC_SCOPES.joinToString(" ")))
                )
            ))
            .andDo(MockMvcResultHandlers.print())
    }
}
