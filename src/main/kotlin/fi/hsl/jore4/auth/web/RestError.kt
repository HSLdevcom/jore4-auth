package fi.hsl.jore4.auth.web

/**
 * Contains a list of validation errors.
 */
class RestError(val validationErrors: List<RestValidationError>)

/**
 * Contains a validation error that identifies the problem
 * found from a specific field of a JSON object.
 */
class RestValidationError(val field: String, val errorCode: String)