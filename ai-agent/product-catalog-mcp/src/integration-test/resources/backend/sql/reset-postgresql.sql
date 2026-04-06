SET search_path TO camelbee_user;

-- Truncate audit log table
BEGIN;
TRUNCATE TABLE camelbee_audit_log CASCADE;
COMMIT;
