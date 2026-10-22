-- ============================================================================
-- JECRC University Placement Portal - V8 Migration
-- Module: Offers, Internships & PPO Tracking
-- ============================================================================

-- 1. INSTITUTIONAL OFFER POLICIES
CREATE TABLE institutional_offer_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_code VARCHAR(50) NOT NULL UNIQUE,
    policy_name VARCHAR(150) NOT NULL,
    description TEXT,
    allow_multiple_offers BOOLEAN NOT NULL DEFAULT FALSE,
    allow_multiple_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    max_active_offers_per_student INT NOT NULL DEFAULT 1,
    dream_tier_multiplier_threshold NUMERIC(5, 2) DEFAULT 1.50,
    super_dream_ctc_threshold_lpa NUMERIC(10, 2) DEFAULT 18.00,
    auto_withdraw_other_applications BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed default institutional offer policy (demo configuration)
INSERT INTO institutional_offer_policies (
    policy_code, policy_name, description, allow_multiple_offers, allow_multiple_accepted,
    max_active_offers_per_student, dream_tier_multiplier_threshold, super_dream_ctc_threshold_lpa,
    auto_withdraw_other_applications, is_active
) VALUES (
    'JECRC_DEFAULT_OFFER_POLICY_2026',
    'JECRC University Standard Placement Offer Rules (Configurable Baseline)',
    'Permits single offer acceptance with dream tier upgrades for CTC >= 1.5x previous offer or >= 18 LPA',
    TRUE,
    FALSE,
    1,
    1.50,
    18.00,
    TRUE,
    TRUE
);

-- 2. PLACEMENT OFFERS
CREATE TABLE placement_offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    offer_reference_number VARCHAR(100) NOT NULL UNIQUE,
    student_id UUID NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    application_id UUID REFERENCES job_applications(application_id) ON DELETE SET NULL,
    company_id UUID NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
    job_profile_id UUID REFERENCES job_profiles(job_profile_id) ON DELETE SET NULL,
    drive_id UUID REFERENCES placement_drives(drive_id) ON DELETE SET NULL,
    applied_policy_id UUID REFERENCES institutional_offer_policies(id),
    designation VARCHAR(150) NOT NULL,
    employment_type VARCHAR(50) NOT NULL DEFAULT 'FULL_TIME',
    total_ctc_lpa NUMERIC(10, 2) NOT NULL,
    fixed_salary_lpa NUMERIC(10, 2),
    variable_salary_lpa NUMERIC(10, 2),
    joining_bonus NUMERIC(10, 2),
    retention_bonus NUMERIC(10, 2),
    monthly_stipend_inr NUMERIC(10, 2),
    job_location VARCHAR(200),
    terms_and_conditions TEXT,
    offer_letter_doc_ref VARCHAR(300),
    offer_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    issuance_date TIMESTAMPTZ,
    decision_deadline TIMESTAMPTZ,
    student_decision VARCHAR(50),
    student_decision_at TIMESTAMPTZ,
    student_decision_remarks TEXT,
    revocation_reason TEXT,
    revoked_at TIMESTAMPTZ,
    revoked_by_user_id UUID REFERENCES users(user_id),
    created_by_user_id UUID REFERENCES users(user_id),
    issued_by_user_id UUID REFERENCES users(user_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_offer_status CHECK (offer_status IN ('DRAFT', 'ISSUED', 'ACCEPTED', 'DECLINED', 'EXPIRED', 'REVOKED'))
);

CREATE INDEX idx_offers_student ON placement_offers(student_id);
CREATE INDEX idx_offers_company ON placement_offers(company_id);
CREATE INDEX idx_offers_status ON placement_offers(offer_status);

-- 3. OFFER DECISION HISTORY (Immutable append-only audit trail)
CREATE TABLE offer_decision_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    offer_id UUID NOT NULL REFERENCES placement_offers(id) ON DELETE CASCADE,
    previous_status VARCHAR(50) NOT NULL,
    new_status VARCHAR(50) NOT NULL,
    actor_user_id UUID REFERENCES users(user_id),
    actor_email VARCHAR(200),
    actor_role VARCHAR(50),
    decision_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_offer_history_offer_id ON offer_decision_history(offer_id);

