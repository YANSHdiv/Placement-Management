-- V3__academic_registry_schema.sql: Student Academic Registry Schema
-- Milestone 2: Departments, Programs, Batches, Students, Qualifications, Semester Results, Backlogs & Import Tracking

-- 1. Ensure departments has is_active column
ALTER TABLE departments ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- 2. Academic Programs table
CREATE TABLE academic_programs (
    program_id SERIAL PRIMARY KEY,
    program_code VARCHAR(30) UNIQUE NOT NULL,
    program_name VARCHAR(150) NOT NULL,
    degree_type VARCHAR(50) NOT NULL, -- UG, PG, DIPLOMA, DOCTORAL
    department_id INT NOT NULL REFERENCES departments(department_id) ON DELETE RESTRICT,
    duration_years INT NOT NULL DEFAULT 4,
    total_semesters INT NOT NULL DEFAULT 8,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_programs_dept ON academic_programs(department_id);
CREATE INDEX idx_programs_code ON academic_programs(program_code);

-- 3. Academic Batches table
CREATE TABLE academic_batches (
    batch_id SERIAL PRIMARY KEY,
    batch_name VARCHAR(100) NOT NULL, -- e.g., '2022-2026'
    start_year INT NOT NULL,
    graduation_year INT NOT NULL,
    program_id INT NOT NULL REFERENCES academic_programs(program_id) ON DELETE RESTRICT,
    is_placement_active BOOLEAN DEFAULT TRUE,
    current_semester INT NOT NULL DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_program_grad_year UNIQUE (program_id, graduation_year)
);

CREATE INDEX idx_batches_program ON academic_batches(program_id);
CREATE INDEX idx_batches_grad_year ON academic_batches(graduation_year);

-- 4. Students table
CREATE TABLE students (
    student_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE REFERENCES users(user_id) ON DELETE SET NULL,
    university_student_id VARCHAR(50) UNIQUE NOT NULL,
    roll_number VARCHAR(50) UNIQUE NOT NULL,
    registration_number VARCHAR(50) UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    university_email VARCHAR(150) UNIQUE NOT NULL,
    personal_email VARCHAR(150),
    phone_number VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(20),
    program_id INT NOT NULL REFERENCES academic_programs(program_id) ON DELETE RESTRICT,
    department_id INT NOT NULL REFERENCES departments(department_id) ON DELETE RESTRICT,
    batch_id INT NOT NULL REFERENCES academic_batches(batch_id) ON DELETE RESTRICT,
    current_semester INT NOT NULL DEFAULT 1,
    section VARCHAR(20),
    admission_date DATE,
    academic_status VARCHAR(30) NOT NULL DEFAULT 'ENROLLED', -- ENROLLED, SUSPENDED, ALUMNI, DROPOUT
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, VERIFIED, REJECTED, CHANGES_REQUESTED
    verified_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    verified_at TIMESTAMP WITH TIME ZONE,
    verification_remarks TEXT,
    -- Summary Metrics (derived from canonical records, NULL allowed, never default to 0.0)
    cgpa NUMERIC(4,2),
    active_backlogs INT NOT NULL DEFAULT 0,
    historical_backlogs INT NOT NULL DEFAULT 0,
    total_credits_earned NUMERIC(5,2) DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_students_dept ON students(department_id);
CREATE INDEX idx_students_program ON students(program_id);
CREATE INDEX idx_students_batch ON students(batch_id);
CREATE INDEX idx_students_univ_id ON students(university_student_id);
CREATE INDEX idx_students_roll ON students(roll_number);
CREATE INDEX idx_students_email ON students(university_email);
CREATE INDEX idx_students_user ON students(user_id);
CREATE INDEX idx_students_verification ON students(verification_status);

-- 5. Student Qualifications table (Class 10, Class 12, Diploma, UG, etc.)
CREATE TABLE student_qualifications (
    qualification_id SERIAL PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    qualification_type VARCHAR(30) NOT NULL, -- CLASS_10, CLASS_12, DIPLOMA, GRADUATION
    board_or_university VARCHAR(150) NOT NULL,
    institution_name VARCHAR(200),
    passing_year INT NOT NULL,
    marking_scheme VARCHAR(20) NOT NULL DEFAULT 'PERCENTAGE', -- PERCENTAGE, CGPA
    percentage_or_cgpa NUMERIC(5,2), -- NULL allowed for missing/unverified, not 0.0
    maximum_marks NUMERIC(6,2),
    obtained_marks NUMERIC(6,2),
    marksheet_document_ref VARCHAR(255),
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    verified_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    verified_at TIMESTAMP WITH TIME ZONE,
    verification_remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_qual_type UNIQUE (student_id, qualification_type)
);

CREATE INDEX idx_qual_student ON student_qualifications(student_id);

-- 6. Semester Results table
CREATE TABLE semester_results (
    result_id SERIAL PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    semester_number INT NOT NULL,
    sgpa NUMERIC(4,2), -- NULL allowed
    credits_registered NUMERIC(4,1),
    credits_earned NUMERIC(4,1),
    result_status VARCHAR(30) NOT NULL DEFAULT 'PASSED', -- PASSED, FAILED, DETAINED, WITHHELD
    has_active_backlog BOOLEAN DEFAULT FALSE,
    marksheet_document_ref VARCHAR(255),
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    verified_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
    verified_at TIMESTAMP WITH TIME ZONE,
    verification_remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_semester UNIQUE (student_id, semester_number)
);

CREATE INDEX idx_sem_res_student ON semester_results(student_id);
CREATE INDEX idx_sem_res_student_sem ON semester_results(student_id, semester_number);

-- 7. Subject Attempts & Backlogs table
CREATE TABLE subject_attempts (
    attempt_id SERIAL PRIMARY KEY,
    result_id INT NOT NULL REFERENCES semester_results(result_id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    subject_code VARCHAR(30) NOT NULL,
    subject_name VARCHAR(150) NOT NULL,
    semester_number INT NOT NULL,
    attempt_number INT NOT NULL DEFAULT 1,
    credits NUMERIC(3,1) NOT NULL DEFAULT 3.0,
    grade VARCHAR(10),
    marks_obtained NUMERIC(5,2),
    max_marks NUMERIC(5,2),
    is_backlog BOOLEAN NOT NULL DEFAULT FALSE,
    is_cleared BOOLEAN NOT NULL DEFAULT FALSE,
    cleared_in_semester INT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attempts_result ON subject_attempts(result_id);
CREATE INDEX idx_attempts_student ON subject_attempts(student_id);
CREATE INDEX idx_attempts_backlog ON subject_attempts(student_id, is_backlog, is_cleared);

-- 8. Bulk Imports Tracking
CREATE TABLE student_imports (
    import_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    imported_by UUID NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(20) NOT NULL, -- CSV, XLSX
    status VARCHAR(30) NOT NULL DEFAULT 'PREVIEWED', -- PREVIEWED, COMMITTED, FAILED, CANCELLED
    total_rows INT NOT NULL DEFAULT 0,
    valid_rows INT NOT NULL DEFAULT 0,
    error_rows INT NOT NULL DEFAULT 0,
    inserted_count INT NOT NULL DEFAULT 0,
    updated_count INT NOT NULL DEFAULT 0,
    skipped_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    committed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_imports_user ON student_imports(imported_by);
CREATE INDEX idx_imports_created ON student_imports(created_at);

-- 9. Bulk Import Rows & Validation Errors
CREATE TABLE student_import_rows (
    row_id SERIAL PRIMARY KEY,
    import_id UUID NOT NULL REFERENCES student_imports(import_id) ON DELETE CASCADE,
    row_number INT NOT NULL,
    row_data JSONB NOT NULL,
    is_valid BOOLEAN NOT NULL,
    errors JSONB,
    warnings JSONB,
    status VARCHAR(20) DEFAULT 'PENDING' -- PENDING, INSERTED, UPDATED, SKIPPED, REJECTED
);

CREATE INDEX idx_import_rows_import_id ON student_import_rows(import_id);
