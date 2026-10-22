-- V7__interview_selection_and_feedback_schema.sql: Interview Rounds, Evaluators, Candidate Selection, Scheduling, Question Bank & Feedback

-- 1. Insert Milestone 4 Permissions
INSERT INTO permissions (permission_name, module, description) VALUES
('INTERVIEW_ROUND_MANAGE', 'INTERVIEW', 'Configure and manage recruitment interview rounds'),
('ROUND_EVALUATOR_ASSIGN', 'INTERVIEW', 'Assign evaluators and interviewers to specific rounds'),
('ROUND_CANDIDATE_VIEW', 'INTERVIEW', 'View candidates scheduled and participating in interview rounds'),
('ROUND_EVALUATION_SUBMIT', 'INTERVIEW', 'Submit scores, qualitative feedback, and recommendations for assigned candidates'),
('ROUND_RESULT_UPLOAD', 'INTERVIEW', 'Upload and enter round candidate scores and evaluation outcomes'),
('ROUND_RESULT_PUBLISH', 'INTERVIEW', 'Officially publish round results and advance candidates to subsequent stages'),
('INTERVIEW_SCHEDULE_MANAGE', 'INTERVIEW', 'Schedule candidate interview appointment slots and resolve scheduling conflicts'),
('QUESTION_BANK_MANAGE', 'INTERVIEW', 'Manage interview question repository, categories, difficulty tags, and guidance'),
('INTERVIEW_FEEDBACK_SUBMIT', 'INTERVIEW', 'Submit recruitment feedback (student, evaluator, or TPO)'),
('INTERVIEW_FEEDBACK_VIEW', 'INTERVIEW', 'View aggregate and individual recruitment feedback reports')
ON CONFLICT (permission_name) DO NOTHING;

-- Map Permissions to Roles
-- SUPER_ADMIN & TPO_ADMIN get all interview permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name IN ('SUPER_ADMIN', 'TPO_ADMIN')
  AND p.permission_name IN (
    'INTERVIEW_ROUND_MANAGE', 'ROUND_EVALUATOR_ASSIGN', 'ROUND_CANDIDATE_VIEW',
    'ROUND_EVALUATION_SUBMIT', 'ROUND_RESULT_UPLOAD', 'ROUND_RESULT_PUBLISH',
    'INTERVIEW_SCHEDULE_MANAGE', 'QUESTION_BANK_MANAGE', 'INTERVIEW_FEEDBACK_SUBMIT',
    'INTERVIEW_FEEDBACK_VIEW'
  )
ON CONFLICT DO NOTHING;

-- TPO_COORDINATOR gets operational round viewing, scheduling, uploads, question bank & feedback viewing
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'TPO_COORDINATOR'
  AND p.permission_name IN (
    'ROUND_CANDIDATE_VIEW', 'ROUND_RESULT_UPLOAD',
    'INTERVIEW_SCHEDULE_MANAGE', 'QUESTION_BANK_MANAGE', 'INTERVIEW_FEEDBACK_VIEW'
  )
ON CONFLICT DO NOTHING;

-- FACULTY gets candidate viewing, evaluation submission, and feedback submission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'FACULTY'
  AND p.permission_name IN (
    'ROUND_CANDIDATE_VIEW', 'ROUND_EVALUATION_SUBMIT', 'INTERVIEW_FEEDBACK_SUBMIT'
  )
ON CONFLICT DO NOTHING;

-- STUDENT gets recruitment feedback submission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'STUDENT'
  AND p.permission_name IN ('INTERVIEW_FEEDBACK_SUBMIT')
ON CONFLICT DO NOTHING;

-- 2. Expand Application Status Check Constraint
ALTER TABLE job_applications DROP CONSTRAINT IF EXISTS chk_app_status;
ALTER TABLE job_applications ADD CONSTRAINT chk_app_status 
  CHECK (application_status IN ('APPLIED', 'SHORTLISTED', 'IN_PROCESS', 'IN_ROUNDS', 'WITHDRAWN', 'REJECTED', 'OFFERED', 'SELECTED', 'PLACED'));


