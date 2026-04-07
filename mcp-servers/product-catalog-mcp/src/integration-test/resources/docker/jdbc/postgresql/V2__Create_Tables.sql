
CREATE SCHEMA IF NOT EXISTS camelbee_user;

-- Set search path to use camelbee_user schema
SET search_path TO camelbee_user;

CREATE SEQUENCE camelbee_audit_log_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE camelbee_audit_log_table (
  id BIGINT DEFAULT nextval('camelbee_audit_log_seq') PRIMARY KEY,
  user_id VARCHAR(255),
  tool_name VARCHAR(255) NOT NULL,
  parameters TEXT,
  timestamp_utc TIMESTAMP WITH TIME ZONE NOT NULL,
  response_status VARCHAR(20) NOT NULL,
  CONSTRAINT chk_response_status CHECK (response_status IN ('SUCCESS', 'FAILURE'))
);
