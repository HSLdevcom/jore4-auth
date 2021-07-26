package fi.hsl.jore4.auth

import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

open class SessionRequestBuilder(
    private val apiPathPrefix: String
) {
    protected fun getWithSession(path: String, session: MockHttpSession? = null): MockHttpServletRequestBuilder {
        val requestBuilder = MockMvcRequestBuilders.get("$apiPathPrefix$path")

        return session?.let { requestBuilder.session(it) } ?: requestBuilder
    }
}
