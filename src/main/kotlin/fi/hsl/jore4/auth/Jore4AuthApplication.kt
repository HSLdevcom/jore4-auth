package fi.hsl.jore4.auth

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Spring boot application definition.
 */
@SpringBootApplication
open class Jore4AuthApplication

fun main(args: Array<String>) {
    SpringApplication.run(Jore4AuthApplication::class.java, *args)
}