-- 4. INTERNSHIP RECORDS
CREATE TABLE internship_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    company_id UUID NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
    job_profile_id UUID REFERENCES job_profiles(job_profile_id) ON DELETE SET NULL,
    application_id UUID REFERENCES job_applications(application_id) ON DELETE SET NULL,
    placement_offer_id UUID REFERENCES placement_offers(id) ON DELETE SET NULL,
    internship_title VARCHAR(150) NOT NULL,
    internship_type VARCHAR(50) NOT NULL DEFAULT 'SUMMER',
    start_date DATE NOT NULL,
    expected_end_date DATE NOT NULL,
    actual_end_date DATE,
    monthly_stipend_inr NUMERIC(10, 2),
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    work_arrangement VARCHAR(50) NOT NULL DEFAULT 'ON_SITE',
    location VARCHAR(200),
    appointment_doc_ref VARCHAR(300),
    faculty_mentor_id UUID REFERENCES users(user_id),
    industry_mentor_name VARCHAR(150),
    industry_mentor_email VARCHAR(200),
    industry_mentor_phone VARCHAR(50),
    internship_status VARCHAR(50) NOT NULL DEFAULT 'OFFERED',
    has_ppo_potential BOOLEAN NOT NULL DEFAULT FALSE,
    completion_remarks TEXT,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_internship_status CHECK (internship_status IN ('OFFERED', 'ACCEPTED', 'ACTIVE', 'COMPLETED', 'TERMINATED', 'WITHDRAWN', 'DECLINED'))
);

CREATE INDEX idx_internships_student ON internship_records(student_id);
CREATE INDEX idx_internships_company ON internship_records(company_id);
CREATE INDEX idx_internships_faculty_mentor ON internship_records(faculty_mentor_id);
CREATE INDEX idx_internships_status ON internship_records(internship_status);

-- 5. INTERNSHIP PROGRESS LOGS
CREATE TABLE internship_progress_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    internship_id UUID NOT NULL REFERENCES internship_records(id) ON DELETE CASCADE,
    log_number INT NOT NULL,
    reporting_period_start DATE NOT NULL,
    reporting_period_end DATE NOT NULL,
    summary_of_work TEXT NOT NULL,
    learning_outcomes TEXT,
    challenges_encountered TEXT,
    evidence_attachment_ref VARCHAR(300),
    submission_status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
    mentor_feedback TEXT,
    mentor_reviewed_at TIMESTAMPTZ,
    mentor_reviewer_id UUID REFERENCES users(user_id),
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_log_submission_status CHECK (submission_status IN ('SUBMITTED', 'REVIEWED', 'CORRECTION_REQUESTED'))
);

CREATE INDEX idx_internship_logs_internship_id ON internship_progress_logs(internship_id);

-- 6. INTERNSHIP EVALUATIONS
CREATE TABLE internship_evaluations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    internship_id UUID NOT NULL REFERENCES internship_records(id) ON DELETE CASCADE,
    evaluator_user_id UUID REFERENCES users(user_id),
    evaluation_type VARCHAR(50) NOT NULL DEFAULT 'MID_TERM',
    technical_score NUMERIC(5, 2),
    communication_score NUMERIC(5, 2),
    professionalism_score NUMERIC(5, 2),
    overall_score NUMERIC(5, 2),
    written_feedback TEXT,
    confidential_staff_notes TEXT,
    recommend_for_ppo BOOLEAN NOT NULL DEFAULT FALSE,
    evaluated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_eval_type CHECK (evaluation_type IN ('MID_TERM', 'FINAL', 'AD_HOC'))
);

CREATE INDEX idx_internship_eval_internship_id ON internship_evaluations(internship_id);

