package fi.hsl.jore4.auth.web

data class GeneralErrorResponse(
        val errorCode: String,
        val message: String?
)