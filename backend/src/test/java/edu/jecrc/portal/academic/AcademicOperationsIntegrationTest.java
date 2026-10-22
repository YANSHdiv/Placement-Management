package edu.jecrc.portal.academic;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.ops.dto.*;
import edu.jecrc.portal.modules.academic.ops.entity.*;
import edu.jecrc.portal.modules.academic.ops.repository.*;
import edu.jecrc.portal.modules.academic.repository.AcademicBatchRepository;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.auth.dto.LoginRequest;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class AcademicOperationsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TimetableSlotRepository timetableSlotRepository;

    @Autowired
    private AttendanceCorrectionHistoryRepository correctionRepository;

    @Autowired
    private AttendanceRecordRepository recordRepository;

    @Autowired
    private AttendanceSessionRepository sessionRepository;

    @Autowired
    private CrtEnrollmentRepository crtEnrollmentRepository;

    @Autowired
    private CrtSessionRepository crtSessionRepository;

    @Autowired
    private CrtProgramRepository crtProgramRepository;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private PortalEventRepository portalEventRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationOutboxRepository outboxRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private AcademicSubjectRepository subjectRepository;

    @Autowired
    private FacultyProfileRepository facultyProfileRepository;

    @Autowired
    private FacultyTeachingAssignmentRepository assignmentRepository;

    @Autowired
    private AcademicBatchRepository batchRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        outboxRepository.deleteAll();
        eventRegistrationRepository.deleteAll();
        portalEventRepository.deleteAll();
        crtEnrollmentRepository.deleteAll();
        crtSessionRepository.deleteAll();
        crtProgramRepository.deleteAll();
        correctionRepository.deleteAll();
        recordRepository.deleteAll();
        sessionRepository.deleteAll();
        timetableSlotRepository.deleteAll();
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

    @Test
    @DisplayName("Milestone 6: Timetable GiST race-free exclusion constraints reject classroom, faculty, and cohort overlaps")
    void testTimetableGistExclusionConstraints() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");

        Classroom room101 = classroomRepository.findByRoomNumberIgnoreCase("LT-101").orElseThrow();
        Classroom room102 = classroomRepository.findByRoomNumberIgnoreCase("LT-102").orElseThrow();
        AcademicSubject dsa = subjectRepository.findBySubjectCodeIgnoreCase("CSE301").orElseThrow();
        AcademicSubject dbms = subjectRepository.findBySubjectCodeIgnoreCase("CSE302").orElseThrow();
        FacultyProfile faculty = facultyProfileRepository.findByFacultyCodeIgnoreCase("JECRC-FAC-CSE-001").orElseThrow();
        AcademicBatch batch = batchRepository.findAll().get(0);

        // 1. Create Slot 1: LT-101, Monday 09:00 - 10:00
        TimetableSlotCreateRequest slot1Req = new TimetableSlotCreateRequest(
                batch.getId(), 5, "A", dsa.getId(), faculty.getId(), room101.getId(),
                "MONDAY", LocalTime.of(9, 0), LocalTime.of(10, 0), "2025-2026", "PUBLISHED"
        );

        mockMvc.perform(post("/api/v1/timetables/slots")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(slot1Req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dayOfWeek").value("MONDAY"));

        // 2. Overlap Constraint 1: Same Classroom (LT-101) during Monday 09:30 - 10:30 -> Expect 409 Conflict
        TimetableSlotCreateRequest roomOverlapReq = new TimetableSlotCreateRequest(
                batch.getId(), 5, "B", dbms.getId(), faculty.getId(), room101.getId(),
                "MONDAY", LocalTime.of(9, 30), LocalTime.of(10, 30), "2025-2026", "PUBLISHED"
        );

        mockMvc.perform(post("/api/v1/timetables/slots")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomOverlapReq)))
                .andExpect(status().isConflict());

        // 3. Overlap Constraint 2: Same Faculty member during Monday 09:30 - 10:30 in Room LT-102 -> Expect 409 Conflict
        TimetableSlotCreateRequest facultyOverlapReq = new TimetableSlotCreateRequest(
                batch.getId(), 5, "B", dbms.getId(), faculty.getId(), room102.getId(),
                "MONDAY", LocalTime.of(9, 30), LocalTime.of(10, 30), "2025-2026", "PUBLISHED"
        );

        mockMvc.perform(post("/api/v1/timetables/slots")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyOverlapReq)))
                .andExpect(status().isConflict());

        // 4. Overlap Constraint 3: Same Student Section (Batch, Sem 5, Sec A) during Monday 09:30 - 10:30 -> Expect 409 Conflict
        TimetableSlotCreateRequest sectionOverlapReq = new TimetableSlotCreateRequest(
                batch.getId(), 5, "A", dbms.getId(), faculty.getId(), room102.getId(),
                "MONDAY", LocalTime.of(9, 30), LocalTime.of(10, 30), "2025-2026", "PUBLISHED"
        );

        mockMvc.perform(post("/api/v1/timetables/slots")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sectionOverlapReq)))
                .andExpect(status().isConflict());

        // 5. Half-open interval test: Adjacent non-overlapping Slot (Monday 10:00 - 11:00) MUST SUCCEED
        TimetableSlotCreateRequest adjacentSlotReq = new TimetableSlotCreateRequest(
                batch.getId(), 5, "A", dbms.getId(), faculty.getId(), room101.getId(),
                "MONDAY", LocalTime.of(10, 0), LocalTime.of(11, 0), "2025-2026", "PUBLISHED"
        );

        mockMvc.perform(post("/api/v1/timetables/slots")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjacentSlotReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.startTime").value("10:00:00"));

        List<TimetableSlot> slots = timetableSlotRepository.findAll();
        assertThat(slots).hasSize(2);
    }

    @Test
    @DisplayName("Milestone 6: Attendance session workflow, geotagged Haversine validation, finalization, and correction history")
    void testAttendanceWorkflowAndGeotagging() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String facultyToken = getAccessToken("faculty.cse@jecrcuniversity.edu.in", "Password@123");

        AcademicSubject dsa = subjectRepository.findBySubjectCodeIgnoreCase("CSE301").orElseThrow();
        Student student = studentRepository.findByRollNumber("22BCSE101").orElseThrow();
        AcademicBatch batch = student.getBatch() != null ? student.getBatch() : batchRepository.findAll().get(0);
        int semester = student.getCurrentSemester() != null ? student.getCurrentSemester() : 5;
        String section = (student.getSection() != null && !student.getSection().isBlank()) ? student.getSection() : "A";

        // 1. Open Attendance Session with Geofencing enabled
        AttendanceSessionOpenRequest openReq = new AttendanceSessionOpenRequest(
                null,
                dsa.getId(),
                batch.getId(),
                semester,
                section,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                "Algorithm Asymptotic Analysis & Recurrences",
                true,
                BigDecimal.valueOf(26.7797820), // JECRC University Campus Baseline
                BigDecimal.valueOf(75.8778640),
                60 // 60 meters radius
        );

        MvcResult sessionRes = mockMvc.perform(post("/api/v1/attendance/sessions")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(openReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionStatus").value("OPEN"))
                .andExpect(jsonPath("$.data.isGeofenceEnabled").value(true))
                .andReturn();

        UUID sessionId = UUID.fromString(objectMapper.readTree(sessionRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Verify pre-initialized roster
        List<AttendanceRecord> preRoster = recordRepository.findBySessionId(sessionId);
        assertThat(preRoster).isNotEmpty();

        // 2. Submit Geotagged Attendance WITHIN Geofence (Distance approx 15m)
        AttendanceRecordSubmitRequest withinReq = new AttendanceRecordSubmitRequest(
                student.getId(),
                "PRESENT",
                "GEOTAGGED_SELF",
                BigDecimal.valueOf(26.7798000), // very close to 26.7797820
                BigDecimal.valueOf(75.8778800), // very close to 75.8778640
                BigDecimal.valueOf(5.0),
                "evidence_photo_hash_101.jpg",
                "Attended on-site"
        );

        mockMvc.perform(post("/api/v1/attendance/sessions/" + sessionId + "/records")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withinReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceStatus").value("PRESENT"))
                .andExpect(jsonPath("$.data.geofenceStatus").value("WITHIN_GEOFENCE"));

        // 3. Submit Geotagged Attendance OUTSIDE Geofence (Distance approx 1km away)
        AttendanceRecordSubmitRequest outsideReq = new AttendanceRecordSubmitRequest(
                student.getId(),
                "PRESENT",
                "GEOTAGGED_SELF",
                BigDecimal.valueOf(26.7888000), // ~1000m away
                BigDecimal.valueOf(75.8888000),
                BigDecimal.valueOf(10.0),
                "evidence_photo_spoof.jpg",
                "Student claiming remote attendance"
        );

        mockMvc.perform(post("/api/v1/attendance/sessions/" + sessionId + "/records")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outsideReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.geofenceStatus").value("OUTSIDE_GEOFENCE"));

        // 4. Finalize the Attendance Session
        mockMvc.perform(post("/api/v1/attendance/sessions/" + sessionId + "/finalize")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionStatus").value("FINALIZED"));

        // 5. Attempt direct edit after finalization -> Must be rejected (400 Bad Request)
        mockMvc.perform(post("/api/v1/attendance/sessions/" + sessionId + "/records")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withinReq)))
                .andExpect(status().isBadRequest());

        // 6. Formal Attendance Correction with mandatory justification reason
        AttendanceRecord record = recordRepository.findBySessionIdAndStudentId(sessionId, student.getId()).orElseThrow();

        AttendanceCorrectionRequest correctionReq = new AttendanceCorrectionRequest(
                "EXCUSED",
                "Medical certificate verified by HOD CSE for university sports meet"
        );

        MvcResult correctRes = mockMvc.perform(post("/api/v1/attendance/records/" + record.getId() + "/correct")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctionReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceStatus").value("EXCUSED"))
                .andExpect(jsonPath("$.data.isCorrected").value(true))
                .andExpect(jsonPath("$.data.originalStatus").value("PRESENT"))
                .andReturn();

        // 7. Verify immutable correction history audit trail
        mockMvc.perform(get("/api/v1/attendance/records/" + record.getId() + "/history")
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].previousStatus").value("PRESENT"))
                .andExpect(jsonPath("$.data[0].newStatus").value("EXCUSED"))
                .andExpect(jsonPath("$.data[0].correctionReason").value("Medical certificate verified by HOD CSE for university sports meet"));
    }

    @Test
    @DisplayName("Milestone 6: Attendance shortage report and student summary metrics")
    void testAttendanceShortageAndAnalytics() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        AcademicSubject dsa = subjectRepository.findBySubjectCodeIgnoreCase("CSE301").orElseThrow();
        AcademicBatch batch = batchRepository.findAll().get(0);
        Student student = studentRepository.findByRollNumber("22BCSE101").orElseThrow();
        FacultyProfile faculty = facultyProfileRepository.findByFacultyCodeIgnoreCase("JECRC-FAC-CSE-001").orElseThrow();

        // Create 4 sessions: Student attends 2 out of 4 (50% attendance, below 75% threshold)
        for (int i = 1; i <= 4; i++) {
            AttendanceSession session = AttendanceSession.builder()
                    .subject(dsa)
                    .faculty(faculty)
                    .batch(batch)
                    .semester(5)
                    .section("A")
                    .sessionDate(LocalDate.now().minusDays(i))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 0))
                    .sessionStatus("FINALIZED")
                    .build();
            session = sessionRepository.save(session);

            AttendanceRecord record = AttendanceRecord.builder()
                    .session(session)
                    .student(student)
                    .attendanceStatus(i <= 2 ? "PRESENT" : "ABSENT")
                    .build();
            recordRepository.save(record);
        }

        // 1. Query Student Personal Summary via /api/v1/attendance/me
        mockMvc.perform(get("/api/v1/attendance/me")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rollNumber").value("22BCSE101"))
                .andExpect(jsonPath("$.data.totalSessions").value(4))
                .andExpect(jsonPath("$.data.attendedSessions").value(2))
                .andExpect(jsonPath("$.data.overallPercentage").value(50.0));

        // 2. Query Shortage Report for Cohort (Threshold 75%)
        MvcResult shortageRes = mockMvc.perform(get("/api/v1/attendance/reports/shortage")
                        .header("Authorization", "Bearer " + tpoToken)
                        .param("batchId", batch.getId().toString())
                        .param("semester", "5")
                        .param("section", "A")
                        .param("thresholdPercentage", "75.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        List<?> shortages = objectMapper.readTree(shortageRes.getResponse().getContentAsString())
                .path("data").findValues("classesNeededToReachThreshold");
        assertThat(shortages).isNotEmpty();
    }

    @Test
    @DisplayName("Milestone 6: Campus Recruitment Training (CRT) separate tracks, session scoring, and student enrollment")
    void testCrtProgramAndEnrollment() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        AcademicBatch batch = batchRepository.findAll().get(0);
        Student student = studentRepository.findByRollNumber("22BCSE101").orElseThrow();

        // 1. Create CRT Program Track
        CrtProgramCreateRequest progReq = new CrtProgramCreateRequest(
                "CRT-2026-APTI",
                "Advanced Quantitative Aptitude & Reasoning Track",
                "Intensive quantitative aptitude training for product and service tier companies",
                "2025-2026",
                batch.getId()
        );

        MvcResult progRes = mockMvc.perform(post("/api/v1/crt/programs")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.programCode").value("CRT-2026-APTI"))
                .andReturn();

        UUID programId = UUID.fromString(objectMapper.readTree(progRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 2. Schedule CRT Session
        CrtSessionCreateRequest sessionReq = new CrtSessionCreateRequest(
                programId,
                "Dynamic Programming & Greedy Algorithms Masterclass",
                "CODING",
                LocalDate.now().plusDays(2),
                LocalTime.of(14, 0),
                LocalTime.of(16, 0),
                "Central Auditorium Block",
                "Er. Vikas Sharma",
                "vikas.sharma@jecrcuniversity.edu.in",
                120
        );

        MvcResult sessionRes = mockMvc.perform(post("/api/v1/crt/sessions")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Dynamic Programming & Greedy Algorithms Masterclass"))
                .andReturn();

        UUID sessionId = UUID.fromString(objectMapper.readTree(sessionRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 3. Student registers for CRT session
        mockMvc.perform(post("/api/v1/crt/sessions/" + sessionId + "/register")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceStatus").value("REGISTERED"));

        // 4. Submit CRT assessment score & attendance
        CrtAttendanceScoreSubmitRequest scoreReq = new CrtAttendanceScoreSubmitRequest(
                student.getId(),
                "ATTENDED",
                BigDecimal.valueOf(92.50),
                5,
                "Exceptional algorithmic problem-solving ability"
        );

        mockMvc.perform(post("/api/v1/crt/sessions/" + sessionId + "/scores")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scoreReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(92.50))
                .andExpect(jsonPath("$.data.attendanceStatus").value("ATTENDED"));

        // 5. Verify student can view personal CRT records
        mockMvc.perform(get("/api/v1/crt/me")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].score").value(92.50));
    }

    @Test
    @DisplayName("Milestone 6: Events lifecycle, publication, and student registration")
    void testEventsLifecycleAndRegistration() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        // 1. Create Event Draft
        PortalEventCreateRequest eventReq = new PortalEventCreateRequest(
                "Google Cloud & Generative AI Workshop 2026",
                "WORKSHOP",
                "Hands-on workshop on Google Cloud Vertex AI and Gemini APIs",
                LocalDate.now().plusDays(5),
                LocalTime.of(11, 0),
                LocalTime.of(13, 0),
                "Engineering Wing Auditorium",
                "https://meet.google.com/jecrc-genai-2026",
                "ALL",
                null,
                null,
                null,
                200,
                "Google Developer Student Club JECRC"
        );

        MvcResult createRes = mockMvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        UUID eventId = UUID.fromString(objectMapper.readTree(createRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 2. Publish Event
        mockMvc.perform(put("/api/v1/events/" + eventId + "/publish")
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        // 3. Student Registers for Event
        mockMvc.perform(post("/api/v1/events/" + eventId + "/register")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceStatus").value("REGISTERED"));

        // 4. List event registrations
        mockMvc.perform(get("/api/v1/events/" + eventId + "/registrations")
                        .header("Authorization", "Bearer " + tpoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].rollNumber").value("22BCSE101"));
    }

    @Test
    @DisplayName("Milestone 6: Notifications feed, transactional outbox recording, and unread counts")
    void testNotificationsAndTransactionalOutbox() throws Exception {
        String tpoToken = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        User studentUser = userRepository.findByEmailIgnoreCase("student.cse@jecrcuniversity.edu.in").orElseThrow();

        // 1. Broadcast an announcement to student user
        NotificationBroadcastRequest broadcastReq = new NotificationBroadcastRequest(
                studentUser.getId(),
                "INDIVIDUAL",
                null,
                "Mid-Term Examination Schedule Released",
                "The academic schedule for B.Tech CSE Semester 5 has been finalized and published.",
                "ACADEMIC",
                "TIMETABLE",
                "2025-2026",
                "/timetables"
        );

        MvcResult broadcastRes = mockMvc.perform(post("/api/v1/notifications/broadcast")
                        .header("Authorization", "Bearer " + tpoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(broadcastReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Mid-Term Examination Schedule Released"))
                .andReturn();

        UUID notificationId = UUID.fromString(objectMapper.readTree(broadcastRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 2. Verify Transactional Outbox record exists with status PENDING
        List<NotificationOutbox> outboxRecords = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        assertThat(outboxRecords).isNotEmpty();
        assertThat(outboxRecords.get(0).getPayload()).contains("Mid-Term Examination Schedule Released");

        // 3. Check student's unread count
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(1));

        // 4. Student marks notification as read
        mockMvc.perform(put("/api/v1/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isRead").value(true));

        // 5. Unread count should now be 0
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(0));
    }
}
