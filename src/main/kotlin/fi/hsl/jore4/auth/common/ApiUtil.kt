package fi.hsl.jore4.auth.common

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

class ApiUtil {
    companion object {
        fun createRedirect(url: URI): ResponseEntity<Void> {
            val headers = HttpHeaders().apply {
                location = url
            }
            return ResponseEntity(headers, HttpStatus.FOUND)
        }
    }
}
