package fi.hsl.jore4.auth.common

/**
 * This exception is thrown if the requested information is
 * not found from the database.
 */
class NotFoundException(message: String) : RuntimeException(message)