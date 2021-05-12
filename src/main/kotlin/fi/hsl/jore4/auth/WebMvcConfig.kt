package fi.hsl.jore4.auth

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
open class WebMvcConfig : WebMvcConfigurer {

    private val SWAGGER_RESOURCE_LOCATION = "classpath:/public/"

    override fun addResourceHandlers(registry: ResourceHandlerRegistry?) {
        if (registry == null) {
            return
        }

        registry.addResourceHandler("/api-specs/**")
            .addResourceLocations("${SWAGGER_RESOURCE_LOCATION}api-specs/")
    }
}
