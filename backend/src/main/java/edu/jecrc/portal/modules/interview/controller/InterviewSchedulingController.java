package edu.jecrc.portal.modules.interview.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.interview.dto.*;
import edu.jecrc.portal.modules.interview.service.InterviewSchedulingService;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Interview Scheduling", description = "Candidate interview appointments & conflict detection")
public class InterviewSchedulingController {

    private final InterviewSchedulingService schedulingService;
    private final UserRepository userRepository;

    private User getCallerUser(Authentication auth) {
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @PostMapping("/appointments")
    @PreAuthorize("hasAuthority('INTERVIEW_SCHEDULE_MANAGE')")
    @Operation(summary = "Schedule an interview appointment for a candidate")
    public ResponseEntity<ApiResponse<InterviewAppointmentDto>> scheduleAppointment(
            @Valid @RequestBody InterviewAppointmentCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Appointment scheduled", schedulingService.scheduleAppointment(req, getCallerUser(auth))));
    }

    @PutMapping("/appointments/{appointmentId}")
    @PreAuthorize("hasAuthority('INTERVIEW_SCHEDULE_MANAGE')")
    @Operation(summary = "Update or reschedule an interview appointment")
    public ResponseEntity<ApiResponse<InterviewAppointmentDto>> updateAppointment(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody InterviewAppointmentUpdateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Appointment updated", schedulingService.updateAppointment(appointmentId, req, getCallerUser(auth))));
    }

    @GetMapping("/appointments/conflicts")
    @PreAuthorize("hasAuthority('INTERVIEW_SCHEDULE_MANAGE')")
    @Operation(summary = "Check for student or evaluator appointment conflicts")
    public ResponseEntity<ApiResponse<ConflictCheckResponse>> checkConflicts(
            @RequestParam UUID studentId,
            @RequestParam(required = false) UUID evaluatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            @RequestParam(required = false) UUID excludeAppointmentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(schedulingService.checkConflicts(studentId, evaluatorId, startTime, endTime, excludeAppointmentId)));
    }

    @GetMapping("/appointments/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get upcoming interview appointments for the logged-in student")
    public ResponseEntity<ApiResponse<List<InterviewAppointmentDto>>> getMyAppointments(
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success(schedulingService.getMyAppointments(auth.getName())));
    }

    @GetMapping("/round-candidates/{candidateId}/appointments")
    @PreAuthorize("hasAnyAuthority('INTERVIEW_SCHEDULE_MANAGE', 'ROUND_CANDIDATE_VIEW')")
    @Operation(summary = "Get appointments for a round candidate")
    public ResponseEntity<ApiResponse<List<InterviewAppointmentDto>>> getAppointmentsForCandidate(
            @PathVariable UUID candidateId
    ) {
        return ResponseEntity.ok(ApiResponse.success(schedulingService.getAppointmentsForCandidate(candidateId)));
    }
}
