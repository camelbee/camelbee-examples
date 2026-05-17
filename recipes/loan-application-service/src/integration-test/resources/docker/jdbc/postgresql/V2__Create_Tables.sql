-- V2__Create_Tables.sql — Loan Application table for the loan-application-service domain.

SET search_path TO camelbee_user;

CREATE SEQUENCE loan_applications_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE loan_applications (
  id                BIGINT       DEFAULT nextval('loan_applications_seq') PRIMARY KEY,
  application_id    VARCHAR(36)  NOT NULL UNIQUE,
  applicant_id      VARCHAR(100),
  applicant_name    VARCHAR(200),
  applicant_email   VARCHAR(200),
  requested_amount  DECIMAL(15,2),
  purpose           VARCHAR(50),
  term_months       INT,
  monthly_income    DECIMAL(15,2),
  credit_score      INT,
  employment_status VARCHAR(50),
  status            VARCHAR(20),
  risk_score        INT,
  decision_reason   VARCHAR(500),
  submitted_at      TIMESTAMP,
  processed_at      TIMESTAMP,
  CONSTRAINT chk_status_loan
    CHECK (status IN ('RECEIVED', 'APPROVED', 'REJECTED', 'PENDING_REVIEW')),
  CONSTRAINT chk_purpose_loan
    CHECK (purpose IS NULL OR purpose IN ('HOME_PURCHASE', 'CAR_LOAN', 'PERSONAL', 'BUSINESS', 'EDUCATION', 'DEBT_CONSOLIDATION')),
  CONSTRAINT chk_employment_loan
    CHECK (employment_status IS NULL OR employment_status IN ('EMPLOYED', 'SELF_EMPLOYED', 'UNEMPLOYED', 'RETIRED'))
);

CREATE INDEX idx_loan_applications_status ON loan_applications(status);
CREATE INDEX idx_loan_applications_applicant_id ON loan_applications(applicant_id);
