SET search_path TO camelbee_user;

BEGIN;

TRUNCATE TABLE loan_applications;
ALTER SEQUENCE loan_applications_seq RESTART WITH 1;

COMMIT;

-- Seed a small set of fixture rows used by integration/black-box LIST tests.
INSERT INTO loan_applications
  (application_id, applicant_id, applicant_name, applicant_email, requested_amount, purpose,
   term_months, monthly_income, credit_score, employment_status, status, submitted_at)
VALUES
  ('seed-0000-0000-0000-000000000001', 'APP-S01', 'Seed Applicant 1', 'seed1@example.com',
   12500.00, 'PERSONAL', 24, 4500.00, 620, 'EMPLOYED', 'PENDING_REVIEW', '2026-01-10 10:00:00'),
  ('seed-0000-0000-0000-000000000002', 'APP-S02', 'Seed Applicant 2', 'seed2@example.com',
   28000.00, 'CAR_LOAN', 48, 6200.00, 580, 'EMPLOYED', 'PENDING_REVIEW', '2026-01-11 11:00:00'),
  ('seed-0000-0000-0000-000000000003', 'APP-S03', 'Seed Applicant 3', 'seed3@example.com',
   3000.00, 'PERSONAL', 12, 5100.00, 730, 'EMPLOYED', 'APPROVED', '2026-01-12 12:00:00');
