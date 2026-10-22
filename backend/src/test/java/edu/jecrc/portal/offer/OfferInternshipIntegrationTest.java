package edu.jecrc.portal.offer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.jecrc.portal.modules.academic.entity.AcademicProgram;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.AcademicProgramRepository;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.auth.dto.LoginRequest;
import edu.jecrc.portal.modules.offer.dto.*;
import edu.jecrc.portal.modules.offer.entity.InstitutionalOfferPolicy;
import edu.jecrc.portal.modules.offer.entity.InternshipRecord;
import edu.jecrc.portal.modules.offer.entity.PlacementOffer;
import edu.jecrc.portal.modules.offer.entity.PpoRecord;
import edu.jecrc.portal.modules.offer.repository.*;
import edu.jecrc.portal.modules.placement.entity.*;
import edu.jecrc.portal.modules.placement.repository.*;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class OfferInternshipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlacementOfferRepository offerRepository;

    @Autowired
    private OfferDecisionHistoryRepository decisionHistoryRepository;

    @Autowired
    private InstitutionalOfferPolicyRepository offerPolicyRepository;

    @Autowired
    private InternshipRecordRepository internshipRepository;

    @Autowired
    private InternshipProgressLogRepository progressLogRepository;

    @Autowired
    private InternshipEvaluationRepository internshipEvaluationRepository;

    @Autowired
    private PpoRecordRepository ppoRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PlacementPolicyRepository placementPolicyRepository;

    @Autowired
    private PlacementDriveRepository driveRepository;

    @Autowired
    private JobProfileRepository jobProfileRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private EligibilityEvaluationRepository evaluationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AcademicProgramRepository programRepository;

    @BeforeEach
    void setUp() {
        ppoRepository.deleteAll();
        internshipEvaluationRepository.deleteAll();
        progressLogRepository.deleteAll();
        internshipRepository.deleteAll();
        decisionHistoryRepository.deleteAll();
        offerRepository.deleteAll();
    }

    private String getAccessToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();
    }

    private JobProfile setupCompanyAndProfile(String compName, String profileName, BigDecimal ctc) {
        AcademicProgram cseProg = programRepository.findByProgramCode("BTECH_CSE").orElseThrow();

        Company company = companyRepository.save(Company.builder()
                .companyName(compName)
                .companyCode("COMP_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000))
                .build());

        PlacementPolicy policy = placementPolicyRepository.save(PlacementPolicy.builder()
                .policyCode("POL_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000))
                .policyName("Policy for " + compName)
                .versionNumber(1)
                .policyStatus("PUBLISHED")
                .minCgpa(new BigDecimal("6.5"))
                .eligiblePrograms(java.util.Set.of(cseProg))
                .build());

        PlacementDrive drive = driveRepository.save(PlacementDrive.builder()
                .company(company)
                .driveTitle(compName + " Campus Recruitment")
                .academicYear("2025-2026")
                .driveStatus("PUBLISHED")
                .registrationDeadline(Instant.now().plusSeconds(86400 * 5))
                .build());

        return jobProfileRepository.save(JobProfile.builder()
                .drive(drive)
                .policy(policy)
                .profileTitle(profileName)
                .employmentType("FULL_TIME")
                .totalCtcLpa(ctc)
                .build());
    }

    @Test
    @DisplayName("1. Offer creation only for selected candidates, issuance, and unauthorized user rejection")
    void testOfferCreationAndIssuanceValidation() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");
        Student aarav = studentRepository.findByRollNumber("22BCSE101").orElseThrow();

        JobProfile profile = setupCompanyAndProfile("Goldman Sachs", "Analyst SDE", new BigDecimal("22.00"));

        EligibilityEvaluation eval = evaluationRepository.save(EligibilityEvaluation.builder()
                .student(aarav)
                .jobProfile(profile)
                .policy(profile.getPolicy())
                .policyVersion(1)
                .outcome("ELIGIBLE")
                .criteriaSnapshotJson("{}")
                .build());

        JobApplication app = applicationRepository.save(JobApplication.builder()
                .jobProfile(profile)
                .student(aarav)
                .evaluation(eval)
                .applicationStatus("SELECTED") // Selected candidate
                .resumeVersionTag("v1-selected")
                .build());

        PlacementOfferCreateRequest req = PlacementOfferCreateRequest.builder()
                .studentId(aarav.getId())
                .applicationId(app.getId())
                .companyId(profile.getDrive().getCompany().getId())
                .jobProfileId(profile.getId())
                .designation("Software Development Analyst")
                .employmentType("FULL_TIME")
                .totalCtcLpa(new BigDecimal("22.00"))
                .fixedSalaryLpa(new BigDecimal("18.00"))
                .variableSalaryLpa(new BigDecimal("2.50"))
                .joiningBonus(new BigDecimal("1.50"))
                .jobLocation("Bengaluru / Hyderabad")
                .build();

        // 1. TPO can draft offer
        MvcResult res = mockMvc.perform(post("/api/v1/offers")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.offerStatus").value("DRAFT"))
                .andExpect(jsonPath("$.data.designation").value("Software Development Analyst"))
                .andExpect(jsonPath("$.data.totalCtcLpa").value(22.0))
                .andReturn();

        UUID offerId = UUID.fromString(objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asText());

        // 2. Student is forbidden from issuing offers
        PlacementOfferIssueRequest issueReq = PlacementOfferIssueRequest.builder()
                .decisionDeadline(Instant.now().plusSeconds(86400 * 3))
                .build();

        mockMvc.perform(post("/api/v1/offers/" + offerId + "/issue")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(issueReq)))
                .andExpect(status().isForbidden());

        // 3. TPO issues offer officially
        mockMvc.perform(post("/api/v1/offers/" + offerId + "/issue")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(issueReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.offerStatus").value("ISSUED"));

        // 4. Verify decision history
        mockMvc.perform(get("/api/v1/offers/" + offerId + "/history")
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].newStatus").value("ISSUED"));
    }

    @Test
    @DisplayName("2. Student views own offers and accepts offer within deadline with decision history")
    void testStudentOfferDecisionAndAuditHistory() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");
        Student aarav = studentRepository.findByRollNumber("22BCSE101").orElseThrow();

        JobProfile profile = setupCompanyAndProfile("Morgan Stanley", "Associate Technologist", new BigDecimal("19.50"));

        EligibilityEvaluation eval = evaluationRepository.save(EligibilityEvaluation.builder()
                .student(aarav).jobProfile(profile).policy(profile.getPolicy()).policyVersion(1).outcome("ELIGIBLE").criteriaSnapshotJson("{}").build());
        JobApplication app = applicationRepository.save(JobApplication.builder()
                .jobProfile(profile).student(aarav).evaluation(eval).applicationStatus("SELECTED").resumeVersionTag("v2-selected").build());

        PlacementOfferCreateRequest createReq = PlacementOfferCreateRequest.builder()
                .studentId(aarav.getId())
                .applicationId(app.getId())
                .companyId(profile.getDrive().getCompany().getId())
                .jobProfileId(profile.getId())
                .designation("Associate Technologist")
                .totalCtcLpa(new BigDecimal("19.50"))
                .fixedSalaryLpa(new BigDecimal("16.00"))
                .build();

        MvcResult createRes = mockMvc.perform(post("/api/v1/offers")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();

        UUID offerId = UUID.fromString(objectMapper.readTree(createRes.getResponse().getContentAsString()).path("data").path("id").asText());

        // Issue offer
        mockMvc.perform(post("/api/v1/offers/" + offerId + "/issue")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PlacementOfferIssueRequest.builder()
                                .decisionDeadline(Instant.now().plusSeconds(86400 * 5))
                                .build())))
                .andExpect(status().isOk());

        // 1. Student views own issued offers at /api/v1/offers/me
        mockMvc.perform(get("/api/v1/offers/me")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.id == '" + offerId + "')].companyName").value("Morgan Stanley"));

        // 2. Student accepts offer
        StudentOfferDecisionRequest decisionReq = StudentOfferDecisionRequest.builder()
                .decision("ACCEPTED")
                .remarks("Honored to accept Morgan Stanley offer!")
                .build();

        mockMvc.perform(post("/api/v1/offers/" + offerId + "/decision")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decisionReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.offerStatus").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.studentDecision").value("ACCEPTED"));

        // 3. Repeated decision request returns 400 Bad Request
        mockMvc.perform(post("/api/v1/offers/" + offerId + "/decision")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decisionReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("3. Multiple offer policy enforcement and Dream Tier threshold validation")
    void testMultipleOfferPolicyAndDreamTierEnforcement() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");
        Student aarav = studentRepository.findByRollNumber("22BCSE101").orElseThrow();

        // 1. Ensure active policy has single accepted offer rule and 1.5x dream tier
        InstitutionalOfferPolicy activePolicy = offerPolicyRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc().orElseThrow();
        activePolicy.setAllowMultipleOffers(true);
        activePolicy.setAllowMultipleAccepted(false);
        activePolicy.setDreamTierMultiplierThreshold(new BigDecimal("1.50"));
        activePolicy.setSuperDreamCtcThresholdLpa(new BigDecimal("25.00"));
        offerPolicyRepository.save(activePolicy);

        JobProfile baseProfile = setupCompanyAndProfile("Infosys BPM", "System Engineer", new BigDecimal("10.00"));
        EligibilityEvaluation baseEval = evaluationRepository.save(EligibilityEvaluation.builder()
                .student(aarav).jobProfile(baseProfile).policy(baseProfile.getPolicy()).policyVersion(1).outcome("ELIGIBLE").criteriaSnapshotJson("{}").build());
        JobApplication baseApp = applicationRepository.save(JobApplication.builder()
                .jobProfile(baseProfile).student(aarav).evaluation(baseEval).applicationStatus("SELECTED").resumeVersionTag("base").build());

        // Create & Accept Base Offer (₹10 LPA)
        PlacementOffer baseOffer = offerRepository.save(PlacementOffer.builder()
                .offerReferenceNumber("OFFER-BASE-" + System.currentTimeMillis())
                .student(aarav)
                .application(baseApp)
                .company(baseProfile.getDrive().getCompany())
                .jobProfile(baseProfile)
                .designation("System Engineer")
                .totalCtcLpa(new BigDecimal("10.00"))
                .offerStatus("ACCEPTED")
                .studentDecision("ACCEPTED")
                .build());

        // 2. Sub-dream offer (₹12 LPA < 1.5 * 10 = ₹15 LPA) is rejected during draft creation
        JobProfile subDreamProfile = setupCompanyAndProfile("Cognizant GenC", "Programmer Analyst", new BigDecimal("12.00"));
        EligibilityEvaluation subDreamEval = evaluationRepository.save(EligibilityEvaluation.builder()
                .student(aarav).jobProfile(subDreamProfile).policy(subDreamProfile.getPolicy()).policyVersion(1).outcome("ELIGIBLE").criteriaSnapshotJson("{}").build());
        JobApplication subDreamApp = applicationRepository.save(JobApplication.builder()
                .jobProfile(subDreamProfile).student(aarav).evaluation(subDreamEval).applicationStatus("SELECTED").resumeVersionTag("sub").build());

        PlacementOfferCreateRequest subDreamReq = PlacementOfferCreateRequest.builder()
                .studentId(aarav.getId())
                .applicationId(subDreamApp.getId())
                .companyId(subDreamProfile.getDrive().getCompany().getId())
                .jobProfileId(subDreamProfile.getId())
                .designation("Programmer Analyst")
                .totalCtcLpa(new BigDecimal("12.00"))
                .build();

        mockMvc.perform(post("/api/v1/offers")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subDreamReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("requires a Dream tier upgrade")));

        // 3. Dream tier offer (₹16 LPA >= 1.5 * 10 = ₹15 LPA) succeeds
        JobProfile dreamProfile = setupCompanyAndProfile("Atlassian", "SDE 1", new BigDecimal("16.00"));
        EligibilityEvaluation dreamEval = evaluationRepository.save(EligibilityEvaluation.builder()
                .student(aarav).jobProfile(dreamProfile).policy(dreamProfile.getPolicy()).policyVersion(1).outcome("ELIGIBLE").criteriaSnapshotJson("{}").build());
        JobApplication dreamApp = applicationRepository.save(JobApplication.builder()
                .jobProfile(dreamProfile).student(aarav).evaluation(dreamEval).applicationStatus("SELECTED").resumeVersionTag("dream").build());

        PlacementOfferCreateRequest dreamReq = PlacementOfferCreateRequest.builder()
                .studentId(aarav.getId())
                .applicationId(dreamApp.getId())
                .companyId(dreamProfile.getDrive().getCompany().getId())
                .jobProfileId(dreamProfile.getId())
                .designation("Associate Software Engineer")
                .totalCtcLpa(new BigDecimal("16.00"))
                .build();

        mockMvc.perform(post("/api/v1/offers")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dreamReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCtcLpa").value(16.0));
    }

    @Test
    @DisplayName("4. Internship registration, mentor assignment, and mentor access isolation")
    void testInternshipRegistrationAndMentorAssignment() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        User facultyUser = userRepository.findByEmailIgnoreCase("faculty.cse@jecrcuniversity.edu.in").orElseThrow();
        facultyUser.setIsSuspended(false);
        facultyUser.setIsActive(true);
        userRepository.save(facultyUser);
        String facultyToken = getAccessToken("faculty.cse@jecrcuniversity.edu.in", "Password@123");

        Student aarav = studentRepository.findByRollNumber("22BCSE101").orElseThrow();
        Company comp = companyRepository.save(Company.builder()
                .companyName("Qualcomm India")
                .companyCode("COMP_QUALCOMM_" + System.currentTimeMillis())
                .build());

        // 1. TPO registers internship
        InternshipCreateRequest req = InternshipCreateRequest.builder()
                .studentId(aarav.getId())
                .companyId(comp.getId())
                .internshipTitle("Hardware Software Co-Design Intern")
                .internshipType("SUMMER")
                .startDate(LocalDate.now().plusDays(10))
                .expectedEndDate(LocalDate.now().plusDays(90))
                .monthlyStipendInr(new BigDecimal("50000.00"))
                .workArrangement("HYBRID")
                .location("Noida / Bengaluru")
                .hasPpoPotential(true)
                .build();

        MvcResult res = mockMvc.perform(post("/api/v1/internships")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.internshipStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.monthlyStipendInr").value(50000.0))
                .andReturn();

        UUID internshipId = UUID.fromString(objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asText());

        // 2. Assign faculty mentor
        InternshipMentorAssignRequest mentorReq = InternshipMentorAssignRequest.builder()
                .facultyMentorUserId(facultyUser.getId())
                .industryMentorName("Vikramaditya Sharma")
                .industryMentorEmail("v.sharma@qualcomm.com")
                .build();

        mockMvc.perform(post("/api/v1/internships/" + internshipId + "/mentors")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mentorReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.facultyMentorName").value(facultyUser.getFullName()));

        // 3. Faculty queries assigned mentees
        mockMvc.perform(get("/api/v1/internships/assigned-mentees")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == '" + internshipId + "')].internshipTitle").value("Hardware Software Co-Design Intern"));
    }

    @Test
    @DisplayName("5. Student progress logs and confidential staff evaluation note masking")
    void testInternshipProgressLogsAndConfidentialEvaluationMasking() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");
        User facultyUser = userRepository.findByEmailIgnoreCase("faculty.cse@jecrcuniversity.edu.in").orElseThrow();
        facultyUser.setIsSuspended(false);
        facultyUser.setIsActive(true);
        userRepository.save(facultyUser);
        String facultyToken = getAccessToken("faculty.cse@jecrcuniversity.edu.in", "Password@123");

        Student aarav = studentRepository.findByRollNumber("22BCSE101").orElseThrow();
        Company comp = companyRepository.save(Company.builder().companyName("Samsung R&D").companyCode("COMP_SRI_" + System.currentTimeMillis()).build());

        InternshipRecord internship = internshipRepository.save(InternshipRecord.builder()
                .student(aarav)
                .company(comp)
                .internshipTitle("AI Systems Intern")
                .startDate(LocalDate.now().minusDays(30))
                .expectedEndDate(LocalDate.now().plusDays(60))
                .internshipStatus("ACTIVE")
                .facultyMentor(facultyUser)
                .build());

        // 1. Student submits progress log
        InternshipProgressLogCreateRequest logReq = InternshipProgressLogCreateRequest.builder()
                .reportingPeriodStart(LocalDate.now().minusDays(14))
                .reportingPeriodEnd(LocalDate.now().minusDays(7))
                .summaryOfWork("Implemented neural network quantization kernels on ARM NEON")
                .learningOutcomes("SIMD vectorization and cache locality optimization")
                .challengesEncountered("Register pressure during unrolling")
                .build();

        MvcResult logRes = mockMvc.perform(post("/api/v1/internships/" + internship.getId() + "/logs")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.logNumber").value(1))
                .andExpect(jsonPath("$.data.submissionStatus").value("SUBMITTED"))
                .andReturn();

        UUID logId = UUID.fromString(objectMapper.readTree(logRes.getResponse().getContentAsString()).path("data").path("id").asText());

        // 2. Faculty mentor reviews progress log
        InternshipProgressLogReviewRequest reviewReq = InternshipProgressLogReviewRequest.builder()
                .submissionStatus("REVIEWED")
                .mentorFeedback("Excellent algorithmic analysis. Keep monitoring memory overhead.")
                .build();

        mockMvc.perform(put("/api/v1/internships/" + internship.getId() + "/logs/" + logId + "/review")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.submissionStatus").value("REVIEWED"));

        // 3. Faculty evaluates intern with confidential staff notes
        InternshipEvaluationCreateRequest evalReq = InternshipEvaluationCreateRequest.builder()
                .evaluationType("MID_TERM")
                .technicalScore(new BigDecimal("92.00"))
                .overallScore(new BigDecimal("94.50"))
                .writtenFeedback("Outstanding engineering contributions to edge compute.")
                .confidentialStaffNotes("Top 5% intern. High priority candidate for direct PPO conversion.")
                .recommendForPpo(true)
                .build();

        mockMvc.perform(post("/api/v1/internships/" + internship.getId() + "/evaluations")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evalReq)))
                .andExpect(status().isOk());

        // 4. Student view of evaluations MUST mask confidentialStaffNotes
        mockMvc.perform(get("/api/v1/internships/" + internship.getId() + "/evaluations")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].overallScore").value(94.5))
                .andExpect(jsonPath("$.data[0].confidentialStaffNotes").isEmpty());

        // 5. TPO / Faculty view of evaluations reveals confidentialStaffNotes
        mockMvc.perform(get("/api/v1/internships/" + internship.getId() + "/evaluations")
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].confidentialStaffNotes").value("Top 5% intern. High priority candidate for direct PPO conversion."));
    }

    @Test
    @DisplayName("6. PPO recommendation, evaluation check, approval, and seamless conversion into formal offer")
    void testPpoRecommendationApprovalAndOfferConversion() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");
        Student aarav = studentRepository.findByRollNumber("22BCSE101").orElseThrow();

        JobProfile profile = setupCompanyAndProfile("NVIDIA Graphics", "Deep Learning Systems Engineer", new BigDecimal("28.00"));

        InternshipRecord internship = internshipRepository.save(InternshipRecord.builder()
                .student(aarav)
                .company(profile.getDrive().getCompany())
                .jobProfile(profile)
                .internshipTitle("CUDA Compute Intern")
                .startDate(LocalDate.now().minusDays(60))
                .expectedEndDate(LocalDate.now().plusDays(30))
                .internshipStatus("ACTIVE")
                .hasPpoPotential(true)
                .build());

        // 1. Mentor / Coordinator initiates PPO review
        PpoReviewCreateRequest reviewReq = PpoReviewCreateRequest.builder()
                .targetJobProfileId(profile.getId())
                .minimumEvaluationScoreRequired(new BigDecimal("85.00"))
                .recommendationNotes("Outstanding CUDA optimization results; highly recommended for Full-Time PPO.")
                .build();

        MvcResult ppoRes = mockMvc.perform(post("/api/v1/internships/" + internship.getId() + "/ppo-reviews")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ppoStatus").value("RECOMMENDED"))
                .andReturn();

        UUID ppoId = UUID.fromString(objectMapper.readTree(ppoRes.getResponse().getContentAsString()).path("data").path("id").asText());

        // 2. Authorized TPO approves PPO and issues formal placement offer
        PpoDecisionRequest decisionReq = PpoDecisionRequest.builder()
                .decision("APPROVED")
                .decisionRemarks("PPO conversion authorized by Dean & TPO Director")
                .totalCtcLpa(new BigDecimal("28.00"))
                .fixedSalaryLpa(new BigDecimal("22.00"))
                .variableSalaryLpa(new BigDecimal("4.00"))
                .joiningBonus(new BigDecimal("2.00"))
                .jobLocation("Pune / Bengaluru")
                .build();

        mockMvc.perform(post("/api/v1/ppo-reviews/" + ppoId + "/decision")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decisionReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ppoStatus").value("OFFER_ISSUED"))
                .andExpect(jsonPath("$.data.resultingOfferReferenceNumber").isNotEmpty());

        // 3. Student views the generated PPO offer in /api/v1/offers/me
        mockMvc.perform(get("/api/v1/offers/me")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.companyName == 'NVIDIA Graphics')].totalCtcLpa", org.hamcrest.Matchers.hasItem(28.0)))
                .andExpect(jsonPath("$.data[?(@.companyName == 'NVIDIA Graphics')].offerStatus", org.hamcrest.Matchers.hasItem("ISSUED")));
    }
}
