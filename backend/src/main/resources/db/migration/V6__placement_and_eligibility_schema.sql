-- V6__placement_and_eligibility_schema.sql: Placement Drives, Companies, Versioned Policies, Evaluations & Applications

-- 1. Insert Placement and Eligibility Permissions
INSERT INTO permissions (permission_name, module, description) VALUES
('COMPANY_MANAGE', 'PLACEMENT', 'Create, update and archive corporate recruitment partners'),
('COMPANY_VIEW', 'PLACEMENT', 'View recruitment companies and directory profiles'),
('DRIVE_MANAGE', 'PLACEMENT', 'Create, edit, publish, close and manage recruitment drives'),
('DRIVE_VIEW', 'PLACEMENT', 'View active and historical recruitment drives and job profiles'),
('POLICY_MANAGE', 'PLACEMENT', 'Create, version and publish recruitment eligibility policies'),
('ELIGIBILITY_EVALUATE', 'PLACEMENT', 'Execute and view automated eligibility evaluations for students'),
('ELIGIBILITY_OVERRIDE', 'PLACEMENT', 'Approve authorized eligibility exceptions with mandatory justification'),
('APPLICATION_MANAGE', 'PLACEMENT', 'Manage student applications, update review stages and statuses'),
('APPLICATION_SUBMIT', 'PLACEMENT', 'Submit and withdraw placement job profile applications')
ON CONFLICT (permission_name) DO NOTHING;

-- Map Permissions to Roles
-- SUPER_ADMIN gets all placement permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'SUPER_ADMIN'
  AND p.permission_name IN (
    'COMPANY_MANAGE', 'COMPANY_VIEW', 'DRIVE_MANAGE', 'DRIVE_VIEW',
    'POLICY_MANAGE', 'ELIGIBILITY_EVALUATE', 'ELIGIBILITY_OVERRIDE',
    'APPLICATION_MANAGE', 'APPLICATION_SUBMIT'
  )
ON CONFLICT DO NOTHING;

-- TPO_ADMIN gets full placement operations, policy management, and override authority
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'TPO_ADMIN'
  AND p.permission_name IN (
    'COMPANY_MANAGE', 'COMPANY_VIEW', 'DRIVE_MANAGE', 'DRIVE_VIEW',
    'POLICY_MANAGE', 'ELIGIBILITY_EVALUATE', 'ELIGIBILITY_OVERRIDE',
    'APPLICATION_MANAGE'
  )
ON CONFLICT DO NOTHING;

-- TPO_COORDINATOR gets operational viewing, evaluations and application coordination (no override, no policy drafting)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'TPO_COORDINATOR'
  AND p.permission_name IN (
    'COMPANY_VIEW', 'DRIVE_VIEW', 'ELIGIBILITY_EVALUATE', 'APPLICATION_MANAGE'
  )
ON CONFLICT DO NOTHING;

-- DIRECTOR gets read-only placement oversight
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'DIRECTOR'
  AND p.permission_name IN ('COMPANY_VIEW', 'DRIVE_VIEW')
ON CONFLICT DO NOTHING;

-- HOD and FACULTY get drive & company visibility for their departmental students
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name IN ('HOD', 'FACULTY')
  AND p.permission_name IN ('COMPANY_VIEW', 'DRIVE_VIEW')
ON CONFLICT DO NOTHING;

-- STUDENT gets drive viewing, personal eligibility evaluations and application submission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'STUDENT'
  AND p.permission_name IN ('COMPANY_VIEW', 'DRIVE_VIEW', 'ELIGIBILITY_EVALUATE', 'APPLICATION_SUBMIT')
ON CONFLICT DO NOTHING;


