package fi.hsl.jore4.auth.apipublic.v1

import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

class OIDCApiRequestBuilder(
    private val mockMvc: MockMvc,
    private val apiPathPrefix: String
) {
    fun login(): ResultActions =
        mockMvc.perform(MockMvcRequestBuilders.get("$apiPathPrefix/public/v1/login"))
}
