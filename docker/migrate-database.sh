#!/bin/bash
set -e

echo "Running database migrations..."

# Wait for database to be ready
until PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOSTNAME" -U "$DB_USERNAME" -d "$DB_NAME" -c '\q' 2>/dev/null; do
  echo "Waiting for database to be ready..."
  sleep 2
done

echo "Database is ready, applying migrations..."

# Run migration SQL
PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOSTNAME" -U "$DB_USERNAME" -d "$DB_NAME" <<'SQL_EOF'
-- Create login_audit table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'login_audit') THEN
        CREATE TABLE public.login_audit (
            id BIGSERIAL PRIMARY KEY,
            user_id VARCHAR(255) NOT NULL,
            user_name VARCHAR(255),
            login_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
        );
        
        -- Grant permissions
        GRANT SELECT, INSERT ON public.login_audit TO dbauth;
        GRANT USAGE, SELECT ON SEQUENCE public.login_audit_id_seq TO dbauth;
        
        RAISE NOTICE 'Created login_audit table successfully';
    ELSE
        RAISE NOTICE 'login_audit table already exists, skipping creation';
    END IF;
END $$;
SQL_EOF

echo "Database migrations completed successfully"
