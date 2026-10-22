-- ============================================================================
-- JECRC University Placement & Academic Portal - V9 Migration
-- Module: Academic Operations, Timetables, Attendance, Faculty, CRT & Notifications
-- ============================================================================

-- 0. EXTENSIONS & OFFER POLICY ENHANCEMENTS
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE institutional_offer_policies 
    ADD COLUMN IF NOT EXISTS version_number INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS is_synthetic_default BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE institutional_offer_policies 
SET is_synthetic_default = TRUE 
WHERE policy_code = 'JECRC_DEFAULT_OFFER_POLICY_2026';

ALTER TABLE placement_offers
    ADD COLUMN IF NOT EXISTS effective_policy_version INT DEFAULT 1;

ALTER TABLE offer_decision_history
    ADD COLUMN IF NOT EXISTS effective_policy_version INT DEFAULT 1;

-- 1. CLASSROOMS & VENUES
CREATE TABLE classrooms (
    classroom_id SERIAL PRIMARY KEY,
    room_number VARCHAR(50) NOT NULL UNIQUE,
    building_name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL DEFAULT 60,
    room_type VARCHAR(50) NOT NULL DEFAULT 'LECTURE_HALL', -- LECTURE_HALL, COMPUTER_LAB, SEMINAR_ROOM, AUDITORIUM
    latitude NUMERIC(10, 7) DEFAULT 26.7797820,  -- JECRC University Campus Baseline
    longitude NUMERIC(10, 7) DEFAULT 75.8778640,
    geofence_radius_meters INT NOT NULL DEFAULT 60,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed Standard JECRC University Classrooms
INSERT INTO classrooms (room_number, building_name, capacity, room_type, latitude, longitude, geofence_radius_meters) VALUES
    ('LT-101', 'Academic Block A', 80, 'LECTURE_HALL', 26.7797820, 75.8778640, 60),
    ('LT-102', 'Academic Block A', 80, 'LECTURE_HALL', 26.7798100, 75.8778900, 60),
    ('LAB-301', 'Engineering Wing B', 45, 'COMPUTER_LAB', 26.7796500, 75.8777200, 50),
    ('LAB-302', 'Engineering Wing B', 45, 'COMPUTER_LAB', 26.7796800, 75.8777500, 50),
    ('SEMINAR-HALL-1', 'Central Auditorium Block', 150, 'SEMINAR_ROOM', 26.7801000, 75.8781000, 80)
ON CONFLICT (room_number) DO NOTHING;

-- 2. ACADEMIC SUBJECTS / CURRICULUM
CREATE TABLE academic_subjects (
    subject_id SERIAL PRIMARY KEY,
    department_id INT NOT NULL REFERENCES departments(department_id) ON DELETE RESTRICT,
    program_id INT REFERENCES academic_programs(program_id) ON DELETE SET NULL,
    subject_code VARCHAR(50) NOT NULL UNIQUE,
    subject_name VARCHAR(150) NOT NULL,
    credits INT NOT NULL DEFAULT 3,
    semester INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed Core Subjects for CSE / Engineering
INSERT INTO academic_subjects (department_id, program_id, subject_code, subject_name, credits, semester) VALUES
    (1, 1, 'CSE301', 'Design & Analysis of Algorithms', 4, 5),
    (1, 1, 'CSE302', 'Database Management Systems', 4, 5),
    (1, 1, 'CSE303', 'Operating Systems & Concurrency', 4, 5),
    (1, 1, 'CSE304', 'Cloud Computing & Distributed Systems', 3, 5),
    (1, 1, 'CSE305', 'Full-Stack Software Engineering', 4, 6),
    (1, 1, 'CSE306', 'Compiler Design', 4, 6)
ON CONFLICT (subject_code) DO NOTHING;

-- 3. FACULTY PROFILES
CREATE TABLE faculty_profiles (
    faculty_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    faculty_code VARCHAR(50) NOT NULL UNIQUE,
    department_id INT NOT NULL REFERENCES departments(department_id) ON DELETE RESTRICT,
    designation VARCHAR(100) NOT NULL DEFAULT 'Assistant Professor',
    specialization VARCHAR(200),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_faculty_department ON faculty_profiles(department_id);

-- Link existing faculty user to faculty_profiles
INSERT INTO faculty_profiles (user_id, faculty_code, department_id, designation, specialization)
SELECT u.user_id, 'JECRC-FAC-CSE-001', 1, 'Associate Professor', 'Distributed Systems & Cloud Computing'
FROM users u
WHERE u.university_email = 'faculty.cse@jecrcuniversity.edu.in'
ON CONFLICT (faculty_code) DO NOTHING;

-- 4. FACULTY TEACHING ASSIGNMENTS
CREATE TABLE faculty_teaching_assignments (
    assignment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    faculty_id UUID NOT NULL REFERENCES faculty_profiles(faculty_id) ON DELETE CASCADE,
    subject_id INT NOT NULL REFERENCES academic_subjects(subject_id) ON DELETE CASCADE,
    batch_id INT NOT NULL REFERENCES academic_batches(batch_id) ON DELETE CASCADE,
    section VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    academic_year VARCHAR(20) NOT NULL DEFAULT '2025-2026',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_faculty_teaching_assign UNIQUE (faculty_id, subject_id, batch_id, section, semester, academic_year)
);

CREATE INDEX idx_teaching_assign_faculty ON faculty_teaching_assignments(faculty_id);

-- 5. TIMETABLE SLOTS & GIST RACE-FREE CONFLICT PREVENTION
CREATE TABLE timetable_slots (
    slot_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id INT NOT NULL REFERENCES academic_batches(batch_id) ON DELETE CASCADE,
    semester INT NOT NULL,
    section VARCHAR(20) NOT NULL,
    subject_id INT NOT NULL REFERENCES academic_subjects(subject_id) ON DELETE CASCADE,
    faculty_id UUID NOT NULL REFERENCES faculty_profiles(faculty_id) ON DELETE CASCADE,
    classroom_id INT NOT NULL REFERENCES classrooms(classroom_id) ON DELETE RESTRICT,
    day_of_week VARCHAR(15) NOT NULL, -- MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    academic_year VARCHAR(20) NOT NULL DEFAULT '2025-2026',
    status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED', -- DRAFT, PUBLISHED, SUSPENDED, CANCELLED
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Half-open interval slot range in minutes from midnight [start_minute, end_minute)
    time_slot_range INT4RANGE GENERATED ALWAYS AS (
        int4range((extract(epoch from start_time)/60)::int, (extract(epoch from end_time)/60)::int, '[)')
    ) STORED,
    CONSTRAINT chk_time_range CHECK (end_time > start_time),
    -- GiST Exclusion Constraint 1: Prevent overlapping reservations for the same classroom
    CONSTRAINT excl_no_classroom_overlap EXCLUDE USING gist (
        classroom_id WITH =,
        day_of_week WITH =,
        academic_year WITH =,
        time_slot_range WITH &&
    ) WHERE (is_active = TRUE AND status != 'CANCELLED'),
    -- GiST Exclusion Constraint 2: Prevent overlapping reservations for the same faculty member
    CONSTRAINT excl_no_faculty_overlap EXCLUDE USING gist (
        faculty_id WITH =,
        day_of_week WITH =,
        academic_year WITH =,
        time_slot_range WITH &&
    ) WHERE (is_active = TRUE AND status != 'CANCELLED'),
    -- GiST Exclusion Constraint 3: Prevent overlapping reservations for the same student cohort (batch, semester, section)
    CONSTRAINT excl_no_section_overlap EXCLUDE USING gist (
        batch_id WITH =,
        semester WITH =,
        section WITH =,
        day_of_week WITH =,
        academic_year WITH =,
        time_slot_range WITH &&
    ) WHERE (is_active = TRUE AND status != 'CANCELLED')
);

CREATE INDEX idx_timetable_batch_sec ON timetable_slots(batch_id, semester, section);
CREATE INDEX idx_timetable_faculty ON timetable_slots(faculty_id);
CREATE INDEX idx_timetable_classroom ON timetable_slots(classroom_id);

-- 6. CLASSROOM ATTENDANCE SESSIONS
CREATE TABLE attendance_sessions (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slot_id UUID REFERENCES timetable_slots(slot_id) ON DELETE SET NULL,
    subject_id INT NOT NULL REFERENCES academic_subjects(subject_id) ON DELETE RESTRICT,
    faculty_id UUID NOT NULL REFERENCES faculty_profiles(faculty_id) ON DELETE RESTRICT,
    batch_id INT NOT NULL REFERENCES academic_batches(batch_id) ON DELETE RESTRICT,
    semester INT NOT NULL,
    section VARCHAR(20) NOT NULL,
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    topic_covered VARCHAR(255),
    session_status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, FINALIZED, CANCELLED
    is_geofence_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    authorized_latitude NUMERIC(10, 7),
    authorized_longitude NUMERIC(10, 7),
    geofence_radius_meters INT NOT NULL DEFAULT 60,
    opened_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finalized_at TIMESTAMPTZ,
    finalized_by UUID REFERENCES users(user_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_attendance_session_status CHECK (session_status IN ('OPEN', 'FINALIZED', 'CANCELLED'))
);

CREATE INDEX idx_att_sessions_batch ON attendance_sessions(batch_id, semester, section, session_date);
CREATE INDEX idx_att_sessions_faculty ON attendance_sessions(faculty_id, session_date);

-- 7. ATTENDANCE RECORDS
CREATE TABLE attendance_records (
    record_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES attendance_sessions(session_id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    attendance_status VARCHAR(30) NOT NULL DEFAULT 'PRESENT', -- PRESENT, ABSENT, LATE, EXCUSED
    marked_by UUID REFERENCES users(user_id),
    marked_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verification_method VARCHAR(30) NOT NULL DEFAULT 'MANUAL_ROSTER', -- MANUAL_ROSTER, GEOTAGGED_SELF, QR_CODE
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    accuracy_meters NUMERIC(8, 2),
    calculated_distance_meters NUMERIC(8, 2),
    geofence_status VARCHAR(30) NOT NULL DEFAULT 'NOT_APPLICABLE', -- WITHIN_GEOFENCE, OUTSIDE_GEOFENCE, LOCATION_UNAVAILABLE, ACCURACY_EXCEEDED, EXEMPTION_APPROVED, NOT_APPLICABLE
    evidence_photo_ref VARCHAR(300),
    remarks TEXT,
    is_corrected BOOLEAN NOT NULL DEFAULT FALSE,
    original_status VARCHAR(30),
    correction_reason TEXT,
    corrected_by UUID REFERENCES users(user_id),
    corrected_at TIMESTAMPTZ,
    CONSTRAINT uq_session_student UNIQUE (session_id, student_id),
    CONSTRAINT chk_att_status CHECK (attendance_status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED')),
    CONSTRAINT chk_geofence_status CHECK (geofence_status IN ('WITHIN_GEOFENCE', 'OUTSIDE_GEOFENCE', 'LOCATION_UNAVAILABLE', 'ACCURACY_EXCEEDED', 'EXEMPTION_APPROVED', 'NOT_APPLICABLE'))
);

CREATE INDEX idx_att_records_student ON attendance_records(student_id);
CREATE INDEX idx_att_records_session ON attendance_records(session_id);

-- 8. ATTENDANCE CORRECTION HISTORY (Append-Only Audit Trail)
CREATE TABLE attendance_correction_history (
    correction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    record_id UUID NOT NULL REFERENCES attendance_records(record_id) ON DELETE CASCADE,
    session_id UUID NOT NULL REFERENCES attendance_sessions(session_id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    previous_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    correction_reason TEXT NOT NULL,
    actor_user_id UUID REFERENCES users(user_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_att_correction_record ON attendance_correction_history(record_id);

-- 9. CAMPUS RECRUITMENT TRAINING (CRT)
CREATE TABLE crt_programs (
    crt_program_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    program_code VARCHAR(50) NOT NULL UNIQUE,
    program_name VARCHAR(150) NOT NULL,
    description TEXT,
    academic_year VARCHAR(20) NOT NULL DEFAULT '2025-2026',
    target_batch_id INT REFERENCES academic_batches(batch_id) ON DELETE SET NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE crt_sessions (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crt_program_id UUID NOT NULL REFERENCES crt_programs(crt_program_id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'APTITUDE', -- APTITUDE, CODING, COMMUNICATION, INTERVIEW_PREP, RESUME_BUILDING
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    venue VARCHAR(150),
    trainer_name VARCHAR(150),
    trainer_email VARCHAR(150),
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    max_seats INT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_crt_status CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_crt_category CHECK (category IN ('APTITUDE', 'CODING', 'COMMUNICATION', 'INTERVIEW_PREP', 'RESUME_BUILDING'))
);

CREATE TABLE crt_enrollments (
    enrollment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES crt_sessions(session_id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    attendance_status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED', -- REGISTERED, ATTENDED, ABSENT, EXCUSED
    score NUMERIC(5, 2),
    feedback_rating INT,
    feedback_comments TEXT,
    marked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_crt_session_student UNIQUE (session_id, student_id),
    CONSTRAINT chk_crt_att_status CHECK (attendance_status IN ('REGISTERED', 'ATTENDED', 'ABSENT', 'EXCUSED'))
);

CREATE INDEX idx_crt_enrollment_session ON crt_enrollments(session_id);
CREATE INDEX idx_crt_enrollment_student ON crt_enrollments(student_id);

-- 10. PORTAL EVENTS & ANNOUNCEMENTS
CREATE TABLE portal_events (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_title VARCHAR(200) NOT NULL,
    event_type VARCHAR(50) NOT NULL DEFAULT 'WORKSHOP', -- WORKSHOP, SEMINAR, HACKATHON, PRE_PLACEMENT_TALK, CAREER_SESSION, ANNOUNCEMENT
    description TEXT,
    event_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    venue VARCHAR(200),
    online_meeting_url VARCHAR(300),
    target_audience VARCHAR(50) NOT NULL DEFAULT 'ALL', -- ALL, BTECH_CSE, FINAL_YEAR, FACULTY_ONLY, PLACEMENT_ELIGIBLE
    department_id INT REFERENCES departments(department_id),
    batch_id INT REFERENCES academic_batches(batch_id),
    registration_deadline TIMESTAMPTZ,
    max_participants INT,
    organizer_name VARCHAR(150),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT, PUBLISHED, CANCELLED, CONCLUDED
    published_at TIMESTAMPTZ,
    published_by UUID REFERENCES users(user_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_event_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'CONCLUDED'))
);

CREATE TABLE event_registrations (
    registration_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES portal_events(event_id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    registered_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    attendance_status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED', -- REGISTERED, ATTENDED, ABSENT
    CONSTRAINT uq_event_student UNIQUE (event_id, student_id),
    CONSTRAINT chk_event_att_status CHECK (attendance_status IN ('REGISTERED', 'ATTENDED', 'ABSENT'))
);

CREATE INDEX idx_event_registrations_event ON event_registrations(event_id);
CREATE INDEX idx_event_registrations_student ON event_registrations(student_id);

-- 11. NOTIFICATIONS & TRANSACTIONAL OUTBOX
CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_user_id UUID REFERENCES users(user_id) ON DELETE CASCADE,
    target_audience_type VARCHAR(50) NOT NULL DEFAULT 'INDIVIDUAL', -- INDIVIDUAL, PROGRAM, BATCH, DEPARTMENT, ALL_STUDENTS, ALL_FACULTY
    target_audience_id VARCHAR(100),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'GENERAL', -- ACADEMIC, TIMETABLE, ATTENDANCE, RECRUITMENT_DRIVE, INTERVIEW, OFFER, CRT, EVENT, GENERAL
    related_entity_type VARCHAR(50),
    related_entity_id VARCHAR(100),
    action_url VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_recipient ON notifications(recipient_user_id, is_read);
CREATE INDEX idx_notifications_created ON notifications(created_at DESC);

CREATE TABLE notification_outbox (
    outbox_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSED, FAILED, RETRYING
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    error_details TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,
    CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED', 'RETRYING'))
);

CREATE INDEX idx_notification_outbox_status ON notification_outbox(status);

-- 12. GRANULAR PERMISSIONS FOR MILESTONE 6
INSERT INTO permissions (permission_name, description, module) VALUES
    ('FACULTY_MANAGE', 'Manage faculty master profiles and academic designations', 'ACADEMIC'),
    ('FACULTY_ASSIGNMENT_MANAGE', 'Allocate teaching subject and class section assignments to faculty', 'ACADEMIC'),
    ('TIMETABLE_VIEW', 'View published academic timetables and room allocations', 'ACADEMIC'),
    ('TIMETABLE_MANAGE', 'Create, update, and publish academic timetable slots and room reservations', 'ACADEMIC'),
    ('ATTENDANCE_MARK', 'Conduct attendance sessions and record student attendance', 'ATTENDANCE'),
    ('ATTENDANCE_CORRECT', 'Submit and approve formal attendance adjustments with mandatory justifications', 'ATTENDANCE'),
    ('ATTENDANCE_EVIDENCE_VIEW', 'Inspect geotagged coordinates, distance, and photo evidence', 'ATTENDANCE'),
    ('ATTENDANCE_REPORT_VIEW', 'Access institutional attendance analytics, subject reports, and shortage rosters', 'ATTENDANCE'),
    ('CRT_MANAGE', 'Manage Campus Recruitment Training programs, modules, and sessions', 'CRT'),
    ('CRT_ATTENDANCE_MARK', 'Record attendance and scores for Campus Recruitment Training sessions', 'CRT'),
    ('EVENT_MANAGE', 'Create and curate academic and career events, seminars, and workshops', 'EVENT'),
    ('EVENT_PUBLISH', 'Officially publish events and announcements to targeted audiences', 'EVENT'),
    ('EVENT_REGISTER', 'Register student participation for campus events and workshops', 'EVENT'),
    ('NOTIFICATION_VIEW', 'Access personal notifications and announcement feeds', 'NOTIFICATION'),
    ('NOTIFICATION_BROADCAST', 'Broadcast institutional announcements and cohort alerts', 'NOTIFICATION')
ON CONFLICT (permission_name) DO NOTHING;

-- Map permissions to roles
-- SUPER_ADMIN & TPO_ADMIN: Full institutional authority
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r, permissions p
WHERE r.role_name IN ('SUPER_ADMIN', 'TPO_ADMIN')
  AND p.permission_name IN (
    'FACULTY_MANAGE', 'FACULTY_ASSIGNMENT_MANAGE', 'TIMETABLE_VIEW', 'TIMETABLE_MANAGE',
    'ATTENDANCE_MARK', 'ATTENDANCE_CORRECT', 'ATTENDANCE_EVIDENCE_VIEW', 'ATTENDANCE_REPORT_VIEW',
    'CRT_MANAGE', 'CRT_ATTENDANCE_MARK', 'EVENT_MANAGE', 'EVENT_PUBLISH', 'EVENT_REGISTER',
    'NOTIFICATION_VIEW', 'NOTIFICATION_BROADCAST'
  )
ON CONFLICT DO NOTHING;

-- HOD: Departmental faculty, timetable, attendance reports and events
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r, permissions p
WHERE r.role_name = 'HOD'
  AND p.permission_name IN (
    'FACULTY_ASSIGNMENT_MANAGE', 'TIMETABLE_VIEW', 'TIMETABLE_MANAGE',
    'ATTENDANCE_MARK', 'ATTENDANCE_CORRECT', 'ATTENDANCE_EVIDENCE_VIEW', 'ATTENDANCE_REPORT_VIEW',
    'CRT_MANAGE', 'EVENT_MANAGE', 'EVENT_PUBLISH', 'NOTIFICATION_VIEW', 'NOTIFICATION_BROADCAST'
  )
ON CONFLICT DO NOTHING;

-- FACULTY: Assigned timetables, class attendance marking, evidence review, and departmental events
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r, permissions p
WHERE r.role_name = 'FACULTY'
  AND p.permission_name IN (
    'TIMETABLE_VIEW', 'ATTENDANCE_MARK', 'ATTENDANCE_CORRECT', 'ATTENDANCE_EVIDENCE_VIEW',
    'ATTENDANCE_REPORT_VIEW', 'CRT_ATTENDANCE_MARK', 'EVENT_MANAGE', 'EVENT_PUBLISH', 'NOTIFICATION_VIEW'
  )
ON CONFLICT DO NOTHING;

-- STUDENT: Own timetable view, own attendance view, CRT participation, event registration, and notifications
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r, permissions p
WHERE r.role_name = 'STUDENT'
  AND p.permission_name IN (
    'TIMETABLE_VIEW', 'EVENT_REGISTER', 'NOTIFICATION_VIEW'
  )
ON CONFLICT DO NOTHING;
