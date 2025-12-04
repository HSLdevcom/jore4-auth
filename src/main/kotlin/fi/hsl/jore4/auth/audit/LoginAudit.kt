package fi.hsl.jore4.auth.audit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * Entity representing a login record.
 */
@Entity
@Table(name = "login_audit")
data class LoginAudit(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "user_id", nullable = false)
    val userId: String,
    @Column(name = "user_name")
    val userName: String?,
    @Column(name = "login_timestamp", nullable = false)
    val loginTimestamp: Instant = Instant.now()
)
