package fi.hsl.jore4.auth

import fi.hsl.jore4.auth.common.DatabaseProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import javax.sql.DataSource


/**
 * Spring boot application definition.
 */
@SpringBootApplication
open class Jore4AuthApplication(
    @Value("spring.session.store-type") private val sessionStoreType: String,
    @Autowired private val databaseProperties: DatabaseProperties
) {

    @Bean
    open fun getDataSource(): DataSource? =
        // only create a data source if we are configured to store sessions
        if (sessionStoreType != "none") {
            DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://${databaseProperties.hostname}:${databaseProperties.port}/${databaseProperties.name}?currentSchema=${databaseProperties.sessionSchema}")
                .username(databaseProperties.username)
                .password(databaseProperties.password)
                .build()
        }
        else null
}

fun main(args: Array<String>) {
    SpringApplication.run(Jore4AuthApplication::class.java, *args)
}