-- 7. PPO RECORDS (Pre-Placement Offer Decisions)
CREATE TABLE ppo_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    internship_id UUID NOT NULL REFERENCES internship_records(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    company_id UUID NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
    target_job_profile_id UUID REFERENCES job_profiles(job_profile_id) ON DELETE SET NULL,
    ppo_status VARCHAR(50) NOT NULL DEFAULT 'UNDER_REVIEW',
    minimum_evaluation_score_required NUMERIC(5, 2),
    recommendation_notes TEXT,
    decision_remarks TEXT,
    recommended_by_user_id UUID REFERENCES users(user_id),
    decided_by_user_id UUID REFERENCES users(user_id),
    decided_at TIMESTAMPTZ,
    resulting_offer_id UUID REFERENCES placement_offers(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_ppo_status CHECK (ppo_status IN ('NOT_ELIGIBLE', 'UNDER_REVIEW', 'RECOMMENDED', 'APPROVED', 'DECLINED', 'OFFER_ISSUED'))
);

CREATE INDEX idx_ppo_internship ON ppo_records(internship_id);
CREATE INDEX idx_ppo_student ON ppo_records(student_id);

-- 8. GRANULAR PERMISSIONS FOR MILESTONE 5
INSERT INTO permissions (permission_name, description, module) VALUES
    ('OFFER_VIEW', 'View job offers issued to candidates', 'OFFER'),
    ('OFFER_CREATE', 'Draft job offer for selected candidate', 'OFFER'),
    ('OFFER_ISSUE', 'Officially issue formal placement offer with deadline', 'OFFER'),
    ('OFFER_REVOKE', 'Revoke or cancel a placement offer', 'OFFER'),
    ('OFFER_DECIDE', 'Accept or decline an offered placement opportunity', 'OFFER'),
    ('OFFER_POLICY_MANAGE', 'Configure institutional multiple-offer and CTC tier rules', 'OFFER'),
    ('INTERNSHIP_MANAGE', 'Manage internship records, mentor assignments, and completions', 'INTERNSHIP'),
    ('INTERNSHIP_LOG_SUBMIT', 'Submit periodic internship progress log entries', 'INTERNSHIP'),
    ('INTERNSHIP_LOG_REVIEW', 'Review student internship logs and provide mentor feedback', 'INTERNSHIP'),
    ('INTERNSHIP_EVALUATE', 'Conduct midterm or final evaluations for interns', 'INTERNSHIP'),
    ('PPO_REVIEW', 'Track and review internship PPO conversion candidates', 'PPO'),
    ('PPO_APPROVE', 'Approve PPO conversion and authorize formal placement offer generation', 'PPO')
ON CONFLICT (permission_name) DO NOTHING;

-- Map permissions to roles
-- SUPER_ADMIN & TPO_ADMIN: Full authority
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r, permissions p
WHERE r.role_name IN ('SUPER_ADMIN', 'TPO_ADMIN')
  AND p.permission_name IN (
    'OFFER_VIEW', 'OFFER_CREATE', 'OFFER_ISSUE', 'OFFER_REVOKE', 'OFFER_POLICY_MANAGE',
    'INTERNSHIP_MANAGE', 'INTERNSHIP_LOG_REVIEW', 'INTERNSHIP_EVALUATE',
    'PPO_REVIEW', 'PPO_APPROVE'
  )
ON CONFLICT DO NOTHING;

-- TPO_COORDINATOR: Operational offer, internship & PPO review
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r, permissions p
WHERE r.role_name = 'TPO_COORDINATOR'
  AND p.permission_name IN ('OFFER_VIEW', 'OFFER_CREATE', 'INTERNSHIP_MANAGE', 'INTERNSHIP_LOG_REVIEW', 'PPO_REVIEW')
ON CONFLICT DO NOTHING;

-- FACULTY: Internship logs review & evaluation
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r, permissions p
WHERE r.role_name = 'FACULTY'
  AND p.permission_name IN ('INTERNSHIP_LOG_REVIEW', 'INTERNSHIP_EVALUATE', 'PPO_REVIEW')
ON CONFLICT DO NOTHING;

-- STUDENT: Own offers view & decision, and internship log submissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r, permissions p
WHERE r.role_name = 'STUDENT'
  AND p.permission_name IN ('OFFER_VIEW', 'OFFER_DECIDE', 'INTERNSHIP_LOG_SUBMIT')
ON CONFLICT DO NOTHING;
