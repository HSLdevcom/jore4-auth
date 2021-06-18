package fi.hsl.jore4.auth

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener

@Configuration
@Import(Jore4AuthApplication::class)
open class IntegrationTestContext {

    @EventListener(ContextRefreshedEvent::class)
    @Primary
    // @Primary is needed to have this listener called before the OIDCProviderMetadataSupplier listener
    fun prepareTestRun() {
        // have the mock OIDC provider created by referencing the object
        MockOIDCProvider.touch()
    }
}
