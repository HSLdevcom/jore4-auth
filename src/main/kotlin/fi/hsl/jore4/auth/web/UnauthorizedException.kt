package fi.hsl.jore4.auth.web

/**
 * Corresponds to http status 401 unauthorized
 */
class UnauthorizedException(message: String) : RuntimeException(message)
