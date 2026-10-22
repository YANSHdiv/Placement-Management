-- V4__seed_academic_master_data.sql: Seed Academic Master Data & Initial Records
-- Seed Programs, Batches, Additional Permissions, and Canonical Students

-- 1. Insert Additional Permissions
INSERT INTO permissions (permission_name, module, description) VALUES
('STUDENT_PROFILE_MANAGE', 'ACADEMICS', 'Create, update and manage student academic profiles'),
('STUDENT_EXPORT', 'ACADEMICS', 'Export student academic records to Excel/CSV with departmental scoping')
ON CONFLICT (permission_name) DO NOTHING;

-- Map to SUPER_ADMIN, TPO_ADMIN, HOD
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name IN ('SUPER_ADMIN', 'TPO_ADMIN', 'HOD')
  AND p.permission_name IN ('STUDENT_PROFILE_MANAGE', 'STUDENT_EXPORT')
ON CONFLICT DO NOTHING;

-- 2. Seed Academic Programs
INSERT INTO academic_programs (program_code, program_name, degree_type, department_id, duration_years, total_semesters)
SELECT 'BTECH_CSE', 'Bachelor of Technology in Computer Science & Engineering', 'UG', d.department_id, 4, 8
FROM departments d WHERE d.department_code = 'CSE'
ON CONFLICT (program_code) DO NOTHING;

INSERT INTO academic_programs (program_code, program_name, degree_type, department_id, duration_years, total_semesters)
SELECT 'BTECH_ECE', 'Bachelor of Technology in Electronics & Communication', 'UG', d.department_id, 4, 8
FROM departments d WHERE d.department_code = 'ECE'
ON CONFLICT (program_code) DO NOTHING;

INSERT INTO academic_programs (program_code, program_name, degree_type, department_id, duration_years, total_semesters)
SELECT 'BTECH_ME', 'Bachelor of Technology in Mechanical Engineering', 'UG', d.department_id, 4, 8
FROM departments d WHERE d.department_code = 'ME'
ON CONFLICT (program_code) DO NOTHING;

INSERT INTO academic_programs (program_code, program_name, degree_type, department_id, duration_years, total_semesters)
SELECT 'BCA', 'Bachelor of Computer Applications', 'UG', d.department_id, 3, 6
FROM departments d WHERE d.department_code = 'IT'
ON CONFLICT (program_code) DO NOTHING;

INSERT INTO academic_programs (program_code, program_name, degree_type, department_id, duration_years, total_semesters)
SELECT 'MCA', 'Master of Computer Applications', 'PG', d.department_id, 2, 4
FROM departments d WHERE d.department_code = 'IT'
ON CONFLICT (program_code) DO NOTHING;

INSERT INTO academic_programs (program_code, program_name, degree_type, department_id, duration_years, total_semesters)
SELECT 'MBA', 'Master of Business Administration', 'PG', d.department_id, 2, 4
FROM departments d WHERE d.department_code = 'SOB'
ON CONFLICT (program_code) DO NOTHING;

-- 3. Seed Academic Batches
-- B.Tech CSE Batches
INSERT INTO academic_batches (batch_name, start_year, graduation_year, program_id, is_placement_active, current_semester)
SELECT 'B.Tech CSE 2021-2025', 2021, 2025, p.program_id, FALSE, 8
FROM academic_programs p WHERE p.program_code = 'BTECH_CSE'
ON CONFLICT ON CONSTRAINT uq_program_grad_year DO NOTHING;

INSERT INTO academic_batches (batch_name, start_year, graduation_year, program_id, is_placement_active, current_semester)
SELECT 'B.Tech CSE 2022-2026', 2022, 2026, p.program_id, TRUE, 6
FROM academic_programs p WHERE p.program_code = 'BTECH_CSE'
ON CONFLICT ON CONSTRAINT uq_program_grad_year DO NOTHING;

INSERT INTO academic_batches (batch_name, start_year, graduation_year, program_id, is_placement_active, current_semester)
SELECT 'B.Tech CSE 2023-2027', 2023, 2027, p.program_id, FALSE, 4
FROM academic_programs p WHERE p.program_code = 'BTECH_CSE'
ON CONFLICT ON CONSTRAINT uq_program_grad_year DO NOTHING;

-- B.Tech ECE 2022-2026
INSERT INTO academic_batches (batch_name, start_year, graduation_year, program_id, is_placement_active, current_semester)
SELECT 'B.Tech ECE 2022-2026', 2022, 2026, p.program_id, TRUE, 6
FROM academic_programs p WHERE p.program_code = 'BTECH_ECE'
ON CONFLICT ON CONSTRAINT uq_program_grad_year DO NOTHING;

-- MCA 2023-2025
INSERT INTO academic_batches (batch_name, start_year, graduation_year, program_id, is_placement_active, current_semester)
SELECT 'MCA 2023-2025', 2023, 2025, p.program_id, TRUE, 4
FROM academic_programs p WHERE p.program_code = 'MCA'
ON CONFLICT ON CONSTRAINT uq_program_grad_year DO NOTHING;

