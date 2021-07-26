package fi.hsl.jore4.auth.apipublic.v1

import fi.hsl.jore4.auth.SessionRequestBuilder
import fi.hsl.jore4.auth.hasura.HasuraAuthService
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions

class HasuraApiRequestBuilder(
    private val mockMvc: MockMvc,
    apiPathPrefix: String
) : SessionRequestBuilder(apiPathPrefix) {

    fun webhook(session: MockHttpSession? = null, requestedRole: String? = null): ResultActions {
        var requestBuilder = getWithSession("/public/v1/hasura/webhook", session)

        if (requestedRole != null) {
            requestBuilder = requestBuilder.header(HasuraAuthService.REQUESTED_ROLE_HEADER, requestedRole)
        }

        return mockMvc.perform(requestBuilder)
    }
}
