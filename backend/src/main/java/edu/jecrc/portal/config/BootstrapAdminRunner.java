package edu.jecrc.portal.config;

import edu.jecrc.portal.modules.academic.entity.*;
import edu.jecrc.portal.modules.academic.repository.*;
import edu.jecrc.portal.modules.user.entity.Department;
import edu.jecrc.portal.modules.user.entity.Role;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.entity.UserDepartmentScope;
import edu.jecrc.portal.modules.user.repository.DepartmentRepository;
import edu.jecrc.portal.modules.user.repository.RoleRepository;
import edu.jecrc.portal.modules.user.repository.UserDepartmentScopeRepository;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapAdminRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserDepartmentScopeRepository departmentScopeRepository;
    private final PasswordEncoder passwordEncoder;

    // Academic repositories for dev-only demo seeding
    private final AcademicProgramRepository programRepository;
    private final AcademicBatchRepository batchRepository;
    private final StudentRepository studentRepository;
    private final StudentQualificationRepository qualificationRepository;
    private final SemesterResultRepository semesterResultRepository;
    private final SubjectAttemptRepository subjectAttemptRepository;

    @Value("${app.bootstrap.admin-email:admin@jecrcuniversity.edu.in}")
    private String adminEmail;

    @Value("${app.bootstrap.admin-password:Admin@JECRC2026!}")
    private String adminPassword;

    @Value("${app.bootstrap.seed-demo-users:false}")
    private boolean seedDemoUsers;

    @Value("${app.bootstrap.demo-password:}")
    private String demoPassword;

    @Override
    @Transactional
    public void run(String... args) {
        bootstrapSuperAdmin();
        if (seedDemoUsers) {
            log.info("Development profile active: seeding demo user accounts and synthetic student profiles");
            bootstrapDemoAccounts();
            bootstrapDemoStudents();
        } else {
            log.info("Production mode: demo account and student seeding is disabled");
        }
    }

    private void bootstrapSuperAdmin() {
        User admin = userRepository.findByEmailIgnoreCase(adminEmail).orElse(null);
        if (admin == null) {
            Role superAdminRole = roleRepository.findByRoleName("SUPER_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role not seeded"));

            admin = User.builder()
                    .email(adminEmail.toLowerCase().trim())
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .firstName("Super")
                    .lastName("Administrator")
                    .phoneNumber("+91-141-2770232")
                    .isActive(true)
                    .isSuspended(false)
                    .securityStamp(UUID.randomUUID())
                    .roles(Set.of(superAdminRole))
                    .build();

            userRepository.save(admin);
            log.info("Initialized default Super Administrator account: {}", adminEmail);
        } else {
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setIsActive(true);
            admin.setIsSuspended(false);
            userRepository.save(admin);
            log.info("Synchronized password for Super Administrator account: {}", adminEmail);
        }
    }

    private void bootstrapDemoAccounts() {
        Department cseDept = departmentRepository.findByDepartmentCode("CSE").orElse(null);

        // 1. TPO Admin
        createDemoUserIfNotExists("tpo.admin@jecrcuniversity.edu.in", "TPO", "Head", "TPO_ADMIN", null);
        // 2. TPO Coordinator & Officer
        createDemoUserIfNotExists("tpo.coord@jecrcuniversity.edu.in", "Placement", "Coordinator", "TPO_COORDINATOR", null);
        createDemoUserIfNotExists("tpo.officer@jecrcuniversity.edu.in", "Placement", "Officer", "TPO_COORDINATOR", null);
        // 3. Director
        createDemoUserIfNotExists("director@jecrcuniversity.edu.in", "University", "Director", "DIRECTOR", null);
        // 4. HOD CSE
        createDemoUserIfNotExists("hod.cse@jecrcuniversity.edu.in", "Head", "CSE", "HOD", cseDept);
        // 5. Faculty CSE
        createDemoUserIfNotExists("faculty.cse@jecrcuniversity.edu.in", "Professor", "Sharma", "FACULTY", cseDept);
        // 6. Student
        createDemoUserIfNotExists("student.cse@jecrcuniversity.edu.in", "Aarav", "Mehta", "STUDENT", cseDept);
    }

    private void bootstrapDemoStudents() {
        Department cseDept = departmentRepository.findByDepartmentCode("CSE").orElse(null);
        Department eceDept = departmentRepository.findByDepartmentCode("ECE").orElse(null);
        AcademicProgram cseProg = programRepository.findByProgramCode("BTECH_CSE").orElse(null);
        AcademicProgram eceProg = programRepository.findByProgramCode("BTECH_ECE").orElse(null);

        if (cseDept == null || cseProg == null) {
            log.warn("Cannot seed demo students: CSE department or program not found");
            return;
        }

        AcademicBatch cseBatch26 = batchRepository.findByProgramIdAndGraduationYear(cseProg.getId(), 2026).orElse(null);
        AcademicBatch eceBatch26 = (eceProg != null)
                ? batchRepository.findByProgramIdAndGraduationYear(eceProg.getId(), 2026).orElse(null)
                : null;

        User studentUser = userRepository.findByEmailIgnoreCase("student.cse@jecrcuniversity.edu.in").orElse(null);

        // Student 1: Aarav Mehta (22JU0101, 22BCSE101)
        if (studentRepository.findByUniversityStudentId("22JU0101").isEmpty()) {
            Student s1 = Student.builder()
                    .universityStudentId("22JU0101")
                    .rollNumber("22BCSE101")
                    .registrationNumber("JU/2022/CSE/101")
                    .firstName("Aarav")
                    .lastName("Mehta")
                    .universityEmail("student.cse@jecrcuniversity.edu.in")
                    .personalEmail("aarav.mehta.demo@gmail.com")
                    .phoneNumber("+91-9876543210")
                    .dateOfBirth(LocalDate.of(2004, 5, 15))
                    .gender("Male")
                    .program(cseProg)
                    .department(cseDept)
                    .batch(cseBatch26)
                    .currentSemester(6)
                    .section("A")
                    .academicStatus("ENROLLED")
                    .verificationStatus("VERIFIED")
                    .cgpa(new BigDecimal("8.42"))
                    .activeBacklogs(0)
                    .historicalBacklogs(1)
                    .totalCreditsEarned(new BigDecimal("108.0"))
                    .user(studentUser)
                    .build();
            s1 = studentRepository.save(s1);

            // Qualifications
            qualificationRepository.save(StudentQualification.builder()
                    .student(s1)
                    .qualificationType("CLASS_10")
                    .boardOrUniversity("CBSE")
                    .institutionName("Delhi Public School, Jaipur")
                    .passingYear(2020)
                    .markingScheme("PERCENTAGE")
                    .percentageOrCgpa(new BigDecimal("92.40"))
                    .verificationStatus("VERIFIED")
                    .build());

            qualificationRepository.save(StudentQualification.builder()
                    .student(s1)
                    .qualificationType("CLASS_12")
                    .boardOrUniversity("CBSE")
                    .institutionName("Delhi Public School, Jaipur")
                    .passingYear(2022)
                    .markingScheme("PERCENTAGE")
                    .percentageOrCgpa(new BigDecimal("89.60"))
                    .verificationStatus("VERIFIED")
                    .build());

            // Semester Results
            SemesterResult r1 = semesterResultRepository.save(SemesterResult.builder()
                    .student(s1)
                    .semesterNumber(1)
                    .sgpa(new BigDecimal("8.20"))
                    .creditsRegistered(new BigDecimal("22.0"))
                    .creditsEarned(new BigDecimal("22.0"))
                    .resultStatus("PASSED")
                    .hasActiveBacklog(false)
                    .verificationStatus("VERIFIED")
                    .build());

            subjectAttemptRepository.save(SubjectAttempt.builder()
                    .result(r1).student(s1).subjectCode("CS101").subjectName("Programming in C")
                    .semesterNumber(1).attemptNumber(1).credits(new BigDecimal("4.0")).grade("A+")
                    .marksObtained(new BigDecimal("88.0")).maxMarks(new BigDecimal("100.0"))
                    .isBacklog(false).isCleared(true).build());

            SemesterResult r2 = semesterResultRepository.save(SemesterResult.builder()
                    .student(s1)
                    .semesterNumber(2)
                    .sgpa(new BigDecimal("7.80"))
                    .creditsRegistered(new BigDecimal("22.0"))
                    .creditsEarned(new BigDecimal("22.0"))
                    .resultStatus("PASSED")
                    .hasActiveBacklog(false)
                    .verificationStatus("VERIFIED")
                    .build());

            subjectAttemptRepository.save(SubjectAttempt.builder()
                    .result(r2).student(s1).subjectCode("CS102").subjectName("Data Structures & Algorithms")
                    .semesterNumber(2).attemptNumber(1).credits(new BigDecimal("4.0")).grade("F")
                    .marksObtained(new BigDecimal("32.0")).maxMarks(new BigDecimal("100.0"))
                    .isBacklog(true).isCleared(true).clearedInSemester(3).build());

            SemesterResult r3 = semesterResultRepository.save(SemesterResult.builder()
                    .student(s1)
                    .semesterNumber(3)
                    .sgpa(new BigDecimal("8.65"))
                    .creditsRegistered(new BigDecimal("24.0"))
                    .creditsEarned(new BigDecimal("24.0"))
                    .resultStatus("PASSED")
                    .hasActiveBacklog(false)
                    .verificationStatus("VERIFIED")
                    .build());

            log.info("Bootstrapped demo student Aarav Mehta (22JU0101 / 22BCSE101)");
        }

        // Student 2: Riya Sharma (22JU0102, 22BECE102)
        if (eceDept != null && eceProg != null && studentRepository.findByUniversityStudentId("22JU0102").isEmpty()) {
            Student s2 = Student.builder()
                    .universityStudentId("22JU0102")
                    .rollNumber("22BECE102")
                    .registrationNumber("JU/2022/ECE/102")
                    .firstName("Riya")
                    .lastName("Sharma")
                    .universityEmail("riya.sharma@jecrcuniversity.edu.in")
                    .personalEmail("riya.ece@gmail.com")
                    .phoneNumber("+91-9876543211")
                    .dateOfBirth(LocalDate.of(2004, 8, 22))
                    .gender("Female")
                    .program(eceProg)
                    .department(eceDept)
                    .batch(eceBatch26)
                    .currentSemester(6)
                    .section("A")
                    .academicStatus("ENROLLED")
                    .verificationStatus("VERIFIED")
                    .cgpa(new BigDecimal("9.10"))
                    .activeBacklogs(0)
                    .historicalBacklogs(0)
                    .totalCreditsEarned(new BigDecimal("110.0"))
                    .build();
            s2 = studentRepository.save(s2);

            qualificationRepository.save(StudentQualification.builder()
                    .student(s2).qualificationType("CLASS_10").boardOrUniversity("RBSE")
                    .institutionName("St. Xavier School, Jaipur").passingYear(2020)
                    .markingScheme("PERCENTAGE").percentageOrCgpa(new BigDecimal("94.20"))
                    .verificationStatus("VERIFIED").build());

            qualificationRepository.save(StudentQualification.builder()
                    .student(s2).qualificationType("CLASS_12").boardOrUniversity("RBSE")
                    .institutionName("St. Xavier School, Jaipur").passingYear(2022)
                    .markingScheme("PERCENTAGE").percentageOrCgpa(new BigDecimal("91.80"))
                    .verificationStatus("VERIFIED").build());

            log.info("Bootstrapped demo student Riya Sharma (22JU0102 / 22BECE102)");
        }

        // Student 3: Vikram Verma (22JU0103, 22BCSE103) - Pending verification, 1 active backlog
        if (studentRepository.findByUniversityStudentId("22JU0103").isEmpty()) {
            Student s3 = Student.builder()
                    .universityStudentId("22JU0103")
                    .rollNumber("22BCSE103")
                    .registrationNumber("JU/2022/CSE/103")
                    .firstName("Vikram")
                    .lastName("Verma")
                    .universityEmail("vikram.verma@jecrcuniversity.edu.in")
                    .personalEmail("vikram.v@gmail.com")
                    .phoneNumber("+91-9876543212")
                    .dateOfBirth(LocalDate.of(2003, 11, 10))
                    .gender("Male")
                    .program(cseProg)
                    .department(cseDept)
                    .batch(cseBatch26)
                    .currentSemester(6)
                    .section("B")
                    .academicStatus("ENROLLED")
                    .verificationStatus("PENDING")
                    .cgpa(new BigDecimal("6.85"))
                    .activeBacklogs(1)
                    .historicalBacklogs(1)
                    .totalCreditsEarned(new BigDecimal("98.0"))
                    .build();
            s3 = studentRepository.save(s3);

            qualificationRepository.save(StudentQualification.builder()
                    .student(s3).qualificationType("CLASS_10").boardOrUniversity("CBSE")
                    .institutionName("Kendriya Vidyalaya No. 1, Jaipur").passingYear(2019)
                    .markingScheme("PERCENTAGE").percentageOrCgpa(new BigDecimal("78.50"))
                    .verificationStatus("PENDING").build());

            qualificationRepository.save(StudentQualification.builder()
                    .student(s3).qualificationType("CLASS_12").boardOrUniversity("CBSE")
                    .institutionName("Kendriya Vidyalaya No. 1, Jaipur").passingYear(2021)
                    .markingScheme("PERCENTAGE").percentageOrCgpa(new BigDecimal("72.00"))
                    .verificationStatus("PENDING").build());

            log.info("Bootstrapped demo student Vikram Verma (22JU0103 / 22BCSE103)");
        }
    }

    private void createDemoUserIfNotExists(String email, String first, String last, String roleName, Department dept) {
        String initialPass = (demoPassword != null && !demoPassword.isBlank()) ? demoPassword : adminPassword;
        User existingUser = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (existingUser == null) {
            roleRepository.findByRoleName(roleName).ifPresent(role -> {
                User user = User.builder()
                        .email(email.toLowerCase().trim())
                        .passwordHash(passwordEncoder.encode(initialPass))
                        .firstName(first)
                        .lastName(last)
                        .phoneNumber("+91-9876543210")
                        .isActive(true)
                        .isSuspended(false)
                        .securityStamp(UUID.randomUUID())
                        .roles(Set.of(role))
                        .build();

                user = userRepository.save(user);

                if (dept != null) {
                    UserDepartmentScope scope = UserDepartmentScope.builder()
                            .user(user)
                            .department(dept)
                            .isPrimary(true)
                            .build();
                    departmentScopeRepository.save(scope);
                }
                log.info("Bootstrapped demo test account: {} with role [{}]", email, roleName);
            });
        } else {
            existingUser.setPasswordHash(passwordEncoder.encode(initialPass));
            existingUser.setIsActive(true);
            existingUser.setIsSuspended(false);
            userRepository.save(existingUser);
            log.info("Synchronized password for demo account: {}", email);
        }
    }
}
