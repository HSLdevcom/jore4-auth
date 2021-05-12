package fi.hsl.jore4.auth.security;

import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.WebUtils

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException

/**
 * Filter to generate a CSRF cookie for the CSRF token generated by the CsrfFilter.
 */
internal class CsrfCookieGeneratorFilter : OncePerRequestFilter() {

    companion object {
        private val XSRF_COOKIE_NAME = "XSRF-TOKEN"
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        val csrfToken = request.getAttribute(CsrfToken::class.java.name)
        if (csrfToken is CsrfToken) {
            var cookie = WebUtils.getCookie(request, XSRF_COOKIE_NAME)
            val token = csrfToken.token

            if (cookie == null || token != null && token != cookie.value) {
                cookie = Cookie(XSRF_COOKIE_NAME, token)
                cookie.path = "/"
                response.addCookie(cookie)
            }
        }

        filterChain.doFilter(request, response)
    }
}
