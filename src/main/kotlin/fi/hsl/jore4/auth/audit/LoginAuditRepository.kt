package fi.hsl.jore4.auth.audit

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository for login records.
 */
@Repository
interface LoginAuditRepository : JpaRepository<LoginAudit, Long>
