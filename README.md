# JECRC University — Integrated Institute, Academic, Training & Placement Management Portal

[![Java](https://img.shields.io/badge/Java-21%20LTS-007396?style=flat&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-6DB33F?style=flat&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16.x-336791?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-18.3.1-61DAFB?style=flat&logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6.3-3178C6?style=flat&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-6.4.5-646CFF?style=flat&logo=vite&logoColor=white)](https://vite.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4.17-06B6D4?style=flat&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![License](https://img.shields.io/badge/Ownership-JECRC%20University-DE1819?style=flat)](https://jecrcuniversity.edu.in/)

---

## 🔐 Demo Credentials Required to access

All test accounts share the same password for evaluation and preview testing.

> **Default Password:** `Admin@JECRC2026!Demo`  
> **Portal Login URL:** [https://frontend-gray-six-63.vercel.app/login](https://frontend-gray-six-63.vercel.app/login)

| Role | Name | Email Address | Access & Scope |
| :--- | :--- | :--- | :--- |
| **Student** | Aarav Mehta | `student.cse@jecrcuniversity.edu.in` | View drives, submit applications, log internships, check attendance |
| **Faculty / Mentor** | Prof. Sharma | `faculty.cse@jecrcuniversity.edu.in` | Mentor reviews, interview evaluations, student monitoring |
| **TPO Officer / Coordinator** | Placement Officer | `tpo.officer@jecrcuniversity.edu.in` | Manage recruitment drives, schedules, and candidate rounds |
| **TPO Admin** | TPO Head | `tpo.admin@jecrcuniversity.edu.in` | Full placement management, corporate partner records, offer decisions |
| **Department Head (HOD)** | Head CSE | `hod.cse@jecrcuniversity.edu.in` | Department-level academic analytics, attendance approvals |
| **University Director** | University Director | `director@jecrcuniversity.edu.in` | Institutional dashboards, high-level placement reporting |
| **Super Administrator** | Super Administrator | `admin@jecrcuniversity.edu.in` | Global user management, RBAC configuration, audit logs, system policies |

> [!NOTE]
> All accounts use synthetic demo data aligned with JECRC University's 2025–26 academic and placement cycle.

## 1. Executive Summary & Architecture

This repository houses the enterprise-grade **Integrated Academic, Training & Placement Management Portal** for **JECRC University, Jaipur**. Architected as a maintainable, high-performance **modular monolith**, the platform consolidates student academic progression, campus recruitment training (CRT), placement drive automation, and administrative governance.

### Milestone 1: Security & Identity Core
- **Zero-Trust Security Core:** In-memory short-lived JWTs (15 min) paired with `HttpOnly`, `SameSite=Lax` refresh cookies (7 days).
- **Refresh Token Rotation (RTR) & Breach Detection:** Persisted token family tracking (`refresh_tokens` table) that detects token replays and revokes all descendant tokens in the session family transactionally.
- **Instantaneous Revocation:** Dynamic user security stamps and Caffeine in-memory cache guarantees $0\text{ ms}$ propagation when accounts are suspended or permissions change.
- **Immutable PostgreSQL Audit Trail:** Append-only PostgreSQL audit log protected by a native database trigger guard (`trg_audit_log_immutable`) that aborts any `UPDATE` or `DELETE` attempt at the database engine level.
- **RBAC & Granular Permissions:** 8 seeded roles (`SUPER_ADMIN`, `TPO_ADMIN`, `TPO_COORDINATOR`, `DIRECTOR`, `HOD`, `FACULTY`, `STUDENT`, `RECRUITER`) across 27 named granular permissions.

### Milestone 2: Student Academic Registry (COMPLETED)
- **Canonical Student Academic Registry:** Departments, Academic Programs, Graduation Cohorts/Batches, Enrolled Students, Pre-University Qualifications (Class 10/12/Diploma), Semester Results, and Subject-Level Attempts.
- **Strict Distinction between Missing and Zero Marks:** Null CGPA or percentage values are never defaulted to `0.0`, preserving distinction between pending/unverified results and genuine zero scores.
- **Active vs. Historical Backlog Calculations:** Transactionally maintained from canonical subject attempts. Active backlogs represent uncleared backlogs; historical backlogs capture lifetime backlog incidents.
- **Departmental Scoping & Student Isolation:**
  - `STUDENT`: Restricted strictly to own academic records (`/students/me` or own ID). Accessing another student's record via URL tampering returns `403 Forbidden`.
  - `HOD` / `FACULTY`: Scoped strictly to assigned department(s) (`user_department_scopes`).
  - `SUPER_ADMIN` / `TPO_ADMIN`: Global institutional access.
- **Two-Phase Validated Bulk Import (.CSV / .XLSX):**
  - **Preview:** Validates file structure, data types, duplicate IDs, batch/program existence, and percentages without modifying student tables.
  - **Commit:** Transactionally inserts valid rows and updates unverified records. **Never overwrites already verified academic records.**
- **Excel Export Engine:** Apache POI-powered spreadsheet export respecting caller departmental scoping.

### Milestone 3: Company Management, Placement Drives, Versioned Eligibility Engine & Applications (COMPLETED)
- **Sanitized Demo Data (`V5`):** Forward-only migration safely purged synthetic demo student rows from production migrations; demo users and synthetic records now seed dynamically only in dev/test via `BootstrapAdminRunner`.
- **Company Management & Privacy Guard:** Duplicate company code/name prevention with strict recruiter contact masking for student roles.
- **Placement Drive Lifecycle:** Complete state transitions (`DRAFT` $\to$ `PUBLISHED` $\to$ `CLOSED` $\to$ `ARCHIVED`), enforced publishing validation (requires at least one job profile and an active academic batch), and registration deadline controls.
- **Comprehensive Job Profiles & Tiered CTC:** Models Total CTC, Fixed Pay, Variable Pay, Monthly Stipend, Retention Bonus, Bond Duration, and PPO Conversion terms.
- **Versioned Placement Policies:** Immutable published policies; updates automatically create iterative versions (`new-version`) preserving historical criteria linked to existing drives.
- **Server-Side Deterministic Eligibility Engine:**
  - Evaluates CGPA, Class 10/12/Diploma percentages, active and historical backlogs, academic gaps, and verification statuses.
  - Returns `ELIGIBLE`, `INELIGIBLE` (with exact failure reasons), or `NEEDS_VERIFICATION` (when unverified marks are pending). **Null/missing marks are never coerced to 0.0.**
- **Authorized TPO Overrides:** TPO Admins can record formal eligibility overrides with mandatory justification reasons and auditable event tracking.
- **Student Application Workflow:** Server-enforced double check on submission, resume version tagging, duplicate prevention, and candidate withdrawal.

### Milestone 4: Interview Rounds, Candidate Selection, Evaluators, Question Bank & Feedback (COMPLETED)
- **Forward-Only Database Schema (`V7`):** 8 relational tables (`interview_rounds`, `round_evaluators`, `round_candidates`, `round_evaluations`, `candidate_interview_appointments`, `candidate_status_history`, `interview_questions`, `interview_feedback`) plus constraint extension on `job_applications` supporting `SHORTLISTED`, `IN_ROUNDS`, and `SELECTED`.
- **Interview Round Configuration & Sequencing:** Sequence uniqueness per job profile, evaluation rubric definition, max score parameters, venue / online meeting URL configurations, and candidate scheduling modes (`BATCH`, `INDIVIDUAL_SLOTS`, `WALK_IN`).
- **Evaluator Assignment & Panel Access Isolation:** Evaluators can only access candidate rosters and submit rubric evaluations for rounds explicitly assigned to them. Unassigned rounds return `403 Forbidden`.
- **Evaluator Scoring & Confidential Notes:** Evaluators can save drafts or submit finalized scoring, recommendation decisions (`RECOMMEND_ADVANCE`, `RECOMMEND_HOLD`, `RECOMMEND_ELIMINATE`, `NEUTRAL`), strengths, weaknesses, and qualitative notes. **Confidential staff notes are strictly hidden from students at the database/service layer.**
- **Candidate Progression & Bulk Result Processing:** Candidates transition through granular statuses (`SCHEDULED`, `IN_PROGRESS`, `ADVANCED`, `ELIMINATED`, `ON_HOLD`, `ABSENT`). Includes bulk CSV/tabular result upload with row-level validation reporting (no silent skipping) and atomic round result publishing. Publishing final round results automatically transitions advanced candidates to `SELECTED` on their `job_applications`.
- **Cross-Company Interview Conflict Detection:** Dynamic slot scheduling prevents overlapping interview appointments across different company recruitment drives for students and prevents evaluator double-booking.
- **Question Bank Repository:** Searchable technical and behavioral question library categorized by domain (`DATA_STRUCTURES`, `ALGORITHMS`, `SYSTEM_DESIGN`, `CS_CORE`, `HR_BEHAVIORAL`, `DOMAIN_SPECIFIC`), difficulty, and company association, complete with sample I/O, scoring rubrics, and confidential interviewer hints.
- **Multi-Tier Recruitment Feedback Workflow:** Structured feedback collection supporting Student-to-Drive reviews (ratings for difficulty, transparency, peer recommendations), Evaluator-to-Candidate qualitative feedback, and TPO-to-Company coordination notes.

### Milestone 5: Offers, Internships & PPO Tracking (COMPLETED)
- **Forward-Only Database Schema (`V8`):** 7 relational tables (`institutional_offer_policies`, `placement_offers`, `offer_decision_history`, `internship_records`, `internship_progress_logs`, `internship_evaluations`, `ppo_records`) plus 12 granular permissions.
- **Institutional Offer Policy Engine:** Configurable institutional policy governing single vs. multiple offer acceptance, max active offers, Dream Tier upgrade thresholds, Super Dream absolute cutoffs, and automatic withdrawal flags for pending applications upon offer acceptance.
- **Formal Placement Offer Lifecycle:** End-to-end state transitions (`DRAFT` $\to$ `ISSUED` $\to$ `ACCEPTED` / `DECLINED` / `EXPIRED` / `REVOKED`) with detailed compensation breakdown.
- **Student Decision Workflow & Audit History:** Real-time decision deadlines, student acceptance / decline workflows with mandatory remarks, and immutable append-only audit trail (`offer_decision_history`).
- **Internship Management & Dual Mentorship:** Dual mentorship allocation linking internal academic faculty and corporate industry mentors with bi-weekly progress journals.
- **Staff Evaluation & Confidential Note Masking:** Midterm and final evaluations with rubric scores, strictly masking confidential staff appraisal notes from student API responses.
- **Pre-Placement Offer (PPO) Conversion Queue:** Dedicated TPO review pipeline for top-performing corporate interns with direct conversion into formal placement offers.

### Milestone 6: Academic Operations, Attendance, Faculty, CRT & Notifications (COMPLETED)
- **Forward-Only Database Schema (`V9`):** 11 relational tables (`classrooms`, `academic_subjects`, `faculty_profiles`, `faculty_teaching_assignments`, `timetable_slots`, `attendance_sessions`, `attendance_records`, `attendance_correction_history`, `crt_programs`, `crt_sessions`, `crt_enrollments`, `portal_events`, `event_registrations`, `notifications`, `notification_outbox`) plus 15 granular permissions (`FACULTY_MANAGE`, `FACULTY_ASSIGNMENT_MANAGE`, `TIMETABLE_VIEW`, `TIMETABLE_MANAGE`, `ATTENDANCE_MARK`, `ATTENDANCE_CORRECT`, `ATTENDANCE_EVIDENCE_VIEW`, `ATTENDANCE_REPORT_VIEW`, `CRT_MANAGE`, `CRT_ATTENDANCE_MARK`, `EVENT_MANAGE`, `EVENT_PUBLISH`, `EVENT_REGISTER`, `NOTIFICATION_VIEW`, `NOTIFICATION_BROADCAST`).
- **Offer Policy System Corrections:** Explicit synthetic default tagging (`is_synthetic_default = true`) on fallback policies, policy version tracking on offers and decision histories (`effective_policy_version`), and atomic auditable withdrawal of pending applications only when explicitly enabled by authorized policy (`autoWithdrawOtherApplications == true`).
- **Timetable Scheduling with PostgreSQL GiST Exclusion Constraints:** Race-free concurrency-safe overlap prevention utilizing PostgreSQL's `btree_gist` extension on half-open `[start, end)` intervals (`int4range` generated columns) preventing classroom, faculty, and student cohort overlaps.
- **Classroom Attendance & Geotagged Haversine Distance Engine:**
  - Full session lifecycle (`OPEN` $\to$ `FINALIZED` $\to$ `CANCELLED`) with pre-populated cohort student rosters.
  - Server-side Haversine distance computation between student GPS coordinates and authorized campus landmarks. Client-reported distance is never trusted. Configurable geofence radius (default 60m) with institutional GPS spoofing disclaimer.
  - Session finalization locks records against direct edits.
  - Formal attendance corrections require mandatory justification reasons ($\ge 5\text{ chars}$) with immutable append-only audit tracking in `attendance_correction_history`.
- **Attendance Analytics & Shortage Warning Engine:**
  - Calculates aggregate and subject-wise attendance percentages.
  - Identifies students below the mandatory 75% threshold, computing the exact number of consecutive classes needed to clear shortage ($\lceil \frac{T \cdot N - A}{1 - T} \rceil$).
- **Campus Recruitment Training (CRT) Management:**
  - Dedicated training program tracks (Aptitude, Coding, Soft Skills, System Design) separate from regular academic attendance.
  - Session scheduling, student 1-click enrollment, capacity gating, and trainer score & rubric feedback evaluation.
- **Campus Events & Announcements:** Full publication workflow (`DRAFT` $\to$ `PUBLISHED` $\to$ `CONCLUDED`), audience targeting, and student participation registration.
- **In-App Notification Center & Transactional Outbox:**
  - User personal notification feed with real-time unread counts and read/unread filtering.
  - Transactional Outbox pattern (`notification_outbox` table) ensuring all institutional broadcasts are transactionally committed with the payload before external email/SMS dispatch.

### Milestone 7: Unified Dashboards, Placement Analytics, Public Portal & Integration QA (COMPLETED)
- **Forward-Only Database Schema (`V10`):** Mapped missing role-permission mappings for `DIRECTOR` (`AUDIT_LOG_VIEW`, `ATTENDANCE_REPORT_VIEW`, `STUDENT_PROFILE_EXPORT`, `ANALYTICS_VIEW`) and `TPO_COORDINATOR` (`DRIVE_VIEW`, `DRIVE_MANAGE`, `OFFER_VIEW`, `OFFER_ISSUE`, `CRT_VIEW`, `CRT_MANAGE`, `ANALYTICS_VIEW`), and added composite performance indexes (`idx_offers_status_ctc`, `idx_offers_student_status`, `idx_att_records_status`, `idx_att_records_student_status`, `idx_students_dept_verification`).
- **JECRC University Public Portal Pages:**
  - Official university branding with crimson (`#B91C1C`) and navy (`#0F172A`) palette, and authenticated campus identity (Plot No. IS-2036 to 2039, Ramchandrapura Industrial Area, Vidhani, Sitapura Extension, Jaipur, Rajasthan 303905).
  - Public routes: Home (`/`), About Us (`/about`), Placement Support & Policies (`/placement-support`), Institute & Student Support (`/institute-support`), Contact Us (`/contact`), and Secure Portal Sign In (`/login`).
  - Public Overview API (`GET /api/v1/public/overview`) accessible without authentication.
- **Unified Role-Specific Server-Side Aggregated Dashboards:**
  - **Student Dashboard (`GET /api/v1/dashboards/student`):** Verification status, live CGPA, active/historical backlogs, aggregate attendance percentage, attendance shortage warning banner, applied drives count, shortlist count, and formal offer letters.
  - **TPO Admin Dashboard (`GET /api/v1/dashboards/tpo`):** Total recruiting partners, active drives, total offers issued, unique placed students, verified placement percentage, tier distribution (`REGULAR`, `DREAM`, `SUPER_DREAM`), top corporate partners, and recently issued offers.
  - **Faculty Dashboard (`GET /api/v1/dashboards/faculty`):** Assigned teaching classes, lectures conducted today, student interns under dual mentorship, pending progress journal reviews, and weekly lecture timetable slots.
  - **HOD Dashboard (`GET /api/v1/dashboards/hod`):** Scoped strictly to caller's assigned department; displays verified vs. pending students, department placement percentage, average attendance, attendance shortage watchlist ($<75\%$), and department placement offers.
  - **Director Executive Console (`GET /api/v1/dashboards/director`):** Institution-wide executive governance; total student enrollment, verification progress, institutional placement rate, highest/median CTC, department-by-department audit comparison table, and live security audit feed.
- **De-duplicated Placement Analytics Engine (`GET /api/v1/analytics/placements`):**
  - **Selected Candidates vs. Offers Issued:** Formally isolates candidates selected across interview rounds from formal offers issued.
  - **Unique Placed Students vs. Total Offers:** Prevents double-counting students holding multiple offers (e.g. Dream Tier upgrades).
  - **Accurate Mathematical Percentiles:** Evaluates compensation percentiles (Highest, Average, Median / 50th percentile, Lowest) strictly from accepted offers.
  - **Policy Disclosures:** Detailed methodological notes explaining institutional offer acceptance rules and tier thresholds.

---

## 2. Technology Stack & Rationale

| Layer | Technology | Version | Engineering Rationale |
| :--- | :--- | :--- | :--- |
| **Runtime** | Java LTS | `21` | Modern LTS baseline offering virtual threads, pattern matching, record types, and long-term production stability. |
| **Backend Framework** | Spring Boot | `3.5.16` | Latest stable 3.5.x patch release, managing compatible Spring Security 6.2.x and Hibernate 6.6.x dependencies. |
| **Database** | PostgreSQL | `16-alpine` | ACID-compliant relational storage with native `jsonb` support, UUID generation (`gen_random_uuid()`), and row-level trigger capabilities. |
| **ORM / Data Access** | Spring Data JPA / Hibernate | `6.6.x` | High-efficiency query generation, audited immutable entities, and relational mapping. |
| **Database Migrations** | Flyway | `10.21.x` | Version-controlled, idempotent SQL schema evolution (`V1`, `V2`, `V3`, `V4`). |
| **Spreadsheets & CSV** | Apache POI & Commons CSV | `5.4.1` / `1.14.1` | High-performance streaming Excel (.xlsx) and RFC 4180 CSV parsing and generation. |
| **Security & JWT** | Spring Security & JJWT | `6.2.x` / `0.12.6` | Stateless filter chain, HMAC-SHA256 token issuance, and granular method-level authorization (`@PreAuthorize`). |
| **In-Memory Cache** | Caffeine Cache | `3.1.8` | High-performance sub-millisecond cache for user permission stamps and instant revocation checks. |
| **API Documentation** | Springdoc OpenAPI | `2.8.5` | Automated OpenAPI 3.1 specification and interactive Swagger UI at `/swagger-ui.html`. |
| **Frontend Framework** | React & TypeScript | `18.3.1` / `5.6.3` | Strongly-typed component architecture ensuring zero runtime type regressions. |
| **Build & Bundling** | Vite | `6.4.5` | Sub-second hot module replacement (HMR) and optimized rollup production bundles. |
| **Styling** | Tailwind CSS | `3.4.17` | Utility-first CSS tailored with JECRC University brand palette and responsive design patterns. |

---

## 3. Directory Layout

```
jecrc-portal/
├── backend/
│   ├── pom.xml                          # Spring Boot 3.5.16, Java 21, POI 5.4.1, Commons CSV
│   └── src/
│       ├── main/
│       │   ├── java/edu/jecrc/portal/
│       │   │   ├── JecrcPortalApplication.java
│       │   │   ├── common/              # ApiResponse, PageResponse, ResourceNotFoundException
│       │   │   ├── config/              # SecurityConfig, CacheConfig, OpenApiConfig, BootstrapAdminRunner
│       │   │   └── modules/
│       │   │       ├── auth/            # AuthController, AuthService, JwtTokenProvider, RefreshToken
│       │   │       ├── user/            # UserController, UserService, User, Role, DepartmentScope
│       │   │       ├── audit/           # AuditController, AuditService, AuditLog (@Immutable)
│       │   │       ├── academic/        # Academic Registry, Student Records, Marks & Backlogs
│       │   │       ├── placement/       # Placement drives, policies, eligibility engine, applications
│       │   │       ├── interview/       # Interview rounds, candidate selection, evaluators, rubrics, feedback
│       │   │       └── offer/           # NEW IN MILESTONE 5:
│       │   │           ├── controller/  # OfferController, InternshipController, PpoController
│       │   │           ├── service/     # OfferService, InternshipService, PpoService
│       │   │           ├── repository/  # PlacementOfferRepository, OfferDecisionHistoryRepository,
│       │   │           │                # InstitutionalOfferPolicyRepository, InternshipRecordRepository,
│       │   │           │                # InternshipProgressLogRepository, InternshipEvaluationRepository, PpoRecordRepository
│       │   │           ├── entity/      # InstitutionalOfferPolicy, PlacementOffer, OfferDecisionHistory,
│       │   │           │                # InternshipRecord, InternshipProgressLog, InternshipEvaluation, PpoRecord
│       │   │           └── dto/         # Complete request/response DTO contracts
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       └── db/migration/
│       │           ├── V1__init_security_and_scopes_schema.sql
│       │           ├── V2__seed_roles_and_permissions.sql
│       │           ├── V3__academic_registry_schema.sql
│       │           ├── V4__seed_academic_master_data.sql
│       │           ├── V5__sanitize_demo_student_data.sql
│       │           ├── V6__placement_and_eligibility_schema.sql
│       │           ├── V7__interview_selection_and_feedback_schema.sql
│       │           └── V8__offers_internships_and_ppo_schema.sql
│       └── test/
│           └── java/edu/jecrc/portal/
│               ├── auth/AuthFlowIntegrationTest.java (3 tests)
│               ├── user/UserManagementSecurityTest.java (4 tests)
│               ├── academic/AcademicRegistryIntegrationTest.java (8 tests)
│               ├── placement/PlacementDriveAndEligibilityIntegrationTest.java (6 tests)
│               ├── interview/InterviewSelectionIntegrationTest.java (8 tests)
│               └── offer/OfferInternshipIntegrationTest.java (6 tests)
│               └── interview/InterviewSelectionIntegrationTest.java (6 tests)
│
├── frontend/
│   ├── package.json                     # React 18, Vite 6.4.5, Tailwind CSS
│   ├── vite.config.ts                   # Proxy configuration: /api -> http://localhost:8080
│   └── src/
│       ├── api/
│       │   ├── client.ts                # In-memory JWT Axios client with mutex refresh interceptor
│       │   ├── academic.ts              # Academic registry REST client
│       │   ├── placement.ts             # Placement & eligibility engine REST client
│       │   └── interview.ts             # Interview rounds, candidate progression, feedback REST client
│       ├── types/
│       │   ├── academic.ts              # Academic entities & imports
│       │   ├── placement.ts             # Companies, drives, job profiles, policies, applications
│       │   └── interview.ts             # Rounds, candidates, evaluations, appointments, questions, feedback
│       ├── pages/
│       │   ├── LoginPage.tsx            # Clean university login
│       │   ├── DashboardPage.tsx        # Role-customized dashboard overview
│       │   ├── UserManagementPage.tsx   # User directory & status toggling
│       │   ├── AuditLogsPage.tsx        # Append-only security audit trail inspector
│       │   ├── StudentsListPage.tsx     # Student search, filters, import & export
│       │   ├── StudentProfilePage.tsx   # 4-tab academic profile
│       │   ├── CompaniesPage.tsx        # Company directory & recruiter management
│       │   ├── DrivesListPage.tsx       # Placement drives list & creation modal
│       │   ├── DriveDetailPage.tsx      # Drive lifecycle, profiles & candidate evaluations
│       │   ├── PolicyManagementPage.tsx # Versioned eligibility criteria policy manager
│       │   ├── CandidateEvaluationsPage.tsx # TPO candidate evaluation & override console
│       │   ├── StudentOpportunitiesPage.tsx # Student recruitment drives & 1-click eligibility check
│       │   ├── MyApplicationsPage.tsx   # Student application tracker with withdrawal
│       │   ├── InterviewManagementPage.tsx # Round sequencing, evaluators & bulk candidate results
│       │   ├── EvaluatorConsolePage.tsx # Evaluator rubric scoring & recommendation console
│       │   ├── InterviewCalendarPage.tsx # Conflict-aware interview scheduling & appointment calendar
│       │   ├── QuestionBankPage.tsx     # Searchable technical & behavioral question repository
│       │   ├── InterviewFeedbackPage.tsx # Multi-tier recruitment experience feedback portal
│       │   └── MyInterviewsPage.tsx     # Student scheduled interview appointments & status tracker
│       └── components/layout/           # Navbar, Sidebar, Footer (Milestone 4 active navigation)
│
└── README.md
```

---

## 4. Implemented API Endpoints (Milestones 1 & 2)

### Academic Master Data (`/api/v1`)
- `GET /api/v1/departments`: List all academic departments.
- `GET /api/v1/programs`: List degree programs (`UG`, `PG`, `DIPLOMA`), optional `departmentId` filter.
- `GET /api/v1/academic-batches`: List student graduation cohorts, optional `programId` filter.

### Student Registry & Profiles (`/api/v1/students`)
- `GET /api/v1/students`: Paginated search with filters (`departmentId`, `programId`, `batchId`, `semester`, `verificationStatus`, `search`). Department-scoped for HOD/Faculty; self-only for Students.
- `POST /api/v1/students`: Register a new student profile (`STUDENT_PROFILE_MANAGE`).
- `GET /api/v1/students/me`: Retrieve authenticated student's own academic profile.
- `GET /api/v1/students/{studentId}`: Retrieve student profile by UUID. Scoped and protected against URL tampering.
- `PUT /api/v1/students/{studentId}`: Update student details.
- `GET /api/v1/students/export`: Export filtered student directory to Excel (.xlsx) with departmental scoping.

### Academic Qualifications & Semester Results (`/api/v1/students/{studentId}`)
- `GET /api/v1/students/{studentId}/qualifications`: Retrieve Class 10, Class 12, Diploma records.
- `PUT /api/v1/students/{studentId}/qualifications`: Upsert qualification records (preserves distinction between null and zero marks).
- `GET /api/v1/students/{studentId}/semester-results`: Retrieve semester grade sheets with subject attempts.
- `POST /api/v1/students/{studentId}/semester-results`: Record semester results and update CGPA and backlog metrics.
- `GET /api/v1/students/{studentId}/backlogs`: Retrieve subject backlog attempt history.
- `POST /api/v1/students/{studentId}/verify`: Officially verify or reject student academic records (`STUDENT_RECORD_VERIFY`).

### Bulk Ingestion & Templates (`/api/v1/students/import`)
- `POST /api/v1/students/import/preview`: Upload CSV or Excel file; validate row-by-row without making database changes.
- `POST /api/v1/students/import/commit`: Persist validated rows; skip or update unverified records; protect verified records from overwrite.
- `GET /api/v1/students/import/{importId}/errors`: Retrieve row-level validation errors.
- `GET /api/v1/students/import/template`: Download sample CSV template.

### Company Management (`/api/v1/companies`)
- `GET /api/v1/companies`: Paginated company directory with query & status filters. Recruiter contact details masked for students.
- `POST /api/v1/companies`: Register corporate partner with duplicate prevention (`COMPANY_MANAGE`).
- `GET /api/v1/companies/{companyId}`: Detailed company profile.
- `PUT /api/v1/companies/{companyId}`: Update company information (`COMPANY_MANAGE`).
- `DELETE /api/v1/companies/{companyId}`: Soft-delete / deactivate company profile (`COMPANY_MANAGE`).

### Versioned Eligibility Policies (`/api/v1/placement-policies`)
- `GET /api/v1/placement-policies`: Paginated list of policies with version numbers and criteria.
- `POST /api/v1/placement-policies`: Create new draft eligibility policy (`POLICY_MANAGE`).
- `GET /api/v1/placement-policies/{policyId}`: Retrieve policy details, eligible programs, and batches.
- `PUT /api/v1/placement-policies/{policyId}`: Update draft policy (`POLICY_MANAGE`).
- `POST /api/v1/placement-policies/{policyId}/publish`: Publish policy; locks policy from further modification (`POLICY_MANAGE`).
- `POST /api/v1/placement-policies/{policyId}/new-version`: Clone and iterate existing policy to create new draft version (`POLICY_MANAGE`).

### Placement Drives (`/api/v1/placement-drives`)
- `GET /api/v1/placement-drives`: Search recruitment drives with status, batch, and date filters (`DRIVE_VIEW`).
- `POST /api/v1/placement-drives`: Create new placement drive draft (`DRIVE_MANAGE`).
- `GET /api/v1/placement-drives/{driveId}`: Retrieve drive details, attached job profiles, and stats (`DRIVE_VIEW`).
- `PUT /api/v1/placement-drives/{driveId}`: Update drive metadata and deadlines (`DRIVE_MANAGE`).
- `POST /api/v1/placement-drives/{driveId}/status`: Advance drive lifecycle (`DRAFT`, `PUBLISHED`, `CLOSED`, `ARCHIVED`) (`DRIVE_MANAGE`).
- `POST /api/v1/placement-drives/{driveId}/job-profiles`: Add job profile with tiered compensation breakdown (`DRIVE_MANAGE`).

### Job Profiles & Evaluations (`/api/v1`)
- `GET /api/v1/job-profiles`: Search open job profiles and recruitment opportunities.
- `GET /api/v1/job-profiles/{profileId}`: Retrieve job profile details.
- `PUT /api/v1/job-profiles/{profileId}`: Update job profile parameters (`DRIVE_MANAGE`).
- `DELETE /api/v1/job-profiles/{profileId}`: Remove job profile (`DRIVE_MANAGE`).
- `POST /api/v1/job-profiles/{profileId}/evaluate`: Authenticated student instant server-side eligibility check.
- `POST /api/v1/job-profiles/{profileId}/evaluate-all`: TPO bulk candidate evaluation across targeted batches (`ELIGIBILITY_EVALUATE`).
- `GET /api/v1/job-profiles/{profileId}/evaluations`: List evaluated candidates with status filters (`ELIGIBILITY_EVALUATE`).

### Standalone Eligibility & Overrides (`/api/v1/eligibility-evaluations`)
- `GET /api/v1/eligibility-evaluations/{id}`: Detailed evaluation outcome and failure criteria breakdown.
- `POST /api/v1/eligibility-evaluations/{id}/override`: Record authorized TPO override with mandatory reason (`ELIGIBILITY_OVERRIDE`).
- `GET /api/v1/eligibility-evaluations/{id}/overrides`: Audit log of overrides for this evaluation.

### Student Job Applications (`/api/v1/job-applications`)
- `POST /api/v1/job-applications`: Submit job application with resume tag and atomic server double-check (`APPLICATION_SUBMIT`).
- `GET /api/v1/job-applications/my`: Student view of own applications, statuses, and timeline.
- `GET /api/v1/job-applications`: TPO administrative view of applications for a job profile (`APPLICATION_MANAGE`).
- `POST /api/v1/job-applications/{applicationId}/withdraw`: Candidate withdraws submission (`APPLICATION_SUBMIT`).
- `PUT /api/v1/job-applications/{applicationId}/status`: TPO updates applicant workflow state (`APPLICATION_MANAGE`).

### Interview Rounds & Evaluator Panels (`/api/v1`)
- `GET /api/v1/job-profiles/{profileId}/rounds`: List configured interview rounds with sequence ordering.
- `POST /api/v1/job-profiles/{profileId}/rounds`: Create interview round with sequence uniqueness (`INTERVIEW_ROUND_MANAGE`).
- `GET /api/v1/rounds/{roundId}`: Retrieve round parameters, rubrics, and instructions.
- `PUT /api/v1/rounds/{roundId}`: Update round configuration parameters (`INTERVIEW_ROUND_MANAGE`).
- `POST /api/v1/rounds/{roundId}/publish-results`: Atomic publication of candidate results and synchronization of selected applicants (`INTERVIEW_ROUND_MANAGE`).
- `GET /api/v1/rounds/{roundId}/evaluators`: List assigned interview panel members.
- `POST /api/v1/rounds/{roundId}/evaluators`: Assign faculty/staff evaluator with role or designation (`INTERVIEW_ROUND_MANAGE`).
- `DELETE /api/v1/rounds/{roundId}/evaluators/{evaluatorId}`: Remove evaluator from panel (`INTERVIEW_ROUND_MANAGE`).
- `GET /api/v1/evaluators/me/rounds`: List interview rounds assigned to the authenticated evaluator.

### Candidate Selection & Roster Progression (`/api/v1`)
- `GET /api/v1/rounds/{roundId}/candidates`: Retrieve candidate roster (isolated by evaluator assignment).
- `POST /api/v1/rounds/{roundId}/candidates/bulk-upload`: Bulk result upload with comprehensive row-level error reporting (`INTERVIEW_ROUND_MANAGE`).
- `PUT /api/v1/round-candidates/{candidateId}/status`: Update candidate score, remarks, and round status (`INTERVIEW_ROUND_MANAGE`).
- `POST /api/v1/round-candidates/{candidateId}/advance`: Advance shortlisted candidate to the next sequential round (`INTERVIEW_ROUND_MANAGE`).

### Candidate Evaluations & Rubrics (`/api/v1/round-candidates/{candidateId}/evaluations`)
- `POST /api/v1/round-candidates/{candidateId}/evaluations`: Submit or draft rubric evaluation with recommendation decision (`ROUND_EVALUATION_SUBMIT`).
- `GET /api/v1/round-candidates/{candidateId}/evaluations`: Retrieve evaluations for candidate (confidential staff notes restricted).

### Interview Appointments & Conflict Detection (`/api/v1/appointments`)
- `POST /api/v1/appointments`: Schedule candidate interview slot with real-time conflict prevention (`INTERVIEW_SCHEDULE_MANAGE`).
- `PUT /api/v1/appointments/{appointmentId}`: Reschedule appointment or record status update (`INTERVIEW_SCHEDULE_MANAGE`).
- `GET /api/v1/appointments/conflicts`: Real-time cross-company conflict check for student and evaluator schedules (`INTERVIEW_SCHEDULE_MANAGE`).
- `GET /api/v1/appointments/me`: Student view of upcoming interview appointments, venues, and video links.
- `GET /api/v1/round-candidates/{candidateId}/appointments`: View appointments associated with a round candidate.

### Question Bank Repository (`/api/v1/interview-questions`)
- `GET /api/v1/interview-questions`: Search categorized questions with category, difficulty, type, and company filters.
- `POST /api/v1/interview-questions`: Create question with rubric, sample I/O, and confidential interviewer notes (`QUESTION_BANK_MANAGE`).
- `PUT /api/v1/interview-questions/{questionId}`: Update question parameters (`QUESTION_BANK_MANAGE`).

### Recruitment Feedback Workflows (`/api/v1`)
- `POST /api/v1/interview-feedback`: Submit multi-tier feedback (student-to-drive, evaluator-to-candidate, TPO-to-company) (`INTERVIEW_FEEDBACK_SUBMIT`).
- `GET /api/v1/applications/{applicationId}/feedback`: View feedback associated with an application.
- `GET /api/v1/interview-feedback`: Search and aggregate recruitment feedback entries (`INTERVIEW_FEEDBACK_VIEW`).

### Public Portal & Overview (`/api/v1/public`)
- `GET /api/v1/public/overview`: Publicly accessible university overview, accredited placement highlights, highest/average packages, partner directory, and academic schools without requiring authentication.

### Unified Role-Specific Dashboards (`/api/v1/dashboards`)
- `GET /api/v1/dashboards/student`: Student-specific live placement status, CGPA, backlogs, attendance, shortlist count, and issued offers (`STUDENT`).
- `GET /api/v1/dashboards/tpo`: TPO administrative overview, active drives, total offers, unique placed students, CTC tier distribution, top hiring partners (`TPO_ADMIN`, `TPO_COORDINATOR`, `SUPER_ADMIN`).
- `GET /api/v1/dashboards/faculty`: Teaching allocations, sessions conducted today, intern mentorships, pending log reviews, and weekly timetable (`FACULTY`, `HOD`, `SUPER_ADMIN`).
- `GET /api/v1/dashboards/hod`: Department-scoped metrics (verified students, department placement rate, average attendance, attendance shortage watchlist, recent department offers) (`HOD`, `SUPER_ADMIN`).
- `GET /api/v1/dashboards/director`: Institutional executive console with global enrollment, verified students, institutional placement rate, median/highest compensation, department audit breakdown, and live security audit logs (`DIRECTOR`, `SUPER_ADMIN`).

### Placement Analytics & Compensation Engine (`/api/v1/analytics`)
- `GET /api/v1/analytics/placements`: Institutional placement intelligence distinguishing selected candidates from issued offers, calculating unique placed students to prevent double-counting, computing audited compensation percentiles (highest, average, median / 50th percentile, lowest), tier breakdown, and policy disclosures (`ANALYTICS_VIEW`, `TPO_ADMIN`, `DIRECTOR`, `SUPER_ADMIN`).

---

## 5. Local Development & Setup Guide

### 5.1 Prerequisites
- **Java 21 LTS** (`java -version`).
- **Apache Maven 3.9+** (`mvn -v`).
- **Node.js 20+** and **npm 10+** (`node -v`, `npm -v`).
- **Docker Desktop** running (for PostgreSQL 16 container).

### 5.2 Start PostgreSQL Database
```powershell
docker run --name jecrc_portal_db `
  -e POSTGRES_DB=jecrc_portal `
  -e POSTGRES_USER=postgres `
  -e POSTGRES_PASSWORD=postgres `
  -p 5436:5432 -d postgres:16-alpine
```

### 5.3 Launch the Spring Boot Backend
```powershell
cd c:\Users\div18\Desktop\TPO\jecrc-portal\backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
*Flyway will execute `V1` through `V10` migrations automatically. Swagger UI will be accessible at: `http://localhost:8080/swagger-ui.html`*

### 5.4 Launch the Frontend Application
```powershell
cd c:\Users\div18\Desktop\TPO\jecrc-portal\frontend
npm run dev
```
*Web application available at: `http://localhost:5173`*

---

## 6. Automated Verification & Test Results

The backend contains **57 Comprehensive Integration & Security Tests** covering all 7 milestones: security core, identity, academic registry, placement drives, interview rounds, offers/internships/PPO tracking, academic operations/attendance/notifications, and unified dashboards/analytics/QA.

To run the complete test suite:
```powershell
cd c:\Users\div18\Desktop\TPO\jecrc-portal\backend
mvn clean test
```

### Verified Test Cases (57/57 Passing):
1. `AuthIntegrationTest.testSuccessfulLoginAndMeProfile`: Authenticates super admin, validates JWT signature and profile retrieval.
2. `AuthIntegrationTest.testInvalidCredentialsReturn401`: Verifies rejected logins and failed auth audit records.
3. `RefreshTokenRotationTest.testSuccessfulRefreshTokenRotation`: Tests single-use token rotation and cookie reassignment.
4. `RefreshTokenRotationTest.testTokenReplayTriggersBreachRevocation`: Replays a revoked token; verifies the entire token family is revoked and returns 401.
5. `InstantRevocationIntegrationTest.testSuspendedUserInstantlyDeniedEvenWithValidJwt`: Proves 0 ms revocation when user status becomes `SUSPENDED`.
6. `UserManagementSecurityTest.testAdminCanCreateAndListUsers`: Validates user creation, role assignment, and pagination.
7. `UserManagementSecurityTest.testNonAdminCannotManageUsers`: Confirms students/faculty receive 403 Forbidden on administrative endpoints.
8. `UserManagementSecurityTest.testPasswordResetFlow`: Validates forgot-password token issuance and password update completion.
9. `AcademicRegistryIntegrationTest.testStudentCreationAndDuplicatePrevention`: Validates student creation and uniqueness constraints on Roll Number and Student ID.
10. `AcademicRegistryIntegrationTest.testQualificationValidationAndMissingMarksDistinctFromZero`: Ensures null marks are preserved as NULL and not coerced to 0.0.
11. `AcademicRegistryIntegrationTest.testSemesterResultAndBacklogCalculations`: Tests recording semester results, creating backlogs, and recalculating active vs. historical metrics.
12. `AcademicRegistryIntegrationTest.testStudentAccessRestrictedToOwnRecord`: Confirms students can access `/me`, but URL tampering with another student's UUID returns 403 Forbidden.
13. `AcademicRegistryIntegrationTest.testHodDepartmentLevelAccessAndScoping`: Confirms HOD CSE can access CSE students, but access to ECE students returns 403 Forbidden.
14. `AcademicRegistryIntegrationTest.testAuthorizedVerificationAndDeniedVerification`: Verifies student cannot verify records (403), while authorized HOD/admin can verify records.
15. `AcademicRegistryIntegrationTest.testImportPreviewCausesNoWrites`: Uploads CSV file in preview mode and verifies no records are written to the database.
16. `AcademicRegistryIntegrationTest.testImportCommitProtectsVerifiedRecord`: Verifies that committing an import never overwrites already verified student records.
17. `AcademicRegistryIntegrationTest.testExportAuthorizationAndDepartmentScoping`: Confirms students cannot export (403), while authorized staff receive scoped Excel files.
18. `PlacementDriveAndEligibilityIntegrationTest.testCompanyCreationAndDuplicatePrevention`: Tests company creation, pagination, duplicate prevention, and recruiter masking.
19. `PlacementDriveAndEligibilityIntegrationTest.testPlacementPolicyVersionIteration`: Tests immutable published policies and clone-based version iteration (`new-version`).
20. `PlacementDriveAndEligibilityIntegrationTest.testPlacementDriveLifecycle`: Validates drive transitions `DRAFT` $\to$ `PUBLISHED` $\to$ `CLOSED` $\to$ `ARCHIVED` and profile count validation.
21. `PlacementDriveAndEligibilityIntegrationTest.testEligibilityEvaluationOutcomes`: Evaluates students against strict criteria (`ELIGIBLE`, `INELIGIBLE`, and `NEEDS_VERIFICATION`).
22. `PlacementDriveAndEligibilityIntegrationTest.testStudentApplicationSubmissionAndDuplicateCheck`: Validates atomic double-check on submission, resume tag, and duplicate rejection.
23. `PlacementDriveAndEligibilityIntegrationTest.testTpoEligibilityOverride`: Validates authorized TPO overrides with mandatory reason and immutable audit log.
24. `InterviewSelectionIntegrationTest.testRoundConfigurationAndSequenceValidation`: Validates interview round sequence uniqueness per job profile and role authorization.
25. `InterviewSelectionIntegrationTest.testEvaluatorAssignmentAndAccessControl`: Validates panel assignments and evaluator access isolation (unassigned rounds return 403 Forbidden).
26. `InterviewSelectionIntegrationTest.testCandidateProgressionAndResultPublishing`: Validates bulk result upload with row validation, advancement, and atomic status synchronization on final selection.
27. `InterviewSelectionIntegrationTest.testInterviewSchedulingAndConflictDetection`: Validates appointment booking with student and evaluator cross-company conflict detection.
28. `InterviewSelectionIntegrationTest.testQuestionBankCreationAndConfidentialMasking`: Validates question repository search, tag filtering, and confidential interviewer note masking for students.
29. `InterviewSelectionIntegrationTest.testFeedbackSubmissionAndAccessRestrictions`: Validates multi-tier feedback workflow and staff moderation access control.
30. `OfferInternshipIntegrationTest.testOfferCreationAndIssuanceValidation`: Validates offer draft creation only for selected candidates, formal issuance with decision deadline, and unauthorized rejection.
31. `OfferInternshipIntegrationTest.testStudentOfferDecisionAndAuditHistory`: Validates student acceptance/decline workflow, deadline enforcement, and immutable append-only audit trail in `offer_decision_history`.
32. `OfferInternshipIntegrationTest.testMultipleOfferPolicyAndDreamTierEnforcement`: Validates institutional multiple-offer policies, preventing multiple sub-dream offers and permitting Dream Tier ($\ge 1.5\times$ CTC) upgrades.
33. `OfferInternshipIntegrationTest.testInternshipRegistrationAndMentorAssignment`: Validates internship registration, dual mentorship assignment (faculty + industry), and mentee access isolation.
34. `OfferInternshipIntegrationTest.testInternshipProgressLogsAndConfidentialEvaluationMasking`: Validates bi-weekly journal submissions, mentor review workflow, and **strict confidential staff note masking** from student views.
35. `OfferInternshipIntegrationTest.testPpoRecommendationApprovalAndOfferConversion`: Validates PPO review recommendation, evaluation score validation, authorized approval, and direct conversion into formal placement offers.
36. `AcademicOperationsIntegrationTest.testTimetableGistExclusionConstraints`: Validates PostgreSQL GiST exclusion constraints (`excl_no_classroom_overlap`, `excl_no_faculty_overlap`, `excl_no_section_overlap`) on half-open time ranges `[start, end)` rejecting overlaps race-free while allowing touching adjacent slots.
37. `AcademicOperationsIntegrationTest.testAttendanceWorkflowAndGeotagging`: Validates session launcher, pre-populated student roster, server-side Haversine geofence distance calculation, session locking upon finalization, and formal correction audit trail with mandatory justification.
38. `AcademicOperationsIntegrationTest.testAttendanceShortageAndAnalytics`: Validates student personal attendance summary (`/me`), subject-wise percentage calculations, and cohort shortage warning rosters calculating exact recovery classes needed to reach 75%.
39. `AcademicOperationsIntegrationTest.testCrtProgramAndEnrollment`: Validates dedicated Campus Recruitment Training tracks separate from regular classroom attendance, session scheduling, student registration, and trainer rubric scoring.
40. `AcademicOperationsIntegrationTest.testEventsLifecycleAndRegistration`: Validates campus events and workshops lifecycle (`DRAFT` $\to$ `PUBLISHED`), audience targeting, and student 1-click registration.
41. `AcademicOperationsIntegrationTest.testNotificationsAndTransactionalOutbox`: Validates in-app notification center, unread counter synchronization, mark-as-read workflows, and transactional outbox persistence ensuring resilient asynchronous dispatch.
42. `Milestone7UnifiedDashboardAndIntegrationTest.testPublicOverviewEndpointOpenWithoutAuth`: Validates `/api/v1/public/overview` serves official JECRC institutional data without credentials.
43. `Milestone7UnifiedDashboardAndIntegrationTest.testStudentDashboardViewScopedToOwnProfile`: Verifies student dashboard aggregates personal verification status, CGPA, backlogs, attendance, applications, and offers.
44. `Milestone7UnifiedDashboardAndIntegrationTest.testTpoDashboardAggregatedMetricsAndActiveDrives`: Verifies TPO dashboard aggregates recruiter counts, active drives, total offers, unique placed count, and tier distribution.
45. `Milestone7UnifiedDashboardAndIntegrationTest.testFacultyDashboardAssignedClassesAndInterns`: Verifies faculty dashboard returns timetable teaching allocations, lectures conducted, and assigned intern mentees.
46. `Milestone7UnifiedDashboardAndIntegrationTest.testHodDashboardDepartmentScopedMetrics`: Confirms HOD CSE dashboard aggregates CSE students, department placement percentage, and attendance shortage watchlist.
47. `Milestone7UnifiedDashboardAndIntegrationTest.testHodDashboardRejectsOutOfDepartmentAccess`: Proves HOD cannot query departments outside assigned institutional scope (`403 Forbidden`).
48. `Milestone7UnifiedDashboardAndIntegrationTest.testDirectorDashboardExecutiveOverview`: Confirms Director console returns holistic university-wide enrollment, verification metrics, placement rates, and department comparison audit table.
49. `Milestone7UnifiedDashboardAndIntegrationTest.testDirectorDashboardAccessDeniedForNonDirector`: Verifies students and standard faculty cannot access executive Director dashboard (`403 Forbidden`).
50. `Milestone7UnifiedDashboardAndIntegrationTest.testPlacementAnalyticsDeDuplication`: Verifies de-duplicated counting distinguishing selected candidates from offers issued, and preventing double-counting placed students holding multiple offers.
51. `Milestone7UnifiedDashboardAndIntegrationTest.testPlacementAnalyticsCompensationPercentiles`: Verifies audited compensation percentiles (highest, average, median, lowest) computed exclusively over accepted placement offers.
52. `Milestone7UnifiedDashboardAndIntegrationTest.testPlacementAnalyticsTierBreakdown`: Verifies categorization into `REGULAR` ($< 10\text{ LPA}$), `DREAM` ($10\text{--}25\text{ LPA}$), and `SUPER_DREAM` ($\ge 25\text{ LPA}$).
53. `Milestone7UnifiedDashboardAndIntegrationTest.testDirectorHasAuditAndReportPermissionsInV10`: Confirms `DIRECTOR` role successfully inherits `AUDIT_LOG_VIEW`, `ATTENDANCE_REPORT_VIEW`, and `ANALYTICS_VIEW` mapped in `V10`.
54. `Milestone7UnifiedDashboardAndIntegrationTest.testTpoCoordinatorHasDrivePermissionsInV10`: Confirms `TPO_COORDINATOR` role successfully inherits `DRIVE_VIEW`, `DRIVE_MANAGE`, and `OFFER_ISSUE` mapped in `V10`.
55. `Milestone7UnifiedDashboardAndIntegrationTest.testCrossModuleStudentProfileToOfferFunnel`: Verifies full end-to-end integration: Student Enrollment $\to$ HOD Verification $\to$ Drive Eligibility $\to$ Application $\to$ Interview Selection $\to$ Offer Acceptance.
56. `Milestone7UnifiedDashboardAndIntegrationTest.testCrossModuleAttendanceToShortageDetection`: Verifies end-to-end integration: Session Creation $\to$ Absent Marking $\to$ Finalization $\to$ HOD/Student Shortage Alert Generation.
57. `Milestone7UnifiedDashboardAndIntegrationTest.testCrossModuleCrtToMockInterviewReadiness`: Verifies end-to-end integration: CRT Session Registration $\to$ Attendance Marking $\to$ Trainer Feedback $\to$ Student Readiness Verification.

---

## 7. Release-Readiness Hardening & Production Operations

### Official Institutional Disclosures & Verified Statistics (Batch 2026)
Public portal pages ([`PublicHomePage.tsx`](file:///frontend/src/pages/public/PublicHomePage.tsx), [`PublicPlacementSupportPage.tsx`](file:///frontend/src/pages/public/PublicPlacementSupportPage.tsx), and [`PublicPortalController.java`](file:///backend/src/main/java/edu/jecrc/portal/modules/dashboard/controller/PublicPortalController.java)) reflect official, verified institutional data sourced directly from [JECRC University Placement Records](https://jecrcuniversity.edu.in/placements/):
- **Academic Reporting Period:** 2025–26 Academic Year (figures reported through 31 March 2026 for Batch 2026).
- **On-Campus Offers:** 2,104+ on-campus offers for graduates of batch 2026.
- **Highest Package:** ₹33 LPA.
- **Average Package:** ₹6 LPA.
- **Top 30% Average:** ₹10.70 LPA average package for the top 30% of offers.
- **Corporate Recruiter Partnerships:** 205 top corporate partners (200+ leading corporates).
- **Fortune 500 Placement Footprint:** 856 offers from Fortune 500 corporate partners.
- **Official Institutional Contacts:** Verified against [JECRC University Contact Information](https://jecrcuniversity.edu.in/contact-us/):
  - **Toll-Free Helpline:** `1800-120-5616`
  - **Admissions Office:** `admission@jecrcu.edu.in`
  - **Training & Placement Office:** `tpo@jecrcu.edu.in`
  - **Official Campus Address:** *JECRC University, Plot No. IS-2036 to IS-2039, Ramchandrapura Industrial Area, Vidhani, Sitapura Extension, Jaipur – 303905, Rajasthan, India*.

### Offer Policy Governance & Synthetic Baselines
- Development baselines (e.g. ₹10 LPA / ₹25 LPA tier boundaries or sample multiplier defaults) are marked with explicit regulatory governance disclaimers in [`MyOffersPage.tsx`](file:///frontend/src/pages/MyOffersPage.tsx) and [`OfferManagementPage.tsx`](file:///frontend/src/pages/OfferManagementPage.tsx).
- All institutional policies remain dynamically configurable by authorized TPO administrators in the Placement Policy Engine rather than hardcoded university edicts.
- Public support documentation outlines the three recognized institutional placement tracks:
  1. **Category A:** Core Engineering, IT Services & Management Consulting.
  2. **Category B:** High-Growth Product Engineering, Systems Architecture & FinTech.
  3. **Category C:** Elite Tech R&D, AI Research & DeepTech Specializations.

### Geofence Configuration & Privacy
- Invented GPS coordinates and synthetic travel distance estimates have been removed from public contact pages.
- Classroom attendance geofencing uses administratively configurable lat/lon/radius coordinates with clear development sandbox annotations, preserving historical attendance records without leaking synthetic hardware coordinates.

### Frontend Bundle Optimization (Code-Splitting)
- **Monolithic Single-Chunk Problem Solved:** The original monolithic frontend bundle generated a warning with `index-Bf6tY-qF.js` exceeding 829.49 kB.
- **Solution:** Configured `React.lazy()` dynamic route splitting across all 38 application routes in [`App.tsx`](file:///frontend/src/App.tsx) and chunk division in [`vite.config.ts`](file:///frontend/vite.config.ts).
- **Bundle Metrics:**
  - `vendor.js` (React 18.3, React Router 6.22): ~165.6 kB (53.3 kB gzip).
  - `http.js` (Axios, Lucide React icons): ~51.2 kB (16.6 kB gzip).
  - `ui.js` (Tailwind UI helpers, ClassNames): ~28.0 kB (7.3 kB gzip).
  - `index.js` (Core Application Shell & Context): ~41.9 kB (13.5 kB gzip).
  - **Route Chunks:** 38 lazy-loaded modules ranging from 1.2 kB to 28.6 kB each.
  - **Public First Load:** ~245 kB total uncompressed (~76 kB gzipped) — a **>70% reduction** in initial payload.

### Verified Framework Dependency Hierarchy & Evaluation

Executing Maven dependency tree inspection (`mvn dependency:tree`) on the active production project verifies the exact resolved dependency hierarchy:
- **Java Runtime:** OpenJDK / Eclipse Temurin 21 LTS (`target: 21`)
- **Spring Boot:** `3.5.16`
- **Spring Framework:** `6.2.19` (Core, Web, WebMVC, AOP, JDBC, ORM, TX, Context)
- **Spring Security:** `6.5.11` (Config, Web, Core, Crypto, Test — *corrected from prior inadvertent 6.2.x note*)
- **Hibernate ORM:** `6.6.53.Final` (`hibernate-core`, Jakarta Persistence 3.1)
- **Hibernate Validator:** `8.0.3.Final` (Jakarta Validation 3.0.2)
- **Database Migrations:** Flyway `11.7.2` (`flyway-core` + `flyway-database-postgresql`)
- **Database Driver:** PostgreSQL JDBC Driver `42.7.11`
- **API Documentation:** Springdoc OpenAPI Starter WebMVC UI `2.8.5` (Swagger UI `5.18.3`, Swagger Models `2.2.28`)
- **Servlet Container:** Apache Tomcat Embed `10.1.55`
- **Testing Stack:** JUnit Jupiter `5.12.2`, Mockito `5.17.0`, AssertJ `3.27.7`, Testcontainers `1.21.4`

#### Spring Boot 4.1.x Evaluation & Upgrade Analysis
During the release-readiness evaluation, upgrading to the announced `spring-boot-starter-parent:4.1.1` was tested. The evaluation identified the following breaking architectural shifts:
1. **Spring Security 7.1.1 Upgrade:** Spring Boot 4.1.1 adopts Spring Security 7.x, which removes the zero-argument constructor `new DaoAuthenticationProvider()` and removes `setUserDetailsService(...)` in favor of mandatory constructor injection `DaoAuthenticationProvider(UserDetailsService)`.
2. **Test Infrastructure Modularization:** Spring Boot 4.x test packages split `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc` into separate packages/starters, and upgraded the test runner to JUnit 6 (`org.junit.jupiter:6.0.3`).
3. **Testcontainers BOM Changes:** The 4.1.1 BOM altered Testcontainers version management (`junit-jupiter` and `postgresql` module versions were not automatically inherited).

Per project instructions to maintain a fully tested, consistent baseline without partial upgrades, the backend retains the rock-solid, production-tested **Spring Boot 3.5.16 / Spring Security 6.5.11 / Hibernate 6.6.53** configuration with 100% test pass fidelity (57/57 tests passing).

---

### Production Environment Runbook

#### 1. Managed PostgreSQL
1. Create a PostgreSQL 15+ database on Railway, Supabase, Neon, or AWS RDS.
2. Ensure network connectivity and record the JDBC connection URL:
   ```bash
   jdbc:postgresql://<HOST>:<PORT>/<DATABASE>?sslmode=require
   ```
3. Flyway migrations (`V1` through `V10`) will execute automatically on application startup.

#### 2. Backend (Railway / Containerized Java 21)
1. Deploy using Java 21 LTS runtime.
2. Supply environment variables (refer to `.env.example`):
   ```bash
   SPRING_PROFILES_ACTIVE=prod
   SPRING_DATASOURCE_URL=jdbc:postgresql://<DB_HOST>:<DB_PORT>/<DB_NAME>?sslmode=require
   SPRING_DATASOURCE_USERNAME=<DB_USER>
   SPRING_DATASOURCE_PASSWORD=<DB_PASSWORD>
   APP_BOOTSTRAP_SEED_DEMO_USERS=false
   APP_BOOTSTRAP_ADMIN_EMAIL=tpo.admin@jecrcu.edu.in
   APP_BOOTSTRAP_ADMIN_PASSWORD=<STRONG_GENERATED_PASSWORD>
   APP_SECURITY_JWT_SECRET=<STRONG_64_CHAR_HEX_OR_BASE64_SECRET>
   APP_CORS_ALLOWED_ORIGINS=https://your-portal.vercel.app
   ```
3. Note: `APP_BOOTSTRAP_SEED_DEMO_USERS=false` guarantees no test or synthetic user accounts are seeded into the production environment.

#### 3. Frontend (Vercel Same-Origin Proxy Design)
1. Link the `frontend/` directory to Vercel.
2. Build command: `npm run build`
3. Output directory: `dist`
4. **Same-Origin API Proxy Architecture (`frontend/vercel.json`):**
   The browser communicates exclusively with the Vercel domain (`/api/v1/...`). Vercel proxies all `/api/*` requests directly to the Railway backend. This avoids cross-origin third-party cookie blocking and ensures `HttpOnly; SameSite=Lax; Secure` refresh cookies and CSRF protections operate identically in both local development and production.
   Configure `vercel.json` with the external rewrite before the SPA catch-all fallback:
   ```json
   {
     "rewrites": [
       {
         "source": "/api/:path*",
         "destination": "https://<your-backend-railway-app>.up.railway.app/api/:path*"
       },
       {
         "source": "/(.*)",
         "destination": "/index.html"
       }
     ]
   }
   ```
   *Guaranteed Behavior:* Because `/api/:path*` is matched first, API requests are never routed to `/index.html`. Furthermore, all frontend API endpoints in the codebase consistently use the `/api/v1/...` prefix without hardcoded hostnames, eliminating path duplication.
