package fi.hsl.jore4.auth.common

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

object ApiUtil {
    fun createRedirect(uri: URI) =
        ResponseEntity<Void>(HttpHeaders().apply {
            location = uri
        }, HttpStatus.FOUND)
}
