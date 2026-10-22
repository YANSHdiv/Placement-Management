-- V2: Seed Roles, Named Permissions, Scopes and Initial Departments

-- Seed Departments
INSERT INTO departments (department_code, department_name) VALUES
('CSE', 'Computer Science & Engineering'),
('ECE', 'Electronics & Communication Engineering'),
('ME', 'Mechanical Engineering'),
('IT', 'Information Technology'),
('SOB', 'School of Business');

-- Seed System Roles
INSERT INTO roles (role_name, description, is_system_role) VALUES
('SUPER_ADMIN', 'Full institutional control, system settings, user management, and security audits', TRUE),
('TPO_ADMIN', 'Head of Training and Placement. Drives, policies, company relations and offers', TRUE),
('TPO_COORDINATOR', 'Operational placement coordinator. Interview rounds and candidate logistics', TRUE),
('DIRECTOR', 'Executive leadership. Institution-wide analytics, placement summaries and audits', TRUE),
('HOD', 'Head of Department. Department student records, verification and attendance shortages', TRUE),
('FACULTY', 'Teaching faculty. Timetables, attendance marking and student CRT mentoring', TRUE),
('STUDENT', 'Enrolled student. Academic records, drive applications and placement status', TRUE),
('RECRUITER', 'External corporate recruiter. Candidate shortlisting and interview scoring', TRUE);

-- Seed Named Permissions
INSERT INTO permissions (permission_name, module, description) VALUES
-- IAM & Audit
('USER_MANAGE', 'IAM', 'Create, update and manage user accounts'),
('ROLE_ASSIGN', 'IAM', 'Assign and revoke user roles and custom permissions'),
('USER_SUSPEND', 'IAM', 'Suspend or activate user accounts and revoke sessions'),
('AUDIT_LOG_VIEW', 'AUDIT', 'View system-wide security and administrative audit logs'),

-- Academics
('STUDENT_PROFILE_READ', 'ACADEMICS', 'View student profiles, qualifications, and semester marksheets'),
('STUDENT_RECORD_VERIFY', 'ACADEMICS', 'Officially verify or reject student academic records'),
('RESULT_BULK_IMPORT', 'ACADEMICS', 'Perform bulk Excel import of student semester results'),

-- Timetable & Attendance
('TIMETABLE_MANAGE', 'TIMETABLE', 'Configure classrooms and schedule conflict-free timetable slots'),
('ATTENDANCE_MARK', 'ATTENDANCE', 'Open sessions and record student classroom attendance'),
('ATTENDANCE_CORRECT', 'ATTENDANCE', 'Submit authorized corrections to past attendance records'),
('ATTENDANCE_REPORT', 'ATTENDANCE', 'Generate and view attendance shortage and summary reports'),

-- CRT & Training
('CRT_PROGRAM_MANAGE', 'CRT', 'Schedule CRT sessions, modules, and assessment question banks'),
('CRT_SCORE_RECORD', 'CRT', 'Record scores and evaluations for student mock assessments'),

-- Placement Drives & Policy
('DRIVE_MANAGE', 'PLACEMENT', 'Create, configure, publish and manage company recruitment drives'),
('POLICY_MANAGE', 'PLACEMENT', 'Author and publish versioned placement eligibility policies'),
('ELIGIBILITY_OVERRIDE', 'PLACEMENT', 'Grant authorized policy exceptions with mandatory justification'),
('DRIVE_APPLY', 'PLACEMENT', 'Apply to eligible recruitment drives or withdraw applications'),

-- Interview Rounds
('ROUND_SCORE_RECORD', 'INTERVIEWS', 'Record evaluator scores and candidate interview feedback'),
('ROUND_RESULT_PUBLISH', 'INTERVIEWS', 'Publish round results and advance shortlisted candidates'),

-- Offers & Internships
('OFFER_RECORD_MANAGE', 'OFFERS', 'Issue, update and manage company placement offers'),
('OFFER_DECIDE', 'OFFERS', 'Accept or decline received placement offers'),
('INTERNSHIP_MANAGE', 'INTERNSHIPS', 'Register internships and assign faculty mentors'),
('INTERNSHIP_LOG_SUBMIT', 'INTERNSHIPS', 'Submit student internship progress logs'),

-- Notifications & Executive Reports
('NOTIFICATION_BROADCAST', 'NOTIFICATIONS', 'Broadcast cohort-wide email and in-app notifications'),
('REPORT_INSTITUTION', 'ANALYTICS', 'Access institution-wide placement KPIs and analytical dashboards');

-- Helper function to map role to permission
CREATE OR REPLACE FUNCTION map_role_permission(r_name VARCHAR, p_name VARCHAR)
RETURNS VOID AS $$
BEGIN
    INSERT INTO role_permissions (role_id, permission_id)
    SELECT r.role_id, p.permission_id
    FROM roles r, permissions p
    WHERE r.role_name = r_name AND p.permission_name = p_name
    ON CONFLICT DO NOTHING;
