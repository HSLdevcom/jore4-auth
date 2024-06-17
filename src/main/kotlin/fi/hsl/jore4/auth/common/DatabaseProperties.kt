package fi.hsl.jore4.auth.common

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Grouping of database-related spring properties.
 */
@Configuration
@ConfigurationProperties("db")
open class DatabaseProperties {
    companion object {
        private val placeHolderRegex = Regex("^@.*@$")
    }

    lateinit var hostname: String
    lateinit var port: String
    lateinit var name: String
    lateinit var username: String
    lateinit var password: String
    lateinit var sessionSchema: String

    fun assertAllGood() {
        isGoodProperty("db.hostname", hostname)
        isGoodProperty("db.port", port)
        isGoodProperty("db.name", name)
        isGoodProperty("db.username", username)
        isGoodProperty("db.password", password)
        isGoodProperty("db.sessionSchema", sessionSchema)
    }

    private fun isGoodProperty(
        key: String,
        value: String?
    ) {
        require(!value.isNullOrBlank()) {
            "Expected app property $key to be defined! But value was '$value'"
        }

        require(!placeHolderRegex.matches(value)) {
            "Expected app property $key to be defined! But value was placeholder definition '$value'. Property is likely missing from profile!"
        }
    }
}
