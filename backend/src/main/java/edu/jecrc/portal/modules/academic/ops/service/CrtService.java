package edu.jecrc.portal.modules.academic.ops.service;

import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.ops.dto.*;
import edu.jecrc.portal.modules.academic.ops.entity.CrtEnrollment;
import edu.jecrc.portal.modules.academic.ops.entity.CrtProgram;
import edu.jecrc.portal.modules.academic.ops.entity.CrtSession;
import edu.jecrc.portal.modules.academic.ops.repository.CrtEnrollmentRepository;
import edu.jecrc.portal.modules.academic.ops.repository.CrtProgramRepository;
import edu.jecrc.portal.modules.academic.ops.repository.CrtSessionRepository;
import edu.jecrc.portal.modules.academic.repository.AcademicBatchRepository;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrtService {

    private final CrtProgramRepository crtProgramRepository;
    private final CrtSessionRepository crtSessionRepository;
    private final CrtEnrollmentRepository crtEnrollmentRepository;
    private final AcademicBatchRepository batchRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Transactional
    public CrtProgramDto createProgram(CrtProgramCreateRequest req, User caller) {
        if (crtProgramRepository.findByProgramCodeIgnoreCase(req.programCode()).isPresent()) {
            throw ApiException.conflict("CRT Program with code already exists: " + req.programCode());
        }

        AcademicBatch batch = null;
        if (req.targetBatchId() != null) {
            batch = batchRepository.findById(req.targetBatchId()).orElse(null);
        }

        String year = (req.academicYear() != null && !req.academicYear().isBlank()) ? req.academicYear() : "2025-2026";

        CrtProgram program = CrtProgram.builder()
                .programCode(req.programCode().toUpperCase().trim())
                .programName(req.programName().trim())
                .description(req.description())
                .academicYear(year)
                .targetBatch(batch)
                .isActive(true)
                .build();

        CrtProgram saved = crtProgramRepository.save(program);

        auditService.logAsync(
                caller != null ? caller.getId() : null,
                caller != null ? caller.getEmail() : "SYSTEM",
                "CRT_PROGRAM_CREATE",
                "CRT_PROGRAM",
                saved.getId().toString(),
                Map.of("code", saved.getProgramCode(), "name", saved.getProgramName()),
                "127.0.0.1", "API"
        );

        return CrtProgramDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CrtProgramDto> listPrograms(String academicYear) {
        List<CrtProgram> programs = (academicYear != null && !academicYear.isBlank())
                ? crtProgramRepository.findByAcademicYearAndIsActiveTrue(academicYear)
                : crtProgramRepository.findByIsActiveTrueOrderByProgramNameAsc();
        return programs.stream().map(CrtProgramDto::from).toList();
    }

    @Transactional
    public CrtSessionDto createSession(CrtSessionCreateRequest req, User caller) {
        if (!req.startTime().isBefore(req.endTime())) {
            throw ApiException.badRequest("CRT session start time must be before end time");
        }

        CrtProgram program = crtProgramRepository.findById(req.programId())
                .orElseThrow(() -> new ResourceNotFoundException("CRT Program not found: " + req.programId()));

        CrtSession session = CrtSession.builder()
                .crtProgram(program)
                .title(req.title().trim())
                .category(req.category().toUpperCase().trim())
                .sessionDate(req.sessionDate())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .venue(req.venue())
                .trainerName(req.trainerName())
                .trainerEmail(req.trainerEmail())
                .maxSeats(req.maxSeats())
                .status("SCHEDULED")
                .build();

        CrtSession saved = crtSessionRepository.save(session);

        auditService.logAsync(
                caller != null ? caller.getId() : null,
                caller != null ? caller.getEmail() : "SYSTEM",
                "CRT_SESSION_CREATE",
                "CRT_SESSION",
                saved.getId().toString(),
                Map.of("title", saved.getTitle(), "program", program.getProgramCode()),
                "127.0.0.1", "API"
        );

        return CrtSessionDto.from(saved, 0);
    }

    @Transactional(readOnly = true)
    public List<CrtSessionDto> listSessions(UUID programId, String status) {
        List<CrtSession> sessions;
        if (programId != null) {
            sessions = crtSessionRepository.findByCrtProgramIdOrderBySessionDateAscStartTimeAsc(programId);
        } else if (status != null && !status.isBlank()) {
            sessions = crtSessionRepository.findByStatusOrderBySessionDateAscStartTimeAsc(status.toUpperCase().trim());
        } else {
            sessions = crtSessionRepository.findAll();
        }

        return sessions.stream()
                .map(s -> CrtSessionDto.from(s, crtEnrollmentRepository.countBySessionId(s.getId())))
                .toList();
    }

    @Transactional
    public CrtEnrollmentDto registerStudent(UUID sessionId, UUID studentId, User caller) {
        CrtSession session = crtSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("CRT session not found: " + sessionId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        if (crtEnrollmentRepository.findBySessionIdAndStudentId(sessionId, studentId).isPresent()) {
            throw ApiException.conflict("Student is already registered for this CRT session");
        }

        if (session.getMaxSeats() != null) {
            long currentCount = crtEnrollmentRepository.countBySessionId(sessionId);
            if (currentCount >= session.getMaxSeats()) {
                throw ApiException.conflict("CRT session capacity reached (" + session.getMaxSeats() + " seats)");
            }
        }

        CrtEnrollment enrollment = CrtEnrollment.builder()
                .session(session)
                .student(student)
                .attendanceStatus("REGISTERED")
                .createdAt(Instant.now())
                .build();

        CrtEnrollment saved = crtEnrollmentRepository.save(enrollment);
        return CrtEnrollmentDto.from(saved);
    }

    @Transactional
    public CrtEnrollmentDto submitAttendanceAndScore(UUID sessionId, CrtAttendanceScoreSubmitRequest req, User caller) {
        CrtEnrollment enrollment = crtEnrollmentRepository.findBySessionIdAndStudentId(sessionId, req.studentId())
                .orElseGet(() -> {
                    CrtSession session = crtSessionRepository.findById(sessionId)
                            .orElseThrow(() -> new ResourceNotFoundException("CRT session not found: " + sessionId));
                    Student student = studentRepository.findById(req.studentId())
                            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + req.studentId()));
                    return CrtEnrollment.builder()
                            .session(session)
                            .student(student)
                            .build();
                });

        enrollment.setAttendanceStatus(req.attendanceStatus().toUpperCase().trim());
        enrollment.setScore(req.score());
        enrollment.setFeedbackRating(req.feedbackRating());
        enrollment.setFeedbackComments(req.feedbackComments());
        enrollment.setMarkedAt(Instant.now());

        CrtEnrollment saved = crtEnrollmentRepository.save(enrollment);
        return CrtEnrollmentDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CrtEnrollmentDto> listEnrollments(UUID sessionId) {
        return crtEnrollmentRepository.findBySessionId(sessionId).stream()
                .map(CrtEnrollmentDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CrtEnrollmentDto> getStudentEnrollments(UUID studentId) {
        return crtEnrollmentRepository.findByStudentId(studentId).stream()
                .map(CrtEnrollmentDto::from)
                .toList();
    }
}