END;
$$ LANGUAGE plpgsql;

-- 1. SUPER_ADMIN Mappings (All Permissions)
DO $$
DECLARE
    perm RECORD;
BEGIN
    FOR perm IN SELECT permission_name FROM permissions LOOP
        PERFORM map_role_permission('SUPER_ADMIN', perm.permission_name);
    END LOOP;
END $$;

-- 2. TPO_ADMIN Mappings
SELECT map_role_permission('TPO_ADMIN', 'STUDENT_PROFILE_READ');
SELECT map_role_permission('TPO_ADMIN', 'STUDENT_RECORD_VERIFY');
SELECT map_role_permission('TPO_ADMIN', 'ATTENDANCE_REPORT');
SELECT map_role_permission('TPO_ADMIN', 'CRT_PROGRAM_MANAGE');
SELECT map_role_permission('TPO_ADMIN', 'CRT_SCORE_RECORD');
SELECT map_role_permission('TPO_ADMIN', 'DRIVE_MANAGE');
SELECT map_role_permission('TPO_ADMIN', 'POLICY_MANAGE');
SELECT map_role_permission('TPO_ADMIN', 'ELIGIBILITY_OVERRIDE');
SELECT map_role_permission('TPO_ADMIN', 'ROUND_SCORE_RECORD');
SELECT map_role_permission('TPO_ADMIN', 'ROUND_RESULT_PUBLISH');
SELECT map_role_permission('TPO_ADMIN', 'OFFER_RECORD_MANAGE');
SELECT map_role_permission('TPO_ADMIN', 'INTERNSHIP_MANAGE');
SELECT map_role_permission('TPO_ADMIN', 'NOTIFICATION_BROADCAST');
SELECT map_role_permission('TPO_ADMIN', 'REPORT_INSTITUTION');

-- 3. TPO_COORDINATOR Mappings
SELECT map_role_permission('TPO_COORDINATOR', 'STUDENT_PROFILE_READ');
SELECT map_role_permission('TPO_COORDINATOR', 'CRT_SCORE_RECORD');
SELECT map_role_permission('TPO_COORDINATOR', 'ROUND_SCORE_RECORD');
SELECT map_role_permission('TPO_COORDINATOR', 'DRIVE_MANAGE');

-- 4. DIRECTOR Mappings
SELECT map_role_permission('DIRECTOR', 'STUDENT_PROFILE_READ');
SELECT map_role_permission('DIRECTOR', 'ATTENDANCE_REPORT');
SELECT map_role_permission('DIRECTOR', 'REPORT_INSTITUTION');
SELECT map_role_permission('DIRECTOR', 'AUDIT_LOG_VIEW');
SELECT map_role_permission('DIRECTOR', 'NOTIFICATION_BROADCAST');

-- 5. HOD Mappings (Department Scoped)
SELECT map_role_permission('HOD', 'STUDENT_PROFILE_READ');
SELECT map_role_permission('HOD', 'STUDENT_RECORD_VERIFY');
SELECT map_role_permission('HOD', 'RESULT_BULK_IMPORT');
SELECT map_role_permission('HOD', 'TIMETABLE_MANAGE');
SELECT map_role_permission('HOD', 'ATTENDANCE_CORRECT');
SELECT map_role_permission('HOD', 'ATTENDANCE_REPORT');
SELECT map_role_permission('HOD', 'INTERNSHIP_MANAGE');
SELECT map_role_permission('HOD', 'NOTIFICATION_BROADCAST');

-- 6. FACULTY Mappings (Class/Section Scoped)
SELECT map_role_permission('FACULTY', 'STUDENT_PROFILE_READ');
SELECT map_role_permission('FACULTY', 'ATTENDANCE_MARK');
SELECT map_role_permission('FACULTY', 'ATTENDANCE_REPORT');
SELECT map_role_permission('FACULTY', 'CRT_SCORE_RECORD');
SELECT map_role_permission('FACULTY', 'INTERNSHIP_MANAGE');

-- 7. STUDENT Mappings (Own Scoped)
SELECT map_role_permission('STUDENT', 'STUDENT_PROFILE_READ');
SELECT map_role_permission('STUDENT', 'ATTENDANCE_REPORT');
SELECT map_role_permission('STUDENT', 'DRIVE_APPLY');
SELECT map_role_permission('STUDENT', 'OFFER_DECIDE');
SELECT map_role_permission('STUDENT', 'INTERNSHIP_LOG_SUBMIT');

-- 8. RECRUITER Mappings (Assigned Drive Scoped)
SELECT map_role_permission('RECRUITER', 'DRIVE_MANAGE');
SELECT map_role_permission('RECRUITER', 'ROUND_SCORE_RECORD');

DROP FUNCTION map_role_permission;
