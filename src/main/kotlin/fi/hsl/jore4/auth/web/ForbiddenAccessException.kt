package fi.hsl.jore4.auth.web

/**
 * Corresponds to http status 403 Forbidden.
 */
class ForbiddenAccessException(message: String) : RuntimeException(message)
