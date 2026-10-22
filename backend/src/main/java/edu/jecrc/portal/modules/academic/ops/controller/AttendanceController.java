package edu.jecrc.portal.modules.academic.ops.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.ops.dto.*;
import edu.jecrc.portal.modules.academic.ops.service.AttendanceService;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Classroom Attendance", description = "Attendance sessions, geotagged validation, corrections, and shortage analytics")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    private User getCallerUser(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @PostMapping("/sessions")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    @Operation(summary = "Open an attendance session for a class")
    public ResponseEntity<ApiResponse<AttendanceSessionDto>> openSession(
            @Valid @RequestBody AttendanceSessionOpenRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Attendance session opened successfully",
                attendanceService.openSession(req, getCallerUser(auth))));
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get attendance session details")
    public ResponseEntity<ApiResponse<AttendanceSessionDto>> getSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(ApiResponse.success("Session retrieved",
                attendanceService.getSession(sessionId)));
    }

    @GetMapping("/sessions")
    @Operation(summary = "List attendance sessions")
    public ResponseEntity<PageResponse<AttendanceSessionDto>> listSessions(
            @RequestParam(required = false) UUID facultyId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer batchId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<AttendanceSessionDto> page = attendanceService.listSessions(facultyId, subjectId, batchId, status, pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    @GetMapping("/sessions/{sessionId}/records")
    @Operation(summary = "List student roster records for an attendance session")
    public ResponseEntity<ApiResponse<List<AttendanceRecordDto>>> listRecords(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved",
                attendanceService.listSessionRecords(sessionId)));
    }

    @PostMapping("/sessions/{sessionId}/records")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    @Operation(summary = "Submit/mark attendance for an individual student (supports geotagging)")
    public ResponseEntity<ApiResponse<AttendanceRecordDto>> submitRecord(
            @PathVariable UUID sessionId,
            @Valid @RequestBody AttendanceRecordSubmitRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Attendance recorded",
                attendanceService.submitRecord(sessionId, req, getCallerUser(auth))));
    }

    @PostMapping("/sessions/{sessionId}/records/batch")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    @Operation(summary = "Submit attendance roster for an entire class")
    public ResponseEntity<ApiResponse<List<AttendanceRecordDto>>> submitBatch(
            @PathVariable UUID sessionId,
            @Valid @RequestBody AttendanceBatchSubmitRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Batch attendance recorded",
                attendanceService.submitBatchRecords(sessionId, req, getCallerUser(auth))));
    }

    @PostMapping("/sessions/{sessionId}/finalize")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    @Operation(summary = "Finalize and lock an attendance session against direct modifications")
    public ResponseEntity<ApiResponse<AttendanceSessionDto>> finalizeSession(
            @PathVariable UUID sessionId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Attendance session finalized",
                attendanceService.finalizeSession(sessionId, getCallerUser(auth))));
    }

    @PostMapping("/records/{recordId}/correct")
    @PreAuthorize("hasAuthority('ATTENDANCE_CORRECT')")
    @Operation(summary = "Formally adjust attendance record with mandatory audit justification")
    public ResponseEntity<ApiResponse<AttendanceRecordDto>> correctRecord(
            @PathVariable UUID recordId,
            @Valid @RequestBody AttendanceCorrectionRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Attendance record corrected and audited",
                attendanceService.correctRecord(recordId, req, getCallerUser(auth))));
    }

    @GetMapping("/records/{recordId}/history")
    @PreAuthorize("hasAnyAuthority('ATTENDANCE_CORRECT', 'ATTENDANCE_REPORT_VIEW')")
    @Operation(summary = "View immutable audit correction trail for a record")
    public ResponseEntity<ApiResponse<List<AttendanceCorrectionHistoryDto>>> getCorrectionHistory(
            @PathVariable UUID recordId
    ) {
        return ResponseEntity.ok(ApiResponse.success("Correction audit trail retrieved",
                attendanceService.getRecordCorrectionHistory(recordId)));
    }

    @GetMapping("/students/{studentId}/summary")
    @PreAuthorize("hasAuthority('ATTENDANCE_REPORT_VIEW')")
    @Operation(summary = "Get aggregate and subject-wise attendance summary for a student")
    public ResponseEntity<ApiResponse<StudentAttendanceSummaryDto>> getStudentSummary(
            @PathVariable UUID studentId
    ) {
        return ResponseEntity.ok(ApiResponse.success("Student attendance summary retrieved",
                attendanceService.getStudentSummary(studentId)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current student's personal attendance summary")
    public ResponseEntity<ApiResponse<StudentAttendanceSummaryDto>> getMySummary(Authentication auth) {
        User user = getCallerUser(auth);
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> ApiException.notFound("Student record not linked to user: " + user.getEmail()));
        return ResponseEntity.ok(ApiResponse.success("My attendance summary retrieved",
                attendanceService.getStudentSummary(student.getId())));
    }

    @GetMapping("/reports/shortage")
    @PreAuthorize("hasAuthority('ATTENDANCE_REPORT_VIEW')")
    @Operation(summary = "Report students below attendance threshold (e.g. 75%) with recovery classes count")
    public ResponseEntity<ApiResponse<List<AttendanceShortageReportDto>>> getShortageReport(
            @RequestParam(required = false) Integer batchId,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) BigDecimal thresholdPercentage
    ) {
        return ResponseEntity.ok(ApiResponse.success("Attendance shortage report generated",
                attendanceService.getShortageReport(batchId, semester, section, subjectId, thresholdPercentage)));
    }
}