-- 3. Interview Rounds Table
CREATE TABLE interview_rounds (
    round_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_profile_id UUID NOT NULL REFERENCES job_profiles(job_profile_id) ON DELETE CASCADE,
    round_name VARCHAR(150) NOT NULL,
    round_type VARCHAR(50) NOT NULL,
    sequence_order INT NOT NULL,
    instructions TEXT,
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    venue_or_meeting_url VARCHAR(500),
    candidate_scheduling_mode VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED_SLOTS',
    evaluation_criteria TEXT,
    max_score NUMERIC(5, 2),
    scoring_scale_description VARCHAR(255),
    publication_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    is_results_published BOOLEAN NOT NULL DEFAULT FALSE,
    results_published_at TIMESTAMPTZ,
    results_published_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT uq_round_profile_sequence UNIQUE (job_profile_id, sequence_order),
    CONSTRAINT chk_round_pub_status CHECK (publication_status IN ('DRAFT', 'CONFIGURED', 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT chk_round_type CHECK (round_type IN ('APTITUDE', 'ONLINE_ASSESSMENT', 'CODING_ASSESSMENT', 'TECHNICAL_INTERVIEW', 'MANAGERIAL_INTERVIEW', 'HR_INTERVIEW', 'GROUP_DISCUSSION', 'CUSTOM'))
);

CREATE INDEX idx_rounds_job_profile ON interview_rounds(job_profile_id, sequence_order);


-- 4. Round Evaluators Table
CREATE TABLE round_evaluators (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    round_id UUID NOT NULL REFERENCES interview_rounds(round_id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role_or_designation VARCHAR(100),
    assigned_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_round_evaluator UNIQUE (round_id, user_id)
);

CREATE INDEX idx_round_evaluators_round ON round_evaluators(round_id);
CREATE INDEX idx_round_evaluators_user ON round_evaluators(user_id);


-- 5. Round Candidates Table (Progression within each round)
CREATE TABLE round_candidates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    round_id UUID NOT NULL REFERENCES interview_rounds(round_id) ON DELETE CASCADE,
    application_id UUID NOT NULL REFERENCES job_applications(application_id) ON DELETE CASCADE,
    candidate_round_status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    final_score NUMERIC(5, 2),
    tpo_decision_remarks TEXT,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMPTZ,
    published_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_round_application UNIQUE (round_id, application_id),
    CONSTRAINT chk_candidate_round_status CHECK (candidate_round_status IN ('SCHEDULED', 'IN_PROGRESS', 'ADVANCED', 'ELIMINATED', 'ON_HOLD', 'ABSENT', 'EXEMPTED'))
);

CREATE INDEX idx_round_candidates_round ON round_candidates(round_id);
CREATE INDEX idx_round_candidates_app ON round_candidates(application_id);
CREATE INDEX idx_round_candidates_status ON round_candidates(candidate_round_status);


-- 6. Round Evaluations Table (Evaluator submissions)
CREATE TABLE round_evaluations (
    evaluation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    round_candidate_id UUID NOT NULL REFERENCES round_candidates(id) ON DELETE CASCADE,
    evaluator_user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    score NUMERIC(5, 2),
    evaluation_state VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    recommendation VARCHAR(30),
    qualitative_feedback TEXT,
    confidential_notes TEXT,
    submitted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_candidate_evaluator UNIQUE (round_candidate_id, evaluator_user_id),
    CONSTRAINT chk_eval_state CHECK (evaluation_state IN ('DRAFT', 'SUBMITTED')),
    CONSTRAINT chk_eval_recommendation CHECK (recommendation IS NULL OR recommendation IN ('STRONG_HIRE', 'HIRE', 'BORDERLINE', 'REJECT'))
);

CREATE INDEX idx_round_eval_candidate ON round_evaluations(round_candidate_id);
CREATE INDEX idx_round_eval_evaluator ON round_evaluations(evaluator_user_id);


-- 7. Candidate Interview Appointments (With Conflict Detection Support)
CREATE TABLE candidate_interview_appointments (
    appointment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    round_candidate_id UUID NOT NULL REFERENCES round_candidates(id) ON DELETE CASCADE,
    evaluator_user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
    student_id UUID NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    venue_or_meeting_link VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    reschedule_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT chk_appointment_time CHECK (end_time > start_time),
    CONSTRAINT chk_appointment_status CHECK (status IN ('SCHEDULED', 'RESCHEDULED', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_appointments_candidate ON candidate_interview_appointments(round_candidate_id);
CREATE INDEX idx_appointments_student_time ON candidate_interview_appointments(student_id, start_time, end_time);
CREATE INDEX idx_appointments_evaluator_time ON candidate_interview_appointments(evaluator_user_id, start_time, end_time);


-- 8. Candidate Status History (Audit timeline for all progression & transitions)
CREATE TABLE candidate_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES job_applications(application_id) ON DELETE CASCADE,
    round_id UUID REFERENCES interview_rounds(round_id) ON DELETE SET NULL,
    from_status VARCHAR(50) NOT NULL,
    to_status VARCHAR(50) NOT NULL,
    changed_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    remarks TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_status_history_app ON candidate_status_history(application_id, created_at DESC);


-- 9. Question Bank Table
CREATE TABLE interview_questions (
    question_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID REFERENCES companies(company_id) ON DELETE SET NULL,
    job_profile_id UUID REFERENCES job_profiles(job_profile_id) ON DELETE SET NULL,
    round_type VARCHAR(50),
    title VARCHAR(255) NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    category VARCHAR(100) NOT NULL,
    difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    tags VARCHAR(255),
    expected_answer_guidance TEXT,
    is_confidential BOOLEAN NOT NULL DEFAULT TRUE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_question_type CHECK (question_type IN ('MULTIPLE_CHOICE', 'CODING_QUESTION', 'TECHNICAL_DISCUSSION', 'BEHAVIORAL_QUESTION', 'HR_QUESTION', 'OPEN_ENDED')),
    CONSTRAINT chk_question_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD'))
);

CREATE INDEX idx_questions_company ON interview_questions(company_id);
CREATE INDEX idx_questions_category ON interview_questions(category);
CREATE INDEX idx_questions_difficulty ON interview_questions(difficulty);


-- 10. Interview Feedback Table
CREATE TABLE interview_feedback (
    feedback_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feedback_type VARCHAR(50) NOT NULL,
    application_id UUID REFERENCES job_applications(application_id) ON DELETE CASCADE,
    round_id UUID REFERENCES interview_rounds(round_id) ON DELETE SET NULL,
    company_id UUID REFERENCES companies(company_id) ON DELETE CASCADE,
    submitted_by UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comments TEXT,
    is_confidential_to_staff BOOLEAN NOT NULL DEFAULT FALSE,
    moderation_status VARCHAR(30) NOT NULL DEFAULT 'APPROVED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_feedback_type CHECK (feedback_type IN ('STUDENT_TO_DRIVE', 'EVALUATOR_TO_CANDIDATE', 'TPO_TO_COMPANY')),
    CONSTRAINT chk_feedback_moderation CHECK (moderation_status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_feedback_company ON interview_feedback(company_id);
CREATE INDEX idx_feedback_app ON interview_feedback(application_id);
CREATE INDEX idx_feedback_round ON interview_feedback(round_id);
