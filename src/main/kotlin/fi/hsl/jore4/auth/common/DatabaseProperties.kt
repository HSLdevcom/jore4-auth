package fi.hsl.jore4.auth.common

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Grouping of database-related spring properties.
 */
@Configuration
@ConfigurationProperties("db")
open class DatabaseProperties {
    lateinit var hostname: String
    lateinit var port: String
    lateinit var name: String
    lateinit var username: String
    lateinit var password: String
    lateinit var sessionSchema: String
}
