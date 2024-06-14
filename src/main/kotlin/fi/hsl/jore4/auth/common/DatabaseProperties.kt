package fi.hsl.jore4.auth.common

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.util.Assert

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

    fun assertAllGood() {
        Assert.hasText(hostname, "Expected app property db.hostname to be defined!")
        Assert.hasText(port, "Expected app property db.port to be defined!")
        Assert.hasText(name, "Expected app property db.name to be defined!")
        Assert.hasText(username, "Expected app property db.username to be defined!")
        Assert.hasText(password, "Expected app property db.hostname to be defined!")
        Assert.hasText(sessionSchema, "Expected app property db.sessionSchema to be defined!")
    }
}
