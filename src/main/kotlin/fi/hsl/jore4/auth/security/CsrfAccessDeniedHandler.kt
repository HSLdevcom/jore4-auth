package fi.hsl.jore4.auth.security

import org.springframework.http.HttpStatus
import org.springframework.security.web.access.AccessDeniedHandlerImpl
import org.springframework.security.web.csrf.InvalidCsrfTokenException
import org.springframework.security.web.csrf.MissingCsrfTokenException
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * AccessDeniedHandler which responds to a request with 401 UNAUTHORIZED when the CSRF
 * token is either missing or invalid.
 *
 * In all other cases the handler inherits the default handler's behaviour.
 */
internal class CsrfAccessDeniedHandler : AccessDeniedHandlerImpl() {

    @Throws(IOException::class, ServletException::class)
    override fun handle(request: HttpServletRequest,
                        response: HttpServletResponse,
                        accessDeniedException: org.springframework.security.access.AccessDeniedException) {

        if (accessDeniedException is InvalidCsrfTokenException ||
                accessDeniedException is MissingCsrfTokenException)
        {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.reasonPhrase)
        }
        else {
            super.handle(request, response, accessDeniedException)
        }
    }
}
