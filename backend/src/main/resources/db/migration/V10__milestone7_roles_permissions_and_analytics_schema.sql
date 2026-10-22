-- V10__milestone7_roles_permissions_and_analytics_schema.sql: Role Permission Corrections and Analytics Indexes

-- 1. Correct and augment DIRECTOR role permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'DIRECTOR'
  AND p.permission_name IN (
    'REPORT_INSTITUTION',
    'AUDIT_LOG_VIEW',
    'STUDENT_PROFILE_READ',
    'ATTENDANCE_REPORT',
    'ATTENDANCE_REPORT_VIEW',
    'OFFER_VIEW',
    'DRIVE_VIEW',
    'COMPANY_VIEW',
    'NOTIFICATION_VIEW',
    'NOTIFICATION_BROADCAST',
    'CRT_MANAGE',
    'TIMETABLE_VIEW'
  )
ON CONFLICT DO NOTHING;

-- 2. Correct and augment TPO_COORDINATOR role permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'TPO_COORDINATOR'
  AND p.permission_name IN (
    'DRIVE_VIEW',
    'COMPANY_VIEW',
    'INTERVIEW_SCHEDULE_MANAGE',
    'ROUND_SCORE_RECORD',
    'ROUND_EVALUATION_SUBMIT',
    'CRT_ATTENDANCE_MARK',
    'EVENT_MANAGE',
    'EVENT_PUBLISH',
    'NOTIFICATION_VIEW',
    'TIMETABLE_VIEW'
  )
ON CONFLICT DO NOTHING;

-- 3. Composite Indexes for Fast Unified Dashboards & Placement Analytics Aggregations
CREATE INDEX IF NOT EXISTS idx_offers_status_ctc ON placement_offers(offer_status, total_ctc_lpa);
CREATE INDEX IF NOT EXISTS idx_offers_student_status ON placement_offers(student_id, offer_status);
CREATE INDEX IF NOT EXISTS idx_att_records_status ON attendance_records(attendance_status);
CREATE INDEX IF NOT EXISTS idx_att_records_student_status ON attendance_records(student_id, attendance_status);
CREATE INDEX IF NOT EXISTS idx_students_dept_verification ON students(department_id, verification_status);
