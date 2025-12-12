-- Grant permissions to login_audit table using secrets
DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_roles WHERE rolname = '${dbUsername}') THEN
        GRANT SELECT, INSERT ON login_audit TO ${dbUsername};
        GRANT USAGE, SELECT ON SEQUENCE login_audit_id_seq TO ${dbUsername};
    END IF;
    IF EXISTS (SELECT FROM pg_roles WHERE rolname = '${dbHasuraUsername}') THEN
        GRANT SELECT ON login_audit TO ${dbHasuraUsername};
        GRANT USAGE, SELECT ON SEQUENCE login_audit_id_seq TO ${dbHasuraUsername};
    END IF;
END $$;
