package fi.hsl.jore4.auth

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unitTest")
class FlywayAutoConfigurationPresentTest {

    @Test
    fun `flyway auto-configuration class is on the classpath`() {
        val clazz = Class.forName("org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration")
        assertNotNull(clazz, "FlywayAutoConfiguration must be on the classpath via spring-boot-starter-flyway")
    }
}
