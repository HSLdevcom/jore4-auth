package fi.hsl.jore4.auth

import fi.hsl.jore4.auth.common.DatabaseProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

/**
 * Spring boot application definition.
 */
@SpringBootApplication
open class Jore4AuthApplication(
    @Autowired private val databaseProperties: DatabaseProperties
) {
    @Bean
    @ConditionalOnProperty(prefix = "session", name = ["enabled"])
    open fun getDataSource(): DataSource {
        databaseProperties.assertAllGood()
        with(databaseProperties) {
            return DataSourceBuilder
                .create()
                .driverClassName("org.postgresql.Driver")
                .url(
                    "jdbc:postgresql://$hostname:$port/$name?currentSchema=$sessionSchema"
                ).username(username)
                .password(password)
                .build()
        }
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Jore4AuthApplication::class.java, *args)
}
