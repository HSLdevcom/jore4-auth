package fi.hsl.jore4.auth

import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

@SpringBootApplication(exclude = [
    DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class,
    SecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class
])
open class Jore4AuthApplication

fun main(args: Array<String>) {
    SpringApplication.run(Jore4AuthApplication::class.java, *args)
}
