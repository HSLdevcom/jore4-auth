package fi.hsl.jore4.auth

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC config to allow downloading the Swagger API specs.
 */
@Configuration
open class WebMvcConfig : WebMvcConfigurer {

    private val SWAGGER_RESOURCE_LOCATION = "classpath:/public/"

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/api-specs/**")
            .addResourceLocations("${SWAGGER_RESOURCE_LOCATION}api-specs/")
    }
}
