package edu.jecrc.portal.modules.academic.ops.service;

import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.ops.dto.*;
import edu.jecrc.portal.modules.academic.ops.entity.*;
import edu.jecrc.portal.modules.academic.ops.repository.*;
import edu.jecrc.portal.modules.academic.repository.AcademicBatchRepository;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AttendanceCorrectionHistoryRepository correctionRepository;
    private final TimetableSlotRepository timetableSlotRepository;
    private final AcademicSubjectRepository subjectRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final AcademicBatchRepository batchRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Transactional
    public AttendanceSessionDto openSession(AttendanceSessionOpenRequest req, User caller) {
        AcademicSubject subject = subjectRepository.findById(req.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic subject not found: " + req.subjectId()));

        AcademicBatch batch = batchRepository.findById(req.batchId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic batch not found: " + req.batchId()));

        TimetableSlot slot = null;
        if (req.slotId() != null) {
            slot = timetableSlotRepository.findById(req.slotId()).orElse(null);
        }

        FacultyProfile faculty = null;
        if (slot != null && slot.getFaculty() != null) {
            faculty = slot.getFaculty();
        } else if (caller != null) {
            faculty = facultyProfileRepository.findByUserId(caller.getId()).orElse(null);
        }

        if (faculty == null) {
            faculty = facultyProfileRepository.findByIsActiveTrue().stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No active faculty profile found to assign attendance session"));
        }

        BigDecimal authLat = req.authorizedLatitude();
        BigDecimal authLon = req.authorizedLongitude();
        Integer geofenceRadius = req.geofenceRadiusMeters() != null ? req.geofenceRadiusMeters() : 60;

        if (authLat == null && slot != null && slot.getClassroom() != null) {
            authLat = slot.getClassroom().getLatitude();
            authLon = slot.getClassroom().getLongitude();
            if (slot.getClassroom().getGeofenceRadiusMeters() != null) {
                geofenceRadius = slot.getClassroom().getGeofenceRadiusMeters();
            }
        }

        AttendanceSession session = AttendanceSession.builder()
                .slot(slot)
                .subject(subject)
                .faculty(faculty)
                .batch(batch)
                .semester(req.semester())
                .section(req.section().toUpperCase().trim())
                .sessionDate(req.sessionDate())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .topicCovered(req.topicCovered())
                .sessionStatus("OPEN")
                .isGeofenceEnabled(Boolean.TRUE.equals(req.isGeofenceEnabled()))
                .authorizedLatitude(authLat)
                .authorizedLongitude(authLon)
                .geofenceRadiusMeters(geofenceRadius)
                .openedAt(Instant.now())
                .build();

        AttendanceSession saved = sessionRepository.save(session);

        // Pre-initialize roster for cohort students
        List<Student> cohortStudents = studentRepository.findByBatchIdAndCurrentSemesterAndSectionIgnoreCase(
                batch.getId(), req.semester(), req.section().trim()
        );
        if (cohortStudents.isEmpty()) {
            cohortStudents = studentRepository.findByBatchId(batch.getId());
        }

        for (Student student : cohortStudents) {
            AttendanceRecord record = AttendanceRecord.builder()
                    .session(saved)
                    .student(student)
                    .attendanceStatus("ABSENT") // Default until marked
                    .markedBy(caller)
                    .markedAt(Instant.now())
                    .verificationMethod("MANUAL_ROSTER")
                    .geofenceStatus("NOT_APPLICABLE")
                    .build();
            recordRepository.save(record);
        }

        auditService.logAsync(
                caller != null ? caller.getId() : null,
                caller != null ? caller.getEmail() : "SYSTEM",
                "ATTENDANCE_SESSION_OPEN",
                "ATTENDANCE_SESSION",
                saved.getId().toString(),
                Map.of("subject", subject.getSubjectCode(), "cohort", batch.getBatchName() + " Sem " + req.semester() + " Sec " + req.section()),
                "127.0.0.1", "API"
        );

        return AttendanceSessionDto.from(saved);
    }

    @Transactional(readOnly = true)
    public AttendanceSessionDto getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .map(AttendanceSessionDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found: " + sessionId));
    }

    @Transactional(readOnly = true)
    public Page<AttendanceSessionDto> listSessions(UUID facultyId, Integer subjectId, Integer batchId, String status, Pageable pageable) {
        return sessionRepository.findFiltered(facultyId, subjectId, batchId, status, pageable)
                .map(AttendanceSessionDto::from);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> listSessionRecords(UUID sessionId) {
        return recordRepository.findBySessionId(sessionId).stream()
                .map(AttendanceRecordDto::from)
                .toList();
    }

    @Transactional
    public AttendanceRecordDto submitRecord(UUID sessionId, AttendanceRecordSubmitRequest req, User caller) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found: " + sessionId));

        if ("FINALIZED".equalsIgnoreCase(session.getSessionStatus())) {
            throw ApiException.badRequest("Attendance session has already been finalized and locked against direct edits");
        }

        Student student = studentRepository.findById(req.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + req.studentId()));

        AttendanceRecord record = recordRepository.findBySessionIdAndStudentId(sessionId, req.studentId())
                .orElseGet(() -> AttendanceRecord.builder()
                        .session(session)
                        .student(student)
                        .build());

        record.setAttendanceStatus(req.attendanceStatus().toUpperCase().trim());
        record.setMarkedBy(caller);
        record.setMarkedAt(Instant.now());
        record.setRemarks(req.remarks());

        String verification = (req.verificationMethod() != null && !req.verificationMethod().isBlank())
                ? req.verificationMethod().toUpperCase().trim() : "MANUAL_ROSTER";
        record.setVerificationMethod(verification);
        record.setLatitude(req.latitude());
        record.setLongitude(req.longitude());
        record.setAccuracyMeters(req.accuracyMeters());
        record.setEvidencePhotoRef(req.evidencePhotoRef());

        // Server-side Geofence & Haversine Distance Calculation
        if (Boolean.TRUE.equals(session.getIsGeofenceEnabled())) {
            if (req.latitude() == null || req.longitude() == null) {
                record.setGeofenceStatus("LOCATION_UNAVAILABLE");
                record.setCalculatedDistanceMeters(null);
            } else if (session.getAuthorizedLatitude() == null || session.getAuthorizedLongitude() == null) {
                record.setGeofenceStatus("NOT_APPLICABLE");
                record.setCalculatedDistanceMeters(null);
            } else {
                BigDecimal distance = GeoUtils.calculateHaversineDistanceMeters(
                        req.latitude(), req.longitude(),
                        session.getAuthorizedLatitude(), session.getAuthorizedLongitude()
                );
                record.setCalculatedDistanceMeters(distance);

                if (distance != null && distance.compareTo(BigDecimal.valueOf(session.getGeofenceRadiusMeters())) <= 0) {
                    record.setGeofenceStatus("WITHIN_GEOFENCE");
                } else {
                    record.setGeofenceStatus("OUTSIDE_GEOFENCE");
                }
            }
        } else {
            record.setGeofenceStatus("NOT_APPLICABLE");
            record.setCalculatedDistanceMeters(null);
        }

        AttendanceRecord saved = recordRepository.save(record);
        return AttendanceRecordDto.from(saved);
    }

    @Transactional
    public List<AttendanceRecordDto> submitBatchRecords(UUID sessionId, AttendanceBatchSubmitRequest req, User caller) {
        return req.records().stream()
                .map(r -> submitRecord(sessionId, r, caller))
                .toList();
    }

    @Transactional
    public AttendanceSessionDto finalizeSession(UUID sessionId, User caller) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found: " + sessionId));

        if ("FINALIZED".equalsIgnoreCase(session.getSessionStatus())) {
            return AttendanceSessionDto.from(session);
        }

        session.setSessionStatus("FINALIZED");
        session.setFinalizedAt(Instant.now());
        session.setFinalizedBy(caller);

        AttendanceSession saved = sessionRepository.save(session);

        auditService.logAsync(
                caller != null ? caller.getId() : null,
                caller != null ? caller.getEmail() : "SYSTEM",
                "ATTENDANCE_SESSION_FINALIZE",
                "ATTENDANCE_SESSION",
                saved.getId().toString(),
                Map.of("status", "FINALIZED"),
                "127.0.0.1", "API"
        );

        return AttendanceSessionDto.from(saved);
    }

    @Transactional
    public AttendanceRecordDto correctRecord(UUID recordId, AttendanceCorrectionRequest req, User caller) {
        if (req.correctionReason() == null || req.correctionReason().trim().length() < 5) {
            throw ApiException.badRequest("A formal justification reason of at least 5 characters is mandatory for attendance corrections");
        }

        AttendanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found: " + recordId));

        String previousStatus = record.getAttendanceStatus();
        String newStatus = req.newStatus().toUpperCase().trim();

        // 1. Append to immutable correction audit trail
        AttendanceCorrectionHistory history = AttendanceCorrectionHistory.builder()
                .record(record)
                .session(record.getSession())
                .student(record.getStudent())
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .correctionReason(req.correctionReason().trim())
                .actorUser(caller)
                .createdAt(Instant.now())
                .build();
        correctionRepository.save(history);

        // 2. Update record
        record.setOriginalStatus(record.getOriginalStatus() != null ? record.getOriginalStatus() : previousStatus);
        record.setAttendanceStatus(newStatus);
        record.setIsCorrected(true);
        record.setCorrectionReason(req.correctionReason().trim());
        record.setCorrectedBy(caller);
        record.setCorrectedAt(Instant.now());

        AttendanceRecord saved = recordRepository.save(record);

        auditService.logAsync(
                caller != null ? caller.getId() : null,
                caller != null ? caller.getEmail() : "SYSTEM",
                "ATTENDANCE_RECORD_CORRECT",
                "ATTENDANCE_RECORD",
                saved.getId().toString(),
                Map.of("previousStatus", previousStatus, "newStatus", newStatus, "reason", req.correctionReason().trim()),
                "127.0.0.1", "API"
        );

        return AttendanceRecordDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AttendanceCorrectionHistoryDto> getRecordCorrectionHistory(UUID recordId) {
        return correctionRepository.findByRecordIdOrderByCreatedAtDesc(recordId).stream()
                .map(AttendanceCorrectionHistoryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentAttendanceSummaryDto getStudentSummary(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        List<AttendanceRecord> records = recordRepository.findByStudentId(studentId);

        long total = records.size();
        long attended = records.stream()
                .filter(r -> "PRESENT".equalsIgnoreCase(r.getAttendanceStatus()) || "LATE".equalsIgnoreCase(r.getAttendanceStatus()))
                .count();
        long absent = records.stream()
                .filter(r -> "ABSENT".equalsIgnoreCase(r.getAttendanceStatus()))
                .count();

        BigDecimal overallPct = total > 0
                ? BigDecimal.valueOf(attended * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Group by Subject
        Map<AcademicSubject, List<AttendanceRecord>> subjectGroups = records.stream()
                .collect(Collectors.groupingBy(r -> r.getSession().getSubject()));

        List<SubjectAttendanceReportDto> subjectReports = new ArrayList<>();
        for (Map.Entry<AcademicSubject, List<AttendanceRecord>> entry : subjectGroups.entrySet()) {
            AcademicSubject sub = entry.getKey();
            List<AttendanceRecord> subRecords = entry.getValue();
            long subTotal = subRecords.size();
            long subAttended = subRecords.stream()
                    .filter(r -> "PRESENT".equalsIgnoreCase(r.getAttendanceStatus()) || "LATE".equalsIgnoreCase(r.getAttendanceStatus()))
                    .count();
            long subAbsent = subRecords.stream().filter(r -> "ABSENT".equalsIgnoreCase(r.getAttendanceStatus())).count();
            long subLate = subRecords.stream().filter(r -> "LATE".equalsIgnoreCase(r.getAttendanceStatus())).count();
            long subExcused = subRecords.stream().filter(r -> "EXCUSED".equalsIgnoreCase(r.getAttendanceStatus())).count();

            BigDecimal subPct = subTotal > 0
                    ? BigDecimal.valueOf(subAttended * 100.0 / subTotal).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            subjectReports.add(SubjectAttendanceReportDto.builder()
                    .subjectId(sub.getId())
                    .subjectCode(sub.getSubjectCode())
                    .subjectName(sub.getSubjectName())
                    .totalSessions(subTotal)
                    .attendedSessions(subAttended)
                    .absentSessions(subAbsent)
                    .lateSessions(subLate)
                    .excusedSessions(subExcused)
                    .attendancePercentage(subPct)
                    .build());
        }

        return StudentAttendanceSummaryDto.builder()
                .studentId(student.getId())
                .rollNumber(student.getRollNumber())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .batchId(student.getBatch() != null ? student.getBatch().getId() : null)
                .batchName(student.getBatch() != null ? student.getBatch().getBatchName() : null)
                .semester(student.getCurrentSemester())
                .section(student.getSection())
                .totalSessions(total)
                .attendedSessions(attended)
                .absentSessions(absent)
                .overallPercentage(overallPct)
                .subjectReports(subjectReports)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AttendanceShortageReportDto> getShortageReport(
            Integer batchId, Integer semester, String section, Integer subjectId, BigDecimal thresholdPercentage
    ) {
        BigDecimal threshold = thresholdPercentage != null ? thresholdPercentage : BigDecimal.valueOf(75.0);
        List<AttendanceRecord> allRecords;

        if (batchId != null && semester != null && section != null) {
            allRecords = recordRepository.findByCohort(batchId, semester, section.toUpperCase().trim());
        } else {
            allRecords = recordRepository.findAll();
        }

        if (subjectId != null) {
            allRecords = allRecords.stream()
                    .filter(r -> r.getSession() != null && subjectId.equals(r.getSession().getSubject().getId()))
                    .toList();
        }

        // Group by Student + Subject
        record StudentSubjectKey(UUID studentId, Integer subjectId) {}
        Map<StudentSubjectKey, List<AttendanceRecord>> grouped = allRecords.stream()
                .collect(Collectors.groupingBy(r -> new StudentSubjectKey(r.getStudent().getId(), r.getSession().getSubject().getId())));

        List<AttendanceShortageReportDto> shortages = new ArrayList<>();

        for (Map.Entry<StudentSubjectKey, List<AttendanceRecord>> entry : grouped.entrySet()) {
            List<AttendanceRecord> groupRecords = entry.getValue();
            if (groupRecords.isEmpty()) continue;

            AttendanceRecord sample = groupRecords.get(0);
            Student student = sample.getStudent();
            AcademicSubject subject = sample.getSession().getSubject();

            long total = groupRecords.size();
            long attended = groupRecords.stream()
                    .filter(r -> "PRESENT".equalsIgnoreCase(r.getAttendanceStatus()) || "LATE".equalsIgnoreCase(r.getAttendanceStatus()))
                    .count();

            BigDecimal pct = total > 0
                    ? BigDecimal.valueOf(attended * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            if (pct.compareTo(threshold) < 0) {
                // Calculate consecutive classes needed to clear shortage: (T * N - A) / (1 - T)
                double t = threshold.doubleValue() / 100.0;
                double needed = (t * total - attended) / (1.0 - t);
                long classesNeeded = Math.max(0, (long) Math.ceil(needed));

                shortages.add(AttendanceShortageReportDto.builder()
                        .studentId(student.getId())
                        .rollNumber(student.getRollNumber())
                        .studentName(student.getFirstName() + " " + student.getLastName())
                        .batchId(student.getBatch() != null ? student.getBatch().getId() : null)
                        .batchName(student.getBatch() != null ? student.getBatch().getBatchName() : null)
                        .semester(student.getCurrentSemester())
                        .section(student.getSection())
                        .subjectId(subject.getId())
                        .subjectCode(subject.getSubjectCode())
                        .subjectName(subject.getSubjectName())
                        .totalSessions(total)
                        .attendedSessions(attended)
                        .attendancePercentage(pct)
                        .thresholdPercentage(threshold)
                        .classesNeededToReachThreshold(classesNeeded)
                        .build());
            }
        }

        return shortages;
    }
}
