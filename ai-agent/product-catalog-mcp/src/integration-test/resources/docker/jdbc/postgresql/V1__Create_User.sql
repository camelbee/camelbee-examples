-- V1__Create_User.sql
-- First migration - Creates the database user if it doesn't exist

-- Check if user exists and create if needed
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'camelbee_user') THEN
    CREATE USER camelbee_user WITH PASSWORD 'secret';
  ELSE
    -- Reset password if user already exists
    ALTER USER camelbee_user WITH PASSWORD 'secret';
  END IF;
END
$$;

-- Grant privileges on the database
GRANT ALL PRIVILEGES ON DATABASE "CAMELBEE_DATABASE" TO camelbee_user;

-- Create schema for user
CREATE SCHEMA IF NOT EXISTS camelbee_user AUTHORIZATION camelbee_user;

-- Additional user privileges
ALTER USER camelbee_user WITH CREATEDB;
ALTER USER camelbee_user WITH CREATEROLE;

-- Verification
SELECT 'User camelbee_user configured successfully' AS result;