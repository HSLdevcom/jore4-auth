package fi.hsl.jore4.auth.web

import fi.hsl.jore4.auth.authentication.OIDCProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class FrontendController(
        @Autowired private val oidcProperties: OIDCProperties
) {
    @RequestMapping("/")
    fun redirectToOIDCFrontpage(): String {
        return "redirect:${oidcProperties.frontPage}"
    }

    // Match all frontend paths, i.e. all paths that do NOT start with the specified prefixes
    @RequestMapping("/{segment:^(?!api|api-specs|swagger-ui|static|i18n)[^\\.]+}", "/{segment:^(?!api|api-specs|swagger-ui|static|i18n)[^\\.]+}/**")
    fun forwardToFrontend(): String {
        return "forward:/index.html"
    }
}
