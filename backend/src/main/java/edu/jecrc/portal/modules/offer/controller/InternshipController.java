package edu.jecrc.portal.modules.offer.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.offer.dto.*;
import edu.jecrc.portal.modules.offer.service.InternshipService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Internships", description = "Student internships, mentor assignments, progress logs, and evaluations")
public class InternshipController {

    private final InternshipService internshipService;
    private final UserRepository userRepository;

    private User getCallerUser(Authentication auth) {
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @PostMapping("/internships")
    @PreAuthorize("hasAuthority('INTERNSHIP_MANAGE')")
    @Operation(summary = "Register a new student internship record")
    public ResponseEntity<ApiResponse<InternshipRecordDto>> createInternship(
            @Valid @RequestBody InternshipCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Internship registered successfully", internshipService.createInternship(req, getCallerUser(auth))));
    }

    @GetMapping("/internships")
    @PreAuthorize("hasAuthority('INTERNSHIP_MANAGE')")
    @Operation(summary = "Search internships with student, company, status, and mentor filters")
    public ResponseEntity<ApiResponse<PageResponse<InternshipRecordDto>>> searchInternships(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID facultyMentorId,
            @PageableDefault(size = 30) Pageable pageable
    ) {
        Page<InternshipRecordDto> page = internshipService.searchInternships(studentId, companyId, status, facultyMentorId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/internships/{internshipId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get internship record details")
    public ResponseEntity<ApiResponse<InternshipRecordDto>> getInternshipById(
            @PathVariable UUID internshipId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success(internshipService.getInternshipById(internshipId, getCallerUser(auth))));
    }

    @PostMapping("/internships/{internshipId}/mentors")
    @PreAuthorize("hasAuthority('INTERNSHIP_MANAGE')")
    @Operation(summary = "Assign faculty or industry mentors to an intern")
    public ResponseEntity<ApiResponse<InternshipRecordDto>> assignMentor(
            @PathVariable UUID internshipId,
            @Valid @RequestBody InternshipMentorAssignRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Mentor assigned successfully", internshipService.assignMentor(internshipId, req, getCallerUser(auth))));
    }

    @PostMapping("/internships/{internshipId}/logs")
    @PreAuthorize("hasAuthority('INTERNSHIP_LOG_SUBMIT')")
    @Operation(summary = "Student submits periodic internship progress log")
    public ResponseEntity<ApiResponse<InternshipProgressLogDto>> submitProgressLog(
            @PathVariable UUID internshipId,
            @Valid @RequestBody InternshipProgressLogCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Progress log submitted successfully", internshipService.submitProgressLog(internshipId, req, getCallerUser(auth))));
    }

    @GetMapping("/internships/{internshipId}/logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "View progress logs submitted for an internship")
    public ResponseEntity<ApiResponse<List<InternshipProgressLogDto>>> getProgressLogs(
            @PathVariable UUID internshipId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success(internshipService.getProgressLogs(internshipId, getCallerUser(auth))));
    }

    @PutMapping("/internships/{internshipId}/logs/{logId}/review")
    @PreAuthorize("hasAuthority('INTERNSHIP_LOG_REVIEW')")
    @Operation(summary = "Faculty mentor reviews progress log and provides feedback")
    public ResponseEntity<ApiResponse<InternshipProgressLogDto>> reviewProgressLog(
            @PathVariable UUID internshipId,
            @PathVariable UUID logId,
            @Valid @RequestBody InternshipProgressLogReviewRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Progress log reviewed", internshipService.reviewProgressLog(internshipId, logId, req, getCallerUser(auth))));
    }

    @PostMapping("/internships/{internshipId}/evaluations")
    @PreAuthorize("hasAuthority('INTERNSHIP_EVALUATE')")
    @Operation(summary = "Submit midterm or final evaluation for intern")
    public ResponseEntity<ApiResponse<InternshipEvaluationDto>> submitEvaluation(
            @PathVariable UUID internshipId,
            @Valid @RequestBody InternshipEvaluationCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Evaluation recorded successfully", internshipService.submitEvaluation(internshipId, req, getCallerUser(auth))));
    }

    @GetMapping("/internships/{internshipId}/evaluations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get evaluations for an internship (confidential notes masked for student)")
    public ResponseEntity<ApiResponse<List<InternshipEvaluationDto>>> getEvaluations(
            @PathVariable UUID internshipId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success(internshipService.getEvaluations(internshipId, getCallerUser(auth))));
    }

    @PostMapping("/internships/{internshipId}/complete")
    @PreAuthorize("hasAuthority('INTERNSHIP_MANAGE')")
    @Operation(summary = "Record completion or termination of an internship")
    public ResponseEntity<ApiResponse<InternshipRecordDto>> completeInternship(
            @PathVariable UUID internshipId,
            @Valid @RequestBody InternshipCompletionRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Internship completion status updated", internshipService.completeInternship(internshipId, req, getCallerUser(auth))));
    }

    @GetMapping("/internships/me")
    @PreAuthorize("hasAuthority('INTERNSHIP_LOG_SUBMIT')")
    @Operation(summary = "Get internships for the authenticated student")
    public ResponseEntity<ApiResponse<List<InternshipRecordDto>>> getMyInternships(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(internshipService.getMyInternships(auth.getName())));
    }

    @GetMapping("/internships/assigned-mentees")
    @PreAuthorize("hasAuthority('INTERNSHIP_LOG_REVIEW')")
    @Operation(summary = "Get interns assigned to the authenticated faculty mentor")
    public ResponseEntity<ApiResponse<List<InternshipRecordDto>>> getAssignedInternships(Authentication auth) {
        User caller = getCallerUser(auth);
        return ResponseEntity.ok(ApiResponse.success(internshipService.getAssignedInternshipsForFaculty(caller.getId())));
    }
}