-- MBA 2024-2026
INSERT INTO academic_batches (batch_name, start_year, graduation_year, program_id, is_placement_active, current_semester)
SELECT 'MBA 2024-2026', 2024, 2026, p.program_id, TRUE, 2
FROM academic_programs p WHERE p.program_code = 'MBA'
ON CONFLICT ON CONSTRAINT uq_program_grad_year DO NOTHING;

-- 4. Seed Canonical Demo Students (Aarav Mehta, Riya Sharma, Vikram Verma)
DO $$
DECLARE
    cse_dept_id INT;
    ece_dept_id INT;
    cse_prog_id INT;
    ece_prog_id INT;
    cse_batch_26_id INT;
    ece_batch_26_id INT;
    demo_user_id UUID;
    student1_id UUID;
    student2_id UUID;
    student3_id UUID;
    res_sem1 INT;
    res_sem2 INT;
    res_sem3 INT;
BEGIN
    SELECT department_id INTO cse_dept_id FROM departments WHERE department_code = 'CSE';
    SELECT department_id INTO ece_dept_id FROM departments WHERE department_code = 'ECE';
    SELECT program_id INTO cse_prog_id FROM academic_programs WHERE program_code = 'BTECH_CSE';
    SELECT program_id INTO ece_prog_id FROM academic_programs WHERE program_code = 'BTECH_ECE';
    SELECT batch_id INTO cse_batch_26_id FROM academic_batches WHERE program_id = cse_prog_id AND graduation_year = 2026;
    SELECT batch_id INTO ece_batch_26_id FROM academic_batches WHERE program_id = ece_prog_id AND graduation_year = 2026;

    SELECT user_id INTO demo_user_id FROM users WHERE university_email = 'student.cse@jecrcuniversity.edu.in';

    -- Student 1: Aarav Mehta (Linked to student.cse@jecrcuniversity.edu.in if exists)
    INSERT INTO students (
        university_student_id, roll_number, registration_number,
        first_name, last_name, university_email, personal_email, phone_number,
        date_of_birth, gender, program_id, department_id, batch_id, current_semester, section,
        academic_status, verification_status, cgpa, active_backlogs, historical_backlogs, total_credits_earned, user_id
    ) VALUES (
        '22JU0101', '22BCSE101', 'JU/2022/CSE/101',
        'Aarav', 'Mehta', 'student.cse@jecrcuniversity.edu.in', 'aarav.mehta.demo@gmail.com', '+91-9876543210',
        '2004-05-15', 'Male', cse_prog_id, cse_dept_id, cse_batch_26_id, 6, 'A',
        'ENROLLED', 'VERIFIED', 8.42, 0, 1, 108.0, demo_user_id
    )
    ON CONFLICT (university_student_id) DO UPDATE SET user_id = EXCLUDED.user_id
    RETURNING student_id INTO student1_id;

    -- Qualifications for Student 1
    INSERT INTO student_qualifications (student_id, qualification_type, board_or_university, institution_name, passing_year, marking_scheme, percentage_or_cgpa, verification_status)
    VALUES
    (student1_id, 'CLASS_10', 'CBSE', 'Delhi Public School, Jaipur', 2020, 'PERCENTAGE', 92.40, 'VERIFIED'),
    (student1_id, 'CLASS_12', 'CBSE', 'Delhi Public School, Jaipur', 2022, 'PERCENTAGE', 89.60, 'VERIFIED')
    ON CONFLICT ON CONSTRAINT uq_student_qual_type DO NOTHING;

    -- Semester Results for Student 1
    INSERT INTO semester_results (student_id, semester_number, sgpa, credits_registered, credits_earned, result_status, has_active_backlog, verification_status)
    VALUES (student1_id, 1, 8.20, 22.0, 22.0, 'PASSED', FALSE, 'VERIFIED')
    ON CONFLICT ON CONSTRAINT uq_student_semester DO UPDATE SET sgpa = EXCLUDED.sgpa
    RETURNING result_id INTO res_sem1;

    INSERT INTO semester_results (student_id, semester_number, sgpa, credits_registered, credits_earned, result_status, has_active_backlog, verification_status)
    VALUES (student1_id, 2, 7.80, 22.0, 22.0, 'PASSED', FALSE, 'VERIFIED')
    ON CONFLICT ON CONSTRAINT uq_student_semester DO UPDATE SET sgpa = EXCLUDED.sgpa
    RETURNING result_id INTO res_sem2;

    INSERT INTO semester_results (student_id, semester_number, sgpa, credits_registered, credits_earned, result_status, has_active_backlog, verification_status)
    VALUES (student1_id, 3, 8.65, 24.0, 24.0, 'PASSED', FALSE, 'VERIFIED')
    ON CONFLICT ON CONSTRAINT uq_student_semester DO UPDATE SET sgpa = EXCLUDED.sgpa
    RETURNING result_id INTO res_sem3;

    -- Subject Attempts for Semester 1
    INSERT INTO subject_attempts (result_id, student_id, subject_code, subject_name, semester_number, attempt_number, credits, grade, marks_obtained, max_marks, is_backlog, is_cleared)
    VALUES
    (res_sem1, student1_id, 'CS101', 'Programming in C', 1, 1, 4.0, 'A+', 88.0, 100.0, FALSE, TRUE),
    (res_sem1, student1_id, 'MA101', 'Engineering Mathematics I', 1, 1, 4.0, 'A', 81.0, 100.0, FALSE, TRUE),
    (res_sem1, student1_id, 'PH101', 'Engineering Physics', 1, 1, 3.0, 'B+', 74.0, 100.0, FALSE, TRUE)
    ON CONFLICT DO NOTHING;

    -- Subject Attempts for Semester 2 (had a backlog in Data Structures that was cleared in attempt 2)
    INSERT INTO subject_attempts (result_id, student_id, subject_code, subject_name, semester_number, attempt_number, credits, grade, marks_obtained, max_marks, is_backlog, is_cleared, cleared_in_semester)
    VALUES
    (res_sem2, student1_id, 'CS102', 'Data Structures & Algorithms', 2, 1, 4.0, 'F', 32.0, 100.0, TRUE, TRUE, 3),
    (res_sem2, student1_id, 'CS102', 'Data Structures & Algorithms (Backlog Clearance)', 2, 2, 4.0, 'B+', 76.0, 100.0, FALSE, TRUE, 3)
    ON CONFLICT DO NOTHING;

    -- Student 2: Riya Sharma (ECE - Verified, high CGPA 9.10)
    INSERT INTO students (
        university_student_id, roll_number, registration_number,
        first_name, last_name, university_email, personal_email, phone_number,
        date_of_birth, gender, program_id, department_id, batch_id, current_semester, section,
        academic_status, verification_status, cgpa, active_backlogs, historical_backlogs, total_credits_earned
    ) VALUES (
        '22JU0102', '22BECE102', 'JU/2022/ECE/102',
        'Riya', 'Sharma', 'riya.sharma@jecrcuniversity.edu.in', 'riya.ece@gmail.com', '+91-9876543211',
        '2004-08-22', 'Female', ece_prog_id, ece_dept_id, ece_batch_26_id, 6, 'A',
        'ENROLLED', 'VERIFIED', 9.10, 0, 0, 110.0
    )
    ON CONFLICT (university_student_id) DO NOTHING
    RETURNING student_id INTO student2_id;

    IF student2_id IS NOT NULL THEN
        INSERT INTO student_qualifications (student_id, qualification_type, board_or_university, institution_name, passing_year, marking_scheme, percentage_or_cgpa, verification_status)
        VALUES
        (student2_id, 'CLASS_10', 'RBSE', 'St. Xavier School, Jaipur', 2020, 'PERCENTAGE', 94.20, 'VERIFIED'),
        (student2_id, 'CLASS_12', 'RBSE', 'St. Xavier School, Jaipur', 2022, 'PERCENTAGE', 91.80, 'VERIFIED')
        ON CONFLICT ON CONSTRAINT uq_student_qual_type DO NOTHING;
    END IF;

    -- Student 3: Vikram Verma (CSE - Pending Verification, 1 Active Backlog)
    INSERT INTO students (
        university_student_id, roll_number, registration_number,
        first_name, last_name, university_email, personal_email, phone_number,
        date_of_birth, gender, program_id, department_id, batch_id, current_semester, section,
        academic_status, verification_status, cgpa, active_backlogs, historical_backlogs, total_credits_earned
    ) VALUES (
        '22JU0103', '22BCSE103', 'JU/2022/CSE/103',
        'Vikram', 'Verma', 'vikram.verma@jecrcuniversity.edu.in', 'vikram.v@gmail.com', '+91-9876543212',
        '2003-11-10', 'Male', cse_prog_id, cse_dept_id, cse_batch_26_id, 6, 'B',
        'ENROLLED', 'PENDING', 6.85, 1, 1, 98.0
    )
    ON CONFLICT (university_student_id) DO NOTHING
    RETURNING student_id INTO student3_id;

    IF student3_id IS NOT NULL THEN
        INSERT INTO student_qualifications (student_id, qualification_type, board_or_university, institution_name, passing_year, marking_scheme, percentage_or_cgpa, verification_status)
        VALUES
        (student3_id, 'CLASS_10', 'CBSE', 'Kendriya Vidyalaya No. 1, Jaipur', 2019, 'PERCENTAGE', 78.50, 'PENDING'),
        (student3_id, 'CLASS_12', 'CBSE', 'Kendriya Vidyalaya No. 1, Jaipur', 2021, 'PERCENTAGE', 72.00, 'PENDING')
        ON CONFLICT ON CONSTRAINT uq_student_qual_type DO NOTHING;
    END IF;

END $$;
