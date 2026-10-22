-- V5__sanitize_demo_student_data.sql: Clean synthetic demo student data from schema migrations
-- Migrations must strictly contain schema definitions and permanent master data.
-- Demo/synthetic student test data is isolated to application bootstrap (BootstrapAdminRunner) gated by app.bootstrap.seed-demo-users=true.

DELETE FROM subject_attempts WHERE student_id IN (
    SELECT student_id FROM students WHERE university_student_id IN ('22JU0101', '22JU0102', '22JU0103')
);

DELETE FROM semester_results WHERE student_id IN (
    SELECT student_id FROM students WHERE university_student_id IN ('22JU0101', '22JU0102', '22JU0103')
);

DELETE FROM student_qualifications WHERE student_id IN (
    SELECT student_id FROM students WHERE university_student_id IN ('22JU0101', '22JU0102', '22JU0103')
);

DELETE FROM students WHERE university_student_id IN ('22JU0101', '22JU0102', '22JU0103');
