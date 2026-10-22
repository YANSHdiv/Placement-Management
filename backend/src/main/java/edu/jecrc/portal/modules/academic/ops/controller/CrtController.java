package edu.jecrc.portal.modules.academic.ops.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.ops.dto.*;
import edu.jecrc.portal.modules.academic.ops.service.CrtService;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crt")
@RequiredArgsConstructor
@Tag(name = "Campus Recruitment Training (CRT)", description = "CRT program tracks, skill sessions, candidate enrollment, and scoring")
public class CrtController {

    private final CrtService crtService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    private User getCallerUser(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @PostMapping("/programs")
    @PreAuthorize("hasAuthority('CRT_MANAGE')")
    @Operation(summary = "Create a Campus Recruitment Training program track")
    public ResponseEntity<ApiResponse<CrtProgramDto>> createProgram(
            @Valid @RequestBody CrtProgramCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("CRT program created successfully",
                crtService.createProgram(req, getCallerUser(auth))));
    }

    @GetMapping("/programs")
    @Operation(summary = "List CRT programs")
    public ResponseEntity<ApiResponse<List<CrtProgramDto>>> listPrograms(
            @RequestParam(required = false) String academicYear
    ) {
        return ResponseEntity.ok(ApiResponse.success("CRT programs retrieved",
                crtService.listPrograms(academicYear)));
    }

    @PostMapping("/sessions")
    @PreAuthorize("hasAuthority('CRT_MANAGE')")
    @Operation(summary = "Schedule a CRT module session (aptitude, coding, soft skills)")
    public ResponseEntity<ApiResponse<CrtSessionDto>> createSession(
            @Valid @RequestBody CrtSessionCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("CRT session scheduled successfully",
                crtService.createSession(req, getCallerUser(auth))));
    }

    @GetMapping("/sessions")
    @Operation(summary = "List CRT sessions")
    public ResponseEntity<ApiResponse<List<CrtSessionDto>>> listSessions(
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(ApiResponse.success("CRT sessions retrieved",
                crtService.listSessions(programId, status)));
    }

    @PostMapping("/sessions/{sessionId}/register")
    @Operation(summary = "Register student for a CRT session")
    public ResponseEntity<ApiResponse<CrtEnrollmentDto>> register(
            @PathVariable UUID sessionId,
            @RequestParam(required = false) UUID studentId,
            Authentication auth
    ) {
        User user = getCallerUser(auth);
        UUID targetStudentId = studentId;
        if (targetStudentId == null) {
            Student student = studentRepository.findByUserId(user.getId())
                    .orElseThrow(() -> ApiException.notFound("Student record not linked to user: " + user.getEmail()));
            targetStudentId = student.getId();
        }
        return ResponseEntity.ok(ApiResponse.success("Enrolled in CRT session",
                crtService.registerStudent(sessionId, targetStudentId, user)));
    }

    @PostMapping("/sessions/{sessionId}/scores")
    @PreAuthorize("hasAuthority('CRT_ATTENDANCE_MARK')")
    @Operation(summary = "Submit attendance and assessment score for a CRT student")
    public ResponseEntity<ApiResponse<CrtEnrollmentDto>> submitScore(
            @PathVariable UUID sessionId,
            @Valid @RequestBody CrtAttendanceScoreSubmitRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("CRT attendance and score recorded",
                crtService.submitAttendanceAndScore(sessionId, req, getCallerUser(auth))));
    }

    @GetMapping("/sessions/{sessionId}/enrollments")
    @Operation(summary = "List registered students and scores for a CRT session")
    public ResponseEntity<ApiResponse<List<CrtEnrollmentDto>>> listEnrollments(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(ApiResponse.success("CRT enrollments retrieved",
                crtService.listEnrollments(sessionId)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current student's CRT enrollments and performance")
    public ResponseEntity<ApiResponse<List<CrtEnrollmentDto>>> getMyEnrollments(Authentication auth) {
        User user = getCallerUser(auth);
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> ApiException.notFound("Student record not linked to user: " + user.getEmail()));
        return ResponseEntity.ok(ApiResponse.success("My CRT records retrieved",
                crtService.getStudentEnrollments(student.getId())));
    }
}
