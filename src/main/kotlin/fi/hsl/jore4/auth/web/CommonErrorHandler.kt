package fi.hsl.jore4.auth.web

import io.jsonwebtoken.ExpiredJwtException
import jakarta.validation.ConstraintViolationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import javax.naming.AuthenticationException

/**
 * Provides error handler methods that return custom error messages back to client.
 */
@ControllerAdvice
open class CommonErrorHandler {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(CommonErrorHandler::class.java)
    }

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun processNotFoundException(ex: NotFoundException) {
        LOGGER.error("Processing a NotFoundException", ex)
    }

    @ExceptionHandler(ForbiddenAccessException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun processForbiddenAccessException(ex: ForbiddenAccessException) {
        LOGGER.error("Forbidden access exception", ex)
    }

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun processUnauthorizedException(ex: UnauthorizedException) {
        LOGGER.error("Unauthorized exception", ex)
    }

    @ExceptionHandler(ExpiredJwtException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun processUnauthorizedException(ex: ExpiredJwtException) {
        LOGGER.error("Expired JWT exception", ex)
    }

    @ExceptionHandler(AuthenticationException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun processAuthenticationException(ex: AuthenticationException) {
        LOGGER.error("Authentication exception", ex)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun processConstraintViolationException(ex: ConstraintViolationException) {
        // this could potentially be output by openapi-generator in the future
        LOGGER.error("Constraint violation", ex)
    }
}
