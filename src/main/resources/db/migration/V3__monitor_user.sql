DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'monitor') THEN
        CREATE ROLE monitor WITH LOGIN PASSWORD 'monitor';
    END IF;
END
$$;

GRANT pg_monitor TO monitor;
GRANT CONNECT ON DATABASE tasktracker TO monitor;
GRANT USAGE ON SCHEMA public TO monitor;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO monitor;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO monitor;
