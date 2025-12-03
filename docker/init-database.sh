#!/usr/bin/env bash
set -eu

psql --username "$POSTGRES_USER" -v ON_ERROR_STOP=1 <<-EOF
    CREATE DATABASE $SESSION_DB;
    \c $SESSION_DB
    CREATE SCHEMA $SESSION_SCHEMA;
    CREATE USER $SESSION_USERNAME PASSWORD '$SESSION_PASSWORD';
    GRANT ALL PRIVILEGES ON SCHEMA $SESSION_SCHEMA TO $SESSION_USERNAME;

    -- Create login_audit table for tracking login events
    CREATE TABLE $SESSION_SCHEMA.login_audit (
        id BIGSERIAL PRIMARY KEY,
        user_id VARCHAR(255) NOT NULL,
        user_name VARCHAR(255),
        login_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
    
    CREATE INDEX idx_login_audit_user_id ON $SESSION_SCHEMA.login_audit(user_id);
    CREATE INDEX idx_login_audit_timestamp ON $SESSION_SCHEMA.login_audit(login_timestamp);
EOF
