package fi.hsl.jore4.auth.audit

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for logging login events.
 */
@Service
@ConditionalOnProperty(prefix = "session", name = ["enabled"], havingValue = "true")
open class LoginAuditService(
    @Autowired(required = true)
    private val loginAuditRepository: LoginAuditRepository
) {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(LoginAuditService::class.java)
    }

    /**
     * Record a login event for the specified user.
     */
    @Transactional
    open fun recordLogin(
        userId: String,
        userName: String?
    ) {
        try {
            val auditRecord =
                LoginAudit(
                    userId = userId,
                    userName = userName
                )

            loginAuditRepository.save(auditRecord)

            LOGGER.info("Recorded login for user: userId={}, userName={}", userId, userName)
        } catch (e: Exception) {
            // Log the error but don't fail the login process
            LOGGER.error("Failed to record login audit for user: userId={}, userName={}", userId, userName, e)
        }
    }
}
