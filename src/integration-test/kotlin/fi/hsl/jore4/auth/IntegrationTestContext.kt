package fi.hsl.jore4.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener

/**
 * Integration context definitions.
 *
 * We don't use spring security, therefore exclude it from initialization.
 *
 * Neither do we use persistent sessions in the integration tests, therefore exclude
 * the data source initialization.
 */
@Configuration
@SpringBootApplication(exclude = [
    SecurityAutoConfiguration::class,
    DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class
])
open class IntegrationTestContext {

    @EventListener(ContextRefreshedEvent::class)
    @Primary
    // @Primary is needed to have this listener called before the OIDCProviderMetadataSupplier listener
    fun prepareTestRun() {
        // have the mock OIDC provider created by referencing the object
        MockOIDCProvider.touch()
    }
}
