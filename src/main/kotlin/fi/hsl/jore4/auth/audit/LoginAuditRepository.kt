package fi.hsl.jore4.auth.audit

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository for login records.
 */
@Repository
@ConditionalOnProperty(prefix = "session", name = ["enabled"], havingValue = "true")
interface LoginAuditRepository : JpaRepository<LoginAudit, Long>
