package fi.hsl.jore4.auth

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLDecoder.decode

object Matchers {
    fun matchesUriComponents(
        scheme: Matcher<String>? = null,
        host: Matcher<String>? = null,
        port: Matcher<Number>? = null,
        path: Matcher<String>? = null,
        vararg params: Pair<String, Matcher<String>>
    ): Matcher<String> {

        return object : BaseMatcher<String>() {
            override fun describeTo(description: Description) {
                if (scheme != null) {
                    description.appendText("\nURI scheme ")
                    scheme.describeTo(description)
                }
                if (host != null) {
                    description.appendText("\nURI host ")
                    host.describeTo(description)
                }
                if (port != null) {
                    description.appendText("\nURI port ")
                    port.describeTo(description)
                }
                if (path != null) {
                    description.appendText("\nURI path ")
                    path.describeTo(description)
                }
                params.forEach { param ->
                    description.appendText("\nURI has query param named \"${param.first}\" which ")
                    param.second.describeTo(description)
                }
            }

            override fun matches(locationString: Any): Boolean {
                val locationUri = UriComponentsBuilder.fromUriString(locationString as String).build()

                return scheme?.matches(locationUri.scheme) ?: true &&
                    host?.matches(locationUri.host) ?: true &&
                    port?.matches(locationUri.port) ?: true &&
                    path?.matches(locationUri.path) ?: true &&
                    params.all { param ->
                        val foundValues = locationUri
                            .queryParams[param.first]
                            ?.map { value -> decode(value, "UTF-8") }

                        when {
                            foundValues == null -> {
                                println("query param \"${param.first}\" not present")
                                false
                            }
                            foundValues.any { foundValue -> param.second.matches(foundValue) } -> true
                            else -> {
                                println("no match found for \"${param.first}\" in")
                                println(foundValues)
                                false
                            }
                        }
                    }
            }
        }
    }
}
