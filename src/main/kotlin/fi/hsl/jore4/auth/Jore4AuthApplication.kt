package fi.hsl.jore4.auth

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

/**
 * Spring boot application definition.
 *
 * We don't use spring security, therefore exclude it from initialization.
 */
@SpringBootApplication(exclude = [
    SecurityAutoConfiguration::class
])
open class Jore4AuthApplication

fun main(args: Array<String>) {
    SpringApplication.run(Jore4AuthApplication::class.java, *args)
}
