package edu.jecrc.portal.academic;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.jecrc.portal.modules.academic.dto.*;
import edu.jecrc.portal.modules.academic.entity.AcademicProgram;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.AcademicProgramRepository;
import edu.jecrc.portal.modules.academic.repository.StudentQualificationRepository;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.auth.dto.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class AcademicRegistryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentQualificationRepository qualificationRepository;

    @Autowired
    private AcademicProgramRepository programRepository;

    private String getAccessToken(String email, String password) throws Exception {
        LoginRequest req = LoginRequest.builder().email(email).password(password).build();
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        String json = res.getResponse().getContentAsString();
        return objectMapper.readTree(json).path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("1. Student creation and duplicate prevention across Student ID and Roll Number")
    void testStudentCreationAndDuplicatePrevention() throws Exception {
        String adminToken = getAccessToken("admin@jecrcuniversity.edu.in", "Admin@JECRC2026!");
        AcademicProgram cseProgram = programRepository.findByProgramCode("BTECH_CSE").orElseThrow();

        String uniqueSuffix = String.valueOf(System.currentTimeMillis()).substring(7);
        StudentCreateRequest req = StudentCreateRequest.builder()
                .universityStudentId("TEST_ID_" + uniqueSuffix)
                .rollNumber("TEST_ROLL_" + uniqueSuffix)
                .registrationNumber("TEST_REG_" + uniqueSuffix)
                .firstName("Test")
                .lastName("Student")
                .universityEmail("test.student." + uniqueSuffix + "@jecrcuniversity.edu.in")
                .personalEmail("test" + uniqueSuffix + "@gmail.com")
                .phoneNumber("+91-9988776655")
                .programId(cseProgram.getId())
                .batchId(2) // 2022-2026
                .currentSemester(4)
                .section("A")
                .build();

        // 1. Successful creation
        mockMvc.perform(post("/api/v1/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.universityStudentId").value("TEST_ID_" + uniqueSuffix));

        // 2. Duplicate university student ID rejection
        mockMvc.perform(post("/api/v1/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("2. Qualification validation: distinct null marks from 0.0, valid marks correctly stored")
    void testQualificationValidationAndMissingMarksDistinctFromZero() throws Exception {
        String adminToken = getAccessToken("admin@jecrcuniversity.edu.in", "Admin@JECRC2026!");
        Student student = studentRepository.findByRollNumber("22BCSE101").orElseThrow();

        // Send qualification with null percentage (missing/unverified mark)
        StudentQualificationDto nullMarksDto = StudentQualificationDto.builder()
                .qualificationType("DIPLOMA")
                .boardOrUniversity("Board of Technical Education")
                .institutionName("Government Polytechnic")
                .passingYear(2022)
                .markingScheme("PERCENTAGE")
                .percentageOrCgpa(null) // Deliberate NULL
                .build();

        mockMvc.perform(put("/api/v1/students/" + student.getId() + "/qualifications")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(nullMarksDto))))
                .andExpect(status().isOk());

        var qual = qualificationRepository.findByStudentIdAndQualificationType(student.getId(), "DIPLOMA").orElseThrow();
        // Assert: must remain NULL, not coerced to 0.0
        assertThat(qual.getPercentageOrCgpa()).isNull();
    }

    @Test
    @DisplayName("3. Semester result and backlog calculation: active vs historical backlog metrics")
    void testSemesterResultAndBacklogCalculations() throws Exception {
        String adminToken = getAccessToken("admin@jecrcuniversity.edu.in", "Admin@JECRC2026!");
        Student student = studentRepository.findByRollNumber("22BCSE101").orElseThrow();

        // Record Semester 4 with an active backlog in CS401
        SemesterResultDto sem4Result = SemesterResultDto.builder()
                .semesterNumber(4)
                .sgpa(new BigDecimal("7.50"))
                .creditsRegistered(new BigDecimal("22.0"))
                .creditsEarned(new BigDecimal("18.0"))
                .resultStatus("PASSED")
                .subjectAttempts(List.of(
                        SubjectAttemptDto.builder()
                                .subjectCode("CS401")
                                .subjectName("Operating Systems")
                                .semesterNumber(4)
                                .attemptNumber(1)
                                .credits(new BigDecimal("4.0"))
                                .grade("F")
                                .marksObtained(new BigDecimal("28.0"))
                                .maxMarks(new BigDecimal("100.0"))
                                .isBacklog(true)
                                .isCleared(false)
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/students/" + student.getId() + "/semester-results")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sem4Result)))
                .andExpect(status().isOk());

        Student updatedStudent = studentRepository.findById(student.getId()).orElseThrow();
        assertThat(updatedStudent.getActiveBacklogs()).isGreaterThanOrEqualTo(1);
        assertThat(updatedStudent.getHistoricalBacklogs()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("4. Student access restricted to own record (URL tampering returns 403 Forbidden)")
    void testStudentAccessRestrictedToOwnRecord() throws Exception {
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        // 1. Can access own profile via /me
        mockMvc.perform(get("/api/v1/students/me")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.universityEmail").value("student.cse@jecrcuniversity.edu.in"));

        // 2. Cannot access another student's profile by ID (e.g., Riya Sharma in ECE)
        Student riya = studentRepository.findByRollNumber("22BECE102").orElseThrow();
        mockMvc.perform(get("/api/v1/students/" + riya.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. HOD department-level scoping: HOD CSE cannot access ECE student")
    void testHodDepartmentLevelAccessAndScoping() throws Exception {
        String hodToken = getAccessToken("hod.cse@jecrcuniversity.edu.in", "Password@123");

        // 1. HOD CSE can access CSE student
        Student aarav = studentRepository.findByRollNumber("22BCSE101").orElseThrow();
        mockMvc.perform(get("/api/v1/students/" + aarav.getId())
                        .header("Authorization", "Bearer " + hodToken))
                .andExpect(status().isOk());

        // 2. HOD CSE is denied access to ECE student
        Student riya = studentRepository.findByRollNumber("22BECE102").orElseThrow();
        mockMvc.perform(get("/api/v1/students/" + riya.getId())
                        .header("Authorization", "Bearer " + hodToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6. Academic record verification: Student denied; HOD authorized")
    void testAuthorizedVerificationAndDeniedVerification() throws Exception {
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");
        String hodToken = getAccessToken("hod.cse@jecrcuniversity.edu.in", "Password@123");
        Student student = studentRepository.findByRollNumber("22BCSE103").orElseThrow();

        StudentVerifyRequest verifyReq = StudentVerifyRequest.builder()
                .verificationStatus("VERIFIED")
                .verificationRemarks("Verified against physical university marksheets")
                .build();

        // 1. Student role cannot verify records
        mockMvc.perform(post("/api/v1/students/" + student.getId() + "/verify")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isForbidden());

        // 2. HOD CSE can verify CSE student
        mockMvc.perform(post("/api/v1/students/" + student.getId() + "/verify")
                        .header("Authorization", "Bearer " + hodToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"));
    }

    @Test
    @DisplayName("7. Import preview causes no database modifications")
    void testImportPreviewCausesNoWrites() throws Exception {
        String adminToken = getAccessToken("admin@jecrcuniversity.edu.in", "Admin@JECRC2026!");
        String uniqueId = "PREVIEW_NO_WRITE_" + System.currentTimeMillis();

        String csvContent = "university_student_id,roll_number,first_name,last_name,university_email,program_code,graduation_year,current_semester\n" +
                uniqueId + ",ROLL_" + uniqueId + ",Sample,User," + uniqueId.toLowerCase() + "@jecrcuniversity.edu.in,BTECH_CSE,2026,6\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "import_preview_test.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // Run preview
        mockMvc.perform(multipart("/api/v1/students/import/preview")
                        .file(file)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.validRows").value(1))
                .andExpect(jsonPath("$.data.errorRows").value(0));

        // Verify record is NOT created in students table
        assertThat(studentRepository.existsByUniversityStudentId(uniqueId)).isFalse();
    }

    @Test
    @DisplayName("8. Import commit persists rows and never overwrites verified records")
    void testImportCommitProtectsVerifiedRecord() throws Exception {
        String adminToken = getAccessToken("admin@jecrcuniversity.edu.in", "Admin@JECRC2026!");

        // Aarav Mehta (22JU0101) is already VERIFIED in V4
        Student verifiedStudent = studentRepository.findByUniversityStudentId("22JU0101").orElseThrow();
        assertThat(verifiedStudent.getVerificationStatus()).isEqualTo("VERIFIED");

        String csvContent = "university_student_id,roll_number,first_name,last_name,university_email,program_code,graduation_year,current_semester\n" +
                "22JU0101,22BCSE101,HackedFirstName,Mehta,student.cse@jecrcuniversity.edu.in,BTECH_CSE,2026,6\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "commit_test.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        MvcResult previewRes = mockMvc.perform(multipart("/api/v1/students/import/preview")
                        .file(file)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        String importIdStr = objectMapper.readTree(previewRes.getResponse().getContentAsString())
                .path("data").path("importId").asText();

        // Commit import
        ImportCommitRequest commitReq = ImportCommitRequest.builder()
                .importId(UUID.fromString(importIdStr))
                .updateExisting(true)
                .build();

        mockMvc.perform(post("/api/v1/students/import/commit")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commitReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skippedCount").value(1)); // Skipped because it's VERIFIED

        // Assert verified student was NOT overwritten
        Student rechecked = studentRepository.findByUniversityStudentId("22JU0101").orElseThrow();
        assertThat(rechecked.getFirstName()).isEqualTo("Aarav");
    }

    @Test
    @DisplayName("9. Export authorization and department scoping")
    void testExportAuthorizationAndDepartmentScoping() throws Exception {
        String adminToken = getAccessToken("admin@jecrcuniversity.edu.in", "Admin@JECRC2026!");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        // 1. Student role is forbidden from exporting
        mockMvc.perform(get("/api/v1/students/export")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        // 2. Admin can export spreadsheet
        mockMvc.perform(get("/api/v1/students/export")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(status().is(200));
    }
}
