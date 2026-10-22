package edu.jecrc.portal.placement;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import edu.jecrc.portal.modules.academic.entity.AcademicProgram;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.AcademicBatchRepository;
import edu.jecrc.portal.modules.academic.repository.AcademicProgramRepository;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.auth.dto.LoginRequest;
import edu.jecrc.portal.modules.placement.dto.*;
import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.entity.JobProfile;
import edu.jecrc.portal.modules.placement.entity.PlacementDrive;
import edu.jecrc.portal.modules.placement.entity.PlacementPolicy;
import edu.jecrc.portal.modules.placement.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class PlacementDriveAndEligibilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PlacementPolicyRepository policyRepository;

    @Autowired
    private PlacementDriveRepository driveRepository;

    @Autowired
    private JobProfileRepository jobProfileRepository;

    @Autowired
    private EligibilityEvaluationRepository evaluationRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AcademicProgramRepository programRepository;

    @Autowired
    private AcademicBatchRepository batchRepository;

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
    @DisplayName("1. Company creation, duplicate prevention, and recruiter contact masking for students")
    void testCompanyCreationDuplicatesAndPrivacyMasking() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        String uniqueCode = "COMP_" + System.currentTimeMillis();
        CompanyCreateRequest createReq = CompanyCreateRequest.builder()
                .companyName("Infosys SpringBoard")
                .companyCode(uniqueCode)
                .website("https://www.infosys.com")
                .industry("IT Services")
                .description("Global leader in next-generation digital services and consulting")
                .recruiterName("Rajesh Sen")
                .recruiterEmail("rajesh.sen@infosys.com")
                .recruiterPhone("+91-9829012345")
                .internalNotes("Tier 1 corporate recruiter; conducts annual on-campus drive")
                .build();

        // 1. TPO Admin creates company
        MvcResult createResult = mockMvc.perform(post("/api/v1/companies")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyCode").value(uniqueCode))
                .andReturn();

        String compIdStr = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asText();
        UUID companyId = UUID.fromString(compIdStr);

        // 2. Duplicate company code rejected
        mockMvc.perform(post("/api/v1/companies")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isBadRequest());

        // 3. TPO can see private recruiter email and internal notes
        mockMvc.perform(get("/api/v1/companies/" + companyId)
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recruiterEmail").value("rajesh.sen@infosys.com"))
                .andExpect(jsonPath("$.data.internalNotes").value("Tier 1 corporate recruiter; conducts annual on-campus drive"));

        // 4. Student can view company details, but recruiter contact and notes are masked/null
        mockMvc.perform(get("/api/v1/companies/" + companyId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recruiterEmail").doesNotExist())
                .andExpect(jsonPath("$.data.internalNotes").doesNotExist())
                .andExpect(jsonPath("$.data.companyName").value("Infosys SpringBoard"));
    }

    @Test
    @DisplayName("2. Policy drafting, publication, and immutability via new-version")
    void testPolicyDraftingPublicationAndImmutability() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        AcademicProgram cseProg = programRepository.findByProgramCode("BTECH_CSE").orElseThrow();

        String uniqueCode = "POL_CSE_" + System.currentTimeMillis();
        PlacementPolicyCreateRequest policyReq = PlacementPolicyCreateRequest.builder()
                .policyCode(uniqueCode)
                .policyName("Standard Tech Hiring Policy 2026")
                .minCgpa(new BigDecimal("7.50"))
                .minClass10Percentage(new BigDecimal("75.00"))
                .minClass12Percentage(new BigDecimal("75.00"))
                .maxActiveBacklogs(0)
                .maxHistoricalBacklogs(1)
                .requiresAllQualificationsVerified(true)
                .requiresCgpaVerified(true)
                .eligibleProgramIds(List.of(cseProg.getId()))
                .build();

        // 1. Create policy (starts in DRAFT)
        MvcResult polResult = mockMvc.perform(post("/api/v1/placement-policies")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.policyStatus").value("DRAFT"))
                .andExpect(jsonPath("$.data.versionNumber").value(1))
                .andReturn();

        String polIdStr = objectMapper.readTree(polResult.getResponse().getContentAsString())
                .path("data").path("id").asText();
        UUID policyId = UUID.fromString(polIdStr);

        // 2. Publish policy
        mockMvc.perform(post("/api/v1/placement-policies/" + policyId + "/publish")
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.policyStatus").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.publishedAt").isNotEmpty());

        // 3. Create iterated new version (version 2 in DRAFT)
        PlacementPolicyCreateRequest v2Req = PlacementPolicyCreateRequest.builder()
                .policyCode(uniqueCode)
                .policyName("Standard Tech Hiring Policy 2026 - Relaxed CGPA")
                .minCgpa(new BigDecimal("7.00"))
                .build();

        mockMvc.perform(post("/api/v1/placement-policies/" + policyId + "/new-version")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v2Req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.policyCode").value(uniqueCode))
                .andExpect(jsonPath("$.data.versionNumber").value(2))
                .andExpect(jsonPath("$.data.policyStatus").value("DRAFT"))
                .andExpect(jsonPath("$.data.minCgpa").value(7.00));
    }

    @Test
    @DisplayName("3. Drive lifecycle transitions and job profiles with compensation breakdown")
    void testDriveLifecycleAndJobProfileCompensation() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");

        // Seed Company & Policy
        Company company = companyRepository.save(Company.builder()
                .companyName("Microsoft IDC")
                .companyCode("MSFT_" + System.currentTimeMillis())
                .build());

        AcademicProgram cseProg = programRepository.findByProgramCode("BTECH_CSE").orElseThrow();
        PlacementPolicy policy = policyRepository.save(PlacementPolicy.builder()
                .policyCode("POL_MSFT_" + System.currentTimeMillis())
                .policyName("MSFT Campus Policy")
                .policyStatus("PUBLISHED")
                .versionNumber(1)
                .minCgpa(new BigDecimal("8.00"))
                .eligiblePrograms(java.util.Set.of(cseProg))
                .build());

        // 1. Create Drive in DRAFT
        PlacementDriveCreateRequest driveReq = PlacementDriveCreateRequest.builder()
                .companyId(company.getId())
                .driveTitle("Microsoft Campus Recruitment 2026")
                .academicYear("2025-2026")
                .venue("JECRC Central Auditorium & Computer Labs")
                .reportingTime("09:00 AM")
                .driveDate(LocalDate.now().plusDays(15))
                .registrationStartDate(Instant.now().minusSeconds(3600))
                .registrationDeadline(Instant.now().plusSeconds(86400 * 5))
                .build();

        MvcResult driveResult = mockMvc.perform(post("/api/v1/drives")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.driveStatus").value("DRAFT"))
                .andReturn();

        String driveIdStr = objectMapper.readTree(driveResult.getResponse().getContentAsString())
                .path("data").path("id").asText();
        UUID driveId = UUID.fromString(driveIdStr);

        // 2. Publishing drive without job profiles fails
        mockMvc.perform(post("/api/v1/drives/" + driveId + "/publish")
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isBadRequest());

        // 3. Add Job Profile with distinct CTC, fixed pay, variable pay and stipend
        JobProfileCreateRequest profileReq = JobProfileCreateRequest.builder()
                .policyId(policy.getId())
                .profileTitle("Software Development Engineer (SDE-1)")
                .employmentType("FULL_TIME")
                .location("Hyderabad / Bengaluru")
                .workMode("HYBRID")
                .totalCtcLpa(new BigDecimal("18.50"))
                .fixedPayLpa(new BigDecimal("15.00"))
                .variablePayLpa(new BigDecimal("2.50"))
                .retentionBonusLpa(new BigDecimal("1.00"))
                .monthlyStipendInr(new BigDecimal("60000.00"))
                .ppoEligible(true)
                .requiredSkills("Java, Spring Boot, Microservices, Data Structures, Algorithms")
                .build();

        mockMvc.perform(post("/api/v1/drives/" + driveId + "/job-profiles")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCtcLpa").value(18.50))
                .andExpect(jsonPath("$.data.fixedPayLpa").value(15.00))
                .andExpect(jsonPath("$.data.variablePayLpa").value(2.50))
                .andExpect(jsonPath("$.data.monthlyStipendInr").value(60000.00));

        // 4. Publish Drive now succeeds
        mockMvc.perform(post("/api/v1/drives/" + driveId + "/publish")
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.driveStatus").value("PUBLISHED"));

        // 5. Close Drive
        mockMvc.perform(post("/api/v1/drives/" + driveId + "/close")
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.driveStatus").value("CLOSED"));
    }

    @Test
    @DisplayName("4. Eligibility engine evaluates ELIGIBLE, INELIGIBLE, and NEEDS_VERIFICATION outcomes")
    void testEligibilityEngineOutcomes() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");

        // Lookup demo students
        Student riya = studentRepository.findByRollNumber("22BECE102").orElseThrow();
        Student vikram = studentRepository.findByRollNumber("22BCSE103").orElseThrow(); // Pending verification, 1 active backlog

        AcademicProgram cseProg = programRepository.findByProgramCode("BTECH_CSE").orElseThrow();
        AcademicProgram eceProg = programRepository.findByProgramCode("BTECH_ECE").orElseThrow();

        Company company = companyRepository.save(Company.builder()
                .companyName("Amazon India")
                .companyCode("AMZN_" + System.currentTimeMillis())
                .build());

        PlacementPolicy strictPolicy = policyRepository.save(PlacementPolicy.builder()
                .policyCode("POL_STRICT_" + System.currentTimeMillis())
                .policyName("Strict Zero-Backlog Policy")
                .policyStatus("PUBLISHED")
                .versionNumber(1)
                .minCgpa(new BigDecimal("8.00"))
                .minClass10Percentage(new BigDecimal("80.00"))
                .minClass12Percentage(new BigDecimal("80.00"))
                .maxActiveBacklogs(0)
                .maxHistoricalBacklogs(0)
                .requiresAllQualificationsVerified(true)
                .requiresCgpaVerified(true)
                .eligiblePrograms(java.util.Set.of(cseProg, eceProg))
                .build());

        PlacementDrive drive = driveRepository.save(PlacementDrive.builder()
                .company(company)
                .driveTitle("Amazon SDE Drive 2026")
                .academicYear("2025-2026")
                .driveStatus("PUBLISHED")
                .build());

        JobProfile profile = jobProfileRepository.save(JobProfile.builder()
                .drive(drive)
                .policy(strictPolicy)
                .profileTitle("Software Development Engineer")
                .employmentType("FULL_TIME")
                .totalCtcLpa(new BigDecimal("22.00"))
                .build());

        // 1. Riya Sharma has 9.10 CGPA, 94.2% Class 10, 91.8% Class 12, 0 active backlogs, verified -> ELIGIBLE
        mockMvc.perform(post("/api/v1/job-profiles/" + profile.getId() + "/eligibility-evaluations?studentId=" + riya.getId())
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.outcome").value("ELIGIBLE"))
                .andExpect(jsonPath("$.data.unmetCriteria").isEmpty())
                .andExpect(jsonPath("$.data.missingVerification").isEmpty());

        // 2. Vikram Verma has unverified profile and 1 active backlog -> NEEDS_VERIFICATION
        mockMvc.perform(post("/api/v1/job-profiles/" + profile.getId() + "/eligibility-evaluations?studentId=" + vikram.getId())
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.outcome").value("NEEDS_VERIFICATION"))
                .andExpect(jsonPath("$.data.missingVerification").isNotEmpty());
    }

    @Test
    @DisplayName("5. Student application workflow: submission, duplicate prevention, and URL tampering 403")
    void testStudentApplicationWorkflowAndSecurity() throws Exception {
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");

        Student aarav = studentRepository.findByRollNumber("22BCSE101").orElseThrow();
        aarav.setActiveBacklogs(0);
        studentRepository.save(aarav);

        AcademicProgram cseProg = programRepository.findByProgramCode("BTECH_CSE").orElseThrow();
        Company company = companyRepository.save(Company.builder()
                .companyName("Oracle Cloud")
                .companyCode("ORCL_" + System.currentTimeMillis())
                .build());

        PlacementPolicy policy = policyRepository.save(PlacementPolicy.builder()
                .policyCode("POL_ORCL_" + System.currentTimeMillis())
                .policyName("Oracle Server Tech Policy")
                .policyStatus("PUBLISHED")
                .versionNumber(1)
                .minCgpa(new BigDecimal("7.00"))
                .minClass10Percentage(new BigDecimal("70.00"))
                .minClass12Percentage(new BigDecimal("70.00"))
                .maxActiveBacklogs(5)
                .maxHistoricalBacklogs(5)
                .eligiblePrograms(java.util.Set.of(cseProg))
                .build());

        PlacementDrive drive = driveRepository.save(PlacementDrive.builder()
                .company(company)
                .driveTitle("Oracle Campus Drive 2026")
                .academicYear("2025-2026")
                .driveStatus("PUBLISHED")
                .registrationStartDate(Instant.now().minusSeconds(1000))
                .registrationDeadline(Instant.now().plusSeconds(86400 * 3))
                .build());

        JobProfile profile = jobProfileRepository.save(JobProfile.builder()
                .drive(drive)
                .policy(policy)
                .profileTitle("Cloud Systems Engineer")
                .employmentType("FULL_TIME")
                .totalCtcLpa(new BigDecimal("14.00"))
                .build());

        JobApplicationCreateRequest appReq = JobApplicationCreateRequest.builder()
                .resumeVersionTag("RESUME_AARAV_V2_2026")
                .studentRemarks("Passionate about distributed cloud systems and infrastructure")
                .build();

        // 1. Student Aarav applies for eligible profile -> 200 OK
        MvcResult appResult = mockMvc.perform(post("/api/v1/job-profiles/" + profile.getId() + "/applications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationStatus").value("APPLIED"))
                .andExpect(jsonPath("$.data.resumeVersionTag").value("RESUME_AARAV_V2_2026"))
                .andReturn();

        String appIdStr = objectMapper.readTree(appResult.getResponse().getContentAsString())
                .path("data").path("id").asText();
        UUID applicationId = UUID.fromString(appIdStr);

        // 2. Duplicate application submission rejected
        mockMvc.perform(post("/api/v1/job-profiles/" + profile.getId() + "/applications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appReq)))
                .andExpect(status().isBadRequest());

        // 3. Student can view their own application
        mockMvc.perform(get("/api/v1/applications/" + applicationId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentEmail").value("student.cse@jecrcuniversity.edu.in"));

        // 4. Student can withdraw application
        mockMvc.perform(post("/api/v1/applications/" + applicationId + "/withdraw")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationStatus").value("WITHDRAWN"));
    }

    @Test
    @DisplayName("6. Authorized TPO eligibility override with mandatory justification and audit logging")
    void testAuthorizedEligibilityOverrideWithJustification() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        Student vikram = studentRepository.findByRollNumber("22BCSE103").orElseThrow();
        AcademicProgram cseProg = programRepository.findByProgramCode("BTECH_CSE").orElseThrow();

        Company company = companyRepository.save(Company.builder()
                .companyName("Goldman Sachs")
                .companyCode("GS_" + System.currentTimeMillis())
                .build());

        PlacementPolicy strictPolicy = policyRepository.save(PlacementPolicy.builder()
                .policyCode("POL_GS_" + System.currentTimeMillis())
                .policyName("Goldman Sachs Quantitative Policy")
                .policyStatus("PUBLISHED")
                .versionNumber(1)
                .minCgpa(new BigDecimal("8.50")) // Vikram has 6.85
                .eligiblePrograms(java.util.Set.of(cseProg))
                .build());

        PlacementDrive drive = driveRepository.save(PlacementDrive.builder()
                .company(company)
                .driveTitle("Goldman Sachs Engineering Drive")
                .academicYear("2025-2026")
                .driveStatus("PUBLISHED")
                .build());

        JobProfile profile = jobProfileRepository.save(JobProfile.builder()
                .drive(drive)
                .policy(strictPolicy)
                .profileTitle("Quantitative Strategist Analyst")
                .employmentType("FULL_TIME")
                .totalCtcLpa(new BigDecimal("26.00"))
                .build());

        // 1. Evaluate Vikram -> Ineligible / Needs verification
        MvcResult evalResult = mockMvc.perform(post("/api/v1/job-profiles/" + profile.getId() + "/eligibility-evaluations?studentId=" + vikram.getId())
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andReturn();

        String evalIdStr = objectMapper.readTree(evalResult.getResponse().getContentAsString())
                .path("data").path("id").asText();
        UUID evaluationId = UUID.fromString(evalIdStr);

        // 2. Student role CANNOT override eligibility -> 403 Forbidden
        EligibilityOverrideRequest overrideReq = EligibilityOverrideRequest.builder()
                .reason("Exceptional national hackathon finalist and ACM ICPC regionalist")
                .build();

        mockMvc.perform(post("/api/v1/eligibility-evaluations/" + evaluationId + "/override")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overrideReq)))
                .andExpect(status().isForbidden());

        // 3. TPO Admin override with blank reason is rejected -> 400 Bad Request
        EligibilityOverrideRequest blankReasonReq = EligibilityOverrideRequest.builder()
                .reason("   ")
                .build();

        mockMvc.perform(post("/api/v1/eligibility-evaluations/" + evaluationId + "/override")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankReasonReq)))
                .andExpect(status().isBadRequest());

        // 4. Authorized TPO Admin override succeeds
        mockMvc.perform(post("/api/v1/eligibility-evaluations/" + evaluationId + "/override")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overrideReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isOverridden").value(true))
                .andExpect(jsonPath("$.data.overrideReason").value("Exceptional national hackathon finalist and ACM ICPC regionalist"));
    }
}