-- 2. Companies Table
CREATE TABLE companies (
    company_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name VARCHAR(255) NOT NULL,
    company_code VARCHAR(50) NOT NULL UNIQUE,
    website VARCHAR(255),
    industry VARCHAR(100),
    description TEXT,
    recruiter_name VARCHAR(100),
    recruiter_email VARCHAR(100),
    recruiter_phone VARCHAR(25),
    company_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    internal_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT chk_company_status CHECK (company_status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE INDEX idx_companies_code ON companies(company_code);
CREATE INDEX idx_companies_status ON companies(company_status);


-- 3. Versioned Placement Policies Table
CREATE TABLE placement_policies (
    policy_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_code VARCHAR(100) NOT NULL,
    policy_name VARCHAR(255) NOT NULL,
    version_number INT NOT NULL DEFAULT 1,
    policy_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    min_cgpa NUMERIC(4, 2),
    min_class_10_percentage NUMERIC(5, 2),
    min_class_12_percentage NUMERIC(5, 2),
    min_diploma_percentage NUMERIC(5, 2),
    max_active_backlogs INT NOT NULL DEFAULT 0,
    max_historical_backlogs INT NOT NULL DEFAULT 0,
    allow_academic_gaps BOOLEAN NOT NULL DEFAULT TRUE,
    max_academic_gap_years INT DEFAULT 0,
    requires_all_qualifications_verified BOOLEAN NOT NULL DEFAULT TRUE,
    requires_cgpa_verified BOOLEAN NOT NULL DEFAULT TRUE,
    criteria_payload_json TEXT,
    published_at TIMESTAMPTZ,
    published_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT uq_policy_code_version UNIQUE (policy_code, version_number),
    CONSTRAINT chk_policy_status CHECK (policy_status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX idx_policies_code_ver ON placement_policies(policy_code, version_number);
CREATE INDEX idx_policies_status ON placement_policies(policy_status);

-- Policy eligible programs relational junction
CREATE TABLE placement_policy_programs (
    policy_id UUID NOT NULL REFERENCES placement_policies(policy_id) ON DELETE CASCADE,
    program_id INT NOT NULL REFERENCES academic_programs(program_id) ON DELETE RESTRICT,
    PRIMARY KEY (policy_id, program_id)
);

-- Policy eligible batches relational junction
CREATE TABLE placement_policy_batches (
    policy_id UUID NOT NULL REFERENCES placement_policies(policy_id) ON DELETE CASCADE,
    batch_id INT NOT NULL REFERENCES academic_batches(batch_id) ON DELETE RESTRICT,
    PRIMARY KEY (policy_id, batch_id)
);


-- 4. Recruitment Drives Table
CREATE TABLE placement_drives (
    drive_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(company_id) ON DELETE RESTRICT,
    drive_title VARCHAR(255) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    drive_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    registration_start_date TIMESTAMPTZ,
    registration_deadline TIMESTAMPTZ,
    drive_date DATE,
    recruitment_schedule TEXT,
    venue VARCHAR(255),
    reporting_time VARCHAR(50),
    instructions TEXT,
    contact_details TEXT,
    published_at TIMESTAMPTZ,
    published_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT chk_drive_status CHECK (drive_status IN ('DRAFT', 'PUBLISHED', 'IN_PROGRESS', 'CLOSED', 'COMPLETED', 'ARCHIVED'))
);

CREATE INDEX idx_drives_company ON placement_drives(company_id);
CREATE INDEX idx_drives_status ON placement_drives(drive_status);
CREATE INDEX idx_drives_academic_year ON placement_drives(academic_year);


-- 5. Job Profiles Table
CREATE TABLE job_profiles (
    job_profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    drive_id UUID NOT NULL REFERENCES placement_drives(drive_id) ON DELETE CASCADE,
    policy_id UUID NOT NULL REFERENCES placement_policies(policy_id) ON DELETE RESTRICT,
    profile_title VARCHAR(255) NOT NULL,
    job_description TEXT,
    employment_type VARCHAR(50) NOT NULL,
    location VARCHAR(150),
    work_mode VARCHAR(50) NOT NULL DEFAULT 'ON_SITE',
    required_skills TEXT,
    total_ctc_lpa NUMERIC(8, 2) NOT NULL,
    fixed_pay_lpa NUMERIC(8, 2),
    variable_pay_lpa NUMERIC(8, 2),
    retention_bonus_lpa NUMERIC(8, 2),
    monthly_stipend_inr NUMERIC(10, 2),
    ppo_eligible BOOLEAN NOT NULL DEFAULT FALSE,
    selection_process TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT chk_employment_type CHECK (employment_type IN ('FULL_TIME', 'INTERNSHIP', 'INTERNSHIP_PLUS_FULL_TIME')),
    CONSTRAINT chk_work_mode CHECK (work_mode IN ('ON_SITE', 'HYBRID', 'REMOTE'))
);

CREATE INDEX idx_job_profiles_drive ON job_profiles(drive_id);
CREATE INDEX idx_job_profiles_policy ON job_profiles(policy_id);


-- 6. Standalone Eligibility Evaluations Table
CREATE TABLE eligibility_evaluations (
    evaluation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    job_profile_id UUID NOT NULL REFERENCES job_profiles(job_profile_id) ON DELETE CASCADE,
    policy_id UUID NOT NULL REFERENCES placement_policies(policy_id) ON DELETE RESTRICT,
    policy_version INT NOT NULL,
    outcome VARCHAR(30) NOT NULL,
    criteria_snapshot_json TEXT NOT NULL,
    unmet_criteria_json TEXT,
    missing_verification_json TEXT,
    evaluated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    evaluated_by_user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT chk_eval_outcome CHECK (outcome IN ('ELIGIBLE', 'INELIGIBLE', 'NEEDS_VERIFICATION'))
);

CREATE INDEX idx_eval_student_profile ON eligibility_evaluations(student_id, job_profile_id, evaluated_at DESC);
CREATE INDEX idx_eval_outcome ON eligibility_evaluations(outcome);


-- 7. Job Applications Table
CREATE TABLE job_applications (
    application_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_profile_id UUID NOT NULL REFERENCES job_profiles(job_profile_id) ON DELETE RESTRICT,
    student_id UUID NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    evaluation_id UUID NOT NULL REFERENCES eligibility_evaluations(evaluation_id) ON DELETE RESTRICT,
    application_status VARCHAR(30) NOT NULL DEFAULT 'APPLIED',
    resume_version_tag VARCHAR(100) NOT NULL,
    student_remarks TEXT,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status_updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status_updated_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    status_change_reason TEXT,
    CONSTRAINT uq_job_app_student_profile UNIQUE (job_profile_id, student_id),
    CONSTRAINT chk_app_status CHECK (application_status IN ('APPLIED', 'IN_PROCESS', 'WITHDRAWN', 'REJECTED', 'OFFERED', 'PLACED'))
);

CREATE INDEX idx_app_profile ON job_applications(job_profile_id);
CREATE INDEX idx_app_student ON job_applications(student_id);
CREATE INDEX idx_app_status ON job_applications(application_status);


-- 8. Eligibility Overrides Table
CREATE TABLE eligibility_overrides (
    override_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    evaluation_id UUID NOT NULL REFERENCES eligibility_evaluations(evaluation_id) ON DELETE RESTRICT,
    approved_by_user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    override_reason TEXT NOT NULL,
    override_outcome VARCHAR(30) NOT NULL DEFAULT 'ELIGIBLE',
    original_outcome VARCHAR(30) NOT NULL,
    overridden_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_override_outcome CHECK (override_outcome IN ('ELIGIBLE', 'INELIGIBLE', 'NEEDS_VERIFICATION'))
);

CREATE INDEX idx_overrides_evaluation ON eligibility_overrides(evaluation_id);
