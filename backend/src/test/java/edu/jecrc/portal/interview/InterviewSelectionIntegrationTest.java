package edu.jecrc.portal.interview;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.jecrc.portal.modules.academic.entity.AcademicProgram;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.AcademicProgramRepository;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.auth.dto.LoginRequest;
import edu.jecrc.portal.modules.interview.dto.*;
import edu.jecrc.portal.modules.interview.entity.InterviewRound;
import edu.jecrc.portal.modules.interview.entity.RoundCandidate;
import edu.jecrc.portal.modules.interview.repository.InterviewRoundRepository;
import edu.jecrc.portal.modules.interview.repository.RoundCandidateRepository;
import edu.jecrc.portal.modules.interview.repository.CandidateInterviewAppointmentRepository;
import edu.jecrc.portal.modules.placement.entity.*;
import edu.jecrc.portal.modules.placement.repository.*;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class InterviewSelectionIntegrationTest {

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
    private JobApplicationRepository applicationRepository;

    @Autowired
    private EligibilityEvaluationRepository evaluationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AcademicProgramRepository programRepository;

    @Autowired
    private InterviewRoundRepository roundRepository;

    @Autowired
    private RoundCandidateRepository roundCandidateRepository;

    @Autowired
    private CandidateInterviewAppointmentRepository appointmentRepository;

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

    private JobProfile setupCompanyAndProfile(String compName, String profileName) {
        AcademicProgram cseProg = programRepository.findByProgramCode("BTECH_CSE").orElseThrow();

        Company company = companyRepository.save(Company.builder()
                .companyName(compName)
                .companyCode("COMP_" + System.currentTimeMillis())
                .build());

        PlacementPolicy policy = policyRepository.save(PlacementPolicy.builder()
                .policyCode("POL_" + System.currentTimeMillis())
                .policyName("Open Policy")
                .versionNumber(1)
                .policyStatus("PUBLISHED")
                .minCgpa(new BigDecimal("6.00"))
                .minClass10Percentage(new BigDecimal("60.00"))
                .minClass12Percentage(new BigDecimal("60.00"))
                .maxActiveBacklogs(10)
                .maxHistoricalBacklogs(10)
                .eligiblePrograms(java.util.Set.of(cseProg))
                .build());

        PlacementDrive drive = driveRepository.save(PlacementDrive.builder()
                .company(company)
                .driveTitle(compName + " Recruitment 2026")
                .academicYear("2025-2026")
                .driveStatus("PUBLISHED")
                .registrationStartDate(Instant.now().minusSeconds(1000))
                .registrationDeadline(Instant.now().plusSeconds(86400 * 5))
                .build());

        return jobProfileRepository.save(JobProfile.builder()
                .drive(drive)
                .policy(policy)
                .profileTitle(profileName)
                .employmentType("FULL_TIME")
                .totalCtcLpa(new BigDecimal("15.00"))
                .build());
    }

    @Test
    @DisplayName("1. Round configuration: creation, sequence uniqueness, and invalid update rejection")
    void testRoundConfigurationAndSequenceValidation() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        JobProfile profile = setupCompanyAndProfile("Microsoft IDC", "Software Engineer");

        InterviewRoundCreateRequest round1 = InterviewRoundCreateRequest.builder()
                .roundName("Online Technical Assessment")
                .roundType("ONLINE_ASSESSMENT")
                .sequenceOrder(1)
                .instructions("60 mins coding and CS fundamentals on HackerRank")
                .maxScore(new BigDecimal("100.00"))
                .build();

        // 1. TPO can create Round 1
        MvcResult res1 = mockMvc.perform(post("/api/v1/job-profiles/" + profile.getId() + "/rounds")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(round1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roundName").value("Online Technical Assessment"))
                .andExpect(jsonPath("$.data.sequenceOrder").value(1))
                .andExpect(jsonPath("$.data.publicationStatus").value("DRAFT"))
                .andReturn();

        // 2. Duplicate sequence order rejected with 400
        mockMvc.perform(post("/api/v1/job-profiles/" + profile.getId() + "/rounds")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(round1)))
                .andExpect(status().isBadRequest());

        // 3. Student is forbidden from creating rounds
        mockMvc.perform(post("/api/v1/job-profiles/" + profile.getId() + "/rounds")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(round1)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("2. Evaluator assignment and evaluator access isolation")
    void testEvaluatorAssignmentAndAccessControl() throws Exception {
        User facultyUser = userRepository.findByEmailIgnoreCase("faculty.cse@jecrcuniversity.edu.in").orElseThrow();
        facultyUser.setIsSuspended(false);
        facultyUser.setIsActive(true);
        userRepository.save(facultyUser);

        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String facultyToken = getAccessToken("faculty.cse@jecrcuniversity.edu.in", "Password@123");

        JobProfile profile = setupCompanyAndProfile("Cisco Systems", "Network Engineer");

        InterviewRound round = roundRepository.save(InterviewRound.builder()
                .jobProfile(profile)
                .roundName("Technical Interview 1")
                .roundType("TECHNICAL_INTERVIEW")
                .sequenceOrder(1)
                .publicationStatus("CONFIGURED")
                .build());

        // 1. Assign faculty user as round evaluator
        RoundEvaluatorAssignRequest assignReq = RoundEvaluatorAssignRequest.builder()
                .userId(facultyUser.getId())
                .roleOrDesignation("Technical Panel Lead")
                .build();

        mockMvc.perform(post("/api/v1/rounds/" + round.getId() + "/evaluators")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evaluatorEmail").value("faculty.cse@jecrcuniversity.edu.in"));

        // 2. Evaluator can view candidate roster for assigned round
        mockMvc.perform(get("/api/v1/rounds/" + round.getId() + "/candidates")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk());

        // 3. Unassigned round access for evaluator returns 403 Forbidden
        InterviewRound unassignedRound = roundRepository.save(InterviewRound.builder()
                .jobProfile(profile)
                .roundName("Executive Round")
                .roundType("HR_INTERVIEW")
                .sequenceOrder(2)
                .publicationStatus("CONFIGURED")
                .build());

        mockMvc.perform(get("/api/v1/rounds/" + unassignedRound.getId() + "/candidates")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("3. Candidate progression, bulk result upload, and publishing with final selection")
    void testCandidateProgressionAndResultPublishing() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        Student aarav = studentRepository.findByRollNumber("22BCSE101").orElseThrow();

        JobProfile profile = setupCompanyAndProfile("Adobe Systems", "Member of Technical Staff");

        // Seed evaluation & application
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
                .applicationStatus("APPLIED")
                .resumeVersionTag("RESUME_AARAV_2026")
                .build());

        // Create Round 1 (Online Assessment)
        InterviewRound round1 = roundRepository.save(InterviewRound.builder()
                .jobProfile(profile)
                .roundName("Coding Assessment")
                .roundType("CODING_ASSESSMENT")
                .sequenceOrder(1)
                .publicationStatus("CONFIGURED")
                .build());

        // 1. Shortlist candidate into Round 1
        CandidateShortlistRequest shortlistReq = CandidateShortlistRequest.builder()
                .applicationIds(List.of(app.getId()))
                .remarks("Cleared resume screening")
                .build();

        mockMvc.perform(post("/api/v1/rounds/" + round1.getId() + "/candidates/shortlist")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortlistReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].candidateRoundStatus").value("SCHEDULED"));

        // 2. Bulk result upload with status ADVANCED
        BulkCandidateResultUploadRequest bulkReq = BulkCandidateResultUploadRequest.builder()
                .results(List.of(
                        BulkCandidateResultUploadRequest.ResultRow.builder()
                                .rowNumber(1)
                                .applicationId(app.getId())
                                .status("ADVANCED")
                                .score(new BigDecimal("95.00"))
                                .remarks("Outstanding performance in dynamic programming")
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/rounds/" + round1.getId() + "/evaluations/bulk")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successfulCount").value(1))
                .andExpect(jsonPath("$.data.failedCount").value(0));

        // 3. Publish results for Round 1
        mockMvc.perform(post("/api/v1/rounds/" + round1.getId() + "/results/publish")
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isResultsPublished").value(true));

        // Since Round 1 is the only round, application advances to SELECTED
        JobApplication updatedApp = applicationRepository.findById(app.getId()).orElseThrow();
        assertThat(updatedApp.getApplicationStatus()).isEqualTo("SELECTED");

        // 4. Verify candidate status history is recorded
        mockMvc.perform(get("/api/v1/applications/" + app.getId() + "/timeline")
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].toStatus").value("SELECTED"));
    }

    @Test
    @DisplayName("4. Interview scheduling and conflict detection rejection")
    void testInterviewSchedulingAndConflictDetection() throws Exception {
        appointmentRepository.deleteAll();
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        Student aarav = studentRepository.findByRollNumber("22BCSE101").orElseThrow();

        JobProfile profile1 = setupCompanyAndProfile("Google India", "SDE 1");
        JobProfile profile2 = setupCompanyAndProfile("Amazon AWS", "Cloud Engineer");

        // Setup candidate 1 in Google
        EligibilityEvaluation eval1 = evaluationRepository.save(EligibilityEvaluation.builder()
                .student(aarav).jobProfile(profile1).policy(profile1.getPolicy()).policyVersion(1).outcome("ELIGIBLE").criteriaSnapshotJson("{}").build());
        JobApplication app1 = applicationRepository.save(JobApplication.builder()
                .jobProfile(profile1).student(aarav).evaluation(eval1).applicationStatus("IN_ROUNDS").resumeVersionTag("TAG1").build());
        InterviewRound round1 = roundRepository.save(InterviewRound.builder()
                .jobProfile(profile1).roundName("Technical Interview").roundType("TECHNICAL_INTERVIEW").sequenceOrder(1).build());
        RoundCandidate rc1 = roundCandidateRepository.save(RoundCandidate.builder().round(round1).application(app1).build());

        // Setup candidate 2 in Amazon
        EligibilityEvaluation eval2 = evaluationRepository.save(EligibilityEvaluation.builder()
                .student(aarav).jobProfile(profile2).policy(profile2.getPolicy()).policyVersion(1).outcome("ELIGIBLE").criteriaSnapshotJson("{}").build());
        JobApplication app2 = applicationRepository.save(JobApplication.builder()
                .jobProfile(profile2).student(aarav).evaluation(eval2).applicationStatus("IN_ROUNDS").resumeVersionTag("TAG2").build());
        InterviewRound round2 = roundRepository.save(InterviewRound.builder()
                .jobProfile(profile2).roundName("Technical Bar Raiser").roundType("TECHNICAL_INTERVIEW").sequenceOrder(1).build());
        RoundCandidate rc2 = roundCandidateRepository.save(RoundCandidate.builder().round(round2).application(app2).build());

        Instant interviewTime = Instant.now().plusSeconds(86400 * 2);

        // 1. Schedule appointment for Google
        InterviewAppointmentCreateRequest appt1 = InterviewAppointmentCreateRequest.builder()
                .roundCandidateId(rc1.getId())
                .startTime(interviewTime)
                .endTime(interviewTime.plusSeconds(3600)) // 1 hour
                .venueOrMeetingLink("https://meet.google.com/abc-defg-hij")
                .build();

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appt1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));

        // 2. Schedule overlapping appointment for Amazon for the same student -> Conflict detected & rejected
        InterviewAppointmentCreateRequest conflictingAppt = InterviewAppointmentCreateRequest.builder()
                .roundCandidateId(rc2.getId())
                .startTime(interviewTime.plusSeconds(1800)) // Overlaps by 30 mins
                .endTime(interviewTime.plusSeconds(5400))
                .venueOrMeetingLink("https://chime.aws/123456")
                .build();

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictingAppt)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Student conflict detected")));
    }

    @Test
    @DisplayName("5. Question bank: creation, search, and student confidential masking")
    void testQuestionBankPermissionsAndConfidentiality() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        InterviewQuestionCreateRequest questionReq = InterviewQuestionCreateRequest.builder()
                .title("Explain LRU Cache Design")
                .questionText("Design and implement a Data Structure for Least Recently Used (LRU) cache with get and put in O(1).")
                .questionType("CODING_QUESTION")
                .category("Data Structures")
                .difficulty("MEDIUM")
                .tags("hashmap,doubly-linked-list,lru")
                .expectedAnswerGuidance("Candidate should propose Doubly Linked List paired with HashMap. Watch for synchronization in multithreaded context.")
                .isConfidential(true)
                .build();

        // 1. TPO can create question
        MvcResult qResult = mockMvc.perform(post("/api/v1/interview-questions")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Explain LRU Cache Design"))
                .andReturn();

        // 2. TPO can view expectedAnswerGuidance
        mockMvc.perform(get("/api/v1/interview-questions?query=LRU")
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].expectedAnswerGuidance").isNotEmpty());

        // 3. Student searching question bank receives masked / null expectedAnswerGuidance
        mockMvc.perform(get("/api/v1/interview-questions?query=LRU")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].expectedAnswerGuidance").doesNotExist());
    }

    @Test
    @DisplayName("6. Recruitment feedback workflow: student submission and privacy isolation")
    void testRecruitmentFeedbackWorkflow() throws Exception {
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        Student aarav = studentRepository.findByRollNumber("22BCSE101").orElseThrow();

        JobProfile profile = setupCompanyAndProfile("Goldman Sachs", "Analyst");

        EligibilityEvaluation eval = evaluationRepository.save(EligibilityEvaluation.builder()
                .student(aarav).jobProfile(profile).policy(profile.getPolicy()).policyVersion(1).outcome("ELIGIBLE").criteriaSnapshotJson("{}").build());
        JobApplication app = applicationRepository.save(JobApplication.builder()
                .jobProfile(profile).student(aarav).evaluation(eval).applicationStatus("IN_ROUNDS").resumeVersionTag("TAG_GS").build());

        // 1. Student submits feedback for their interview experience
        InterviewFeedbackCreateRequest fbReq = InterviewFeedbackCreateRequest.builder()
                .feedbackType("STUDENT_TO_DRIVE")
                .applicationId(app.getId())
                .rating(5)
                .comments("Very professional interview panel and transparent process.")
                .isConfidentialToStaff(false)
                .build();

        mockMvc.perform(post("/api/v1/interview-feedback")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fbReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.feedbackType").value("STUDENT_TO_DRIVE"));

        // 2. Student can retrieve their own application feedback
        mockMvc.perform(get("/api/v1/applications/" + app.getId() + "/feedback")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].comments").value("Very professional interview panel and transparent process."));

        // 3. TPO can search all feedback
        mockMvc.perform(get("/api/v1/interview-feedback")
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
