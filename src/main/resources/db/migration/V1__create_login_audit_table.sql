-- Create login_audit table for tracking login events
CREATE TABLE IF NOT EXISTS login_audit (
    id BIGSERIAL PRIMARY KEY,
    user_id TEXT NOT NULL,
    user_name TEXT,
    login_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_login_audit_user_id ON login_audit(user_id);
CREATE INDEX IF NOT EXISTS idx_login_audit_timestamp ON login_audit(login_timestamp);

-- Grant permissions to the database user
DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_roles WHERE rolname = 'dbauth') THEN
        GRANT SELECT, INSERT ON login_audit TO dbauth;
        GRANT USAGE, SELECT ON SEQUENCE login_audit_id_seq TO dbauth;
    END IF;
    IF EXISTS (SELECT FROM pg_roles WHERE rolname = '${dbHasuraUsername}') THEN
        GRANT SELECT ON login_audit TO ${dbHasuraUsername};
        GRANT USAGE, SELECT ON SEQUENCE login_audit_id_seq TO ${dbHasuraUsername};
    END IF;
END $$;
