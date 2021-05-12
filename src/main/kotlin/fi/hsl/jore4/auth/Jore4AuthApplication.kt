package fi.hsl.jore4.auth

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

/**
 * Spring boot application definition.
 *
 * Disable data source initialization, since we don't use a database at this stage. Similarly,
 * we don't use spring security, therefore exclude it from initialization.
 */
@SpringBootApplication(exclude = [
    DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class,
    SecurityAutoConfiguration::class
])
open class Jore4AuthApplication

fun main(args: Array<String>) {
    SpringApplication.run(Jore4AuthApplication::class.java, *args)
}
