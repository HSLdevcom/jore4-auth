package fi.hsl.jore4.auth.security

import fi.hsl.jore4.auth.Profiles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.csrf.*

@Configuration
@EnableWebSecurity
open class WebSecurityConfig {
    @Bean
    open fun authenticationEntryPoint(): HttpStatusEntryPoint {
        return HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
    }

    @Bean
    open fun roleHierarchy(): RoleHierarchy {
        val roleHierarchy = RoleHierarchyImpl()
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_ORGANIZATION_USER > ROLE_USER")
        return roleHierarchy
    }

    /**
     * Web security config for internal frontend requests. Authentication is session-based.
     */
    @Configuration
    @Profile(Profiles.APPLICATION)
    open inner class DefaultWebSecurityConfig : WebSecurityConfigurerAdapter() {
        @Throws(Exception::class)
        override fun configure(security: HttpSecurity) {
            security
                    .addFilterAfter(CsrfCookieGeneratorFilter(), CsrfFilter::class.java)

                    .csrf()
                    .csrfTokenRepository(LazyCsrfTokenRepository(csrfTokenRepository()))

                    .and()
                    .exceptionHandling()
                    .accessDeniedHandler(CsrfAccessDeniedHandler())

                    .and()
                    .httpBasic().disable()
                    .exceptionHandling()
                    .authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                    .and()
                    .authorizeRequests()
                    .antMatchers(HttpMethod.GET, *permitAllApiPatterns).permitAll()
                    .antMatchers(
                            *roleUserApiPatterns,
                            "/api-specs/**",
                            "/swagger-ui/**"
                    ).hasRole("USER")
        }

        private fun csrfTokenRepository(): CsrfTokenRepository {
            val repository = HttpSessionCsrfTokenRepository()
            repository.setHeaderName(XSRF_HEADER_NAME)
            return repository
        }
    }

    companion object {
        private const val XSRF_HEADER_NAME = "X-XSRF-TOKEN"

        private val permitAllApiPatterns = arrayOf(
                "/api/public/v1.0/login",
                "/api/public/v1.0/logout",
                "/api/public/v1.0/account/active",
                "/api/v1.0/oidc/exchange"
        )

        private val roleUserApiPatterns = arrayOf(
                "/api/**"
        )

        private fun prefixPaths(paths: Array<String>, prefix: String) =
                paths.map { pattern -> "$prefix$pattern" }.toTypedArray()
    }
}
