SET search_path TO camelbee_user;

-- Truncate audit log table
BEGIN;

TRUNCATE TABLE camelbee_audit_log_table CASCADE;
ALTER SEQUENCE camelbee_audit_log_seq RESTART WITH 1;

COMMIT;
