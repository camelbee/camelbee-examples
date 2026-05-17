-- R__Verify_Ready.sql
-- Repeatable migration to verify the database is ready
-- This will run after all versioned migrations and can be rerun

-- This will output a message that can be detected by the testcontainer configuration
SELECT 'Database is ready' AS status;