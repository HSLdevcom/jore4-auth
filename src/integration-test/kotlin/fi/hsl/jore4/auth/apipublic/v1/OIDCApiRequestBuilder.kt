package fi.hsl.jore4.auth.apipublic.v1

import fi.hsl.jore4.auth.apipublic.v1.OIDCCodeExchangeApiController.Companion.EXCHANGE_ENDPOINT_PATH_SUFFIX
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

class OIDCApiRequestBuilder(
    private val mockMvc: MockMvc,
    private val apiPathPrefix: String
) {
    fun login(session: MockHttpSession? = null): ResultActions =
        mockMvc.perform(getWithSession("/public/v1/login", session))

    fun logout(session: MockHttpSession? = null): ResultActions =
        mockMvc.perform(getWithSession("/public/v1/logout", session))

    fun exchangeCode(code: String, state: String, session: MockHttpSession? = null): ResultActions =
        mockMvc.perform(
            getWithSession(EXCHANGE_ENDPOINT_PATH_SUFFIX, session)
                .queryParam("code", code)
                .queryParam("state", state)
        )

    private fun getWithSession(path: String, session: MockHttpSession? = null): MockHttpServletRequestBuilder {
        var requestBuilder = MockMvcRequestBuilders.get("$apiPathPrefix$path")

        if (session != null) requestBuilder = requestBuilder.session(session)

        return requestBuilder
    }
}