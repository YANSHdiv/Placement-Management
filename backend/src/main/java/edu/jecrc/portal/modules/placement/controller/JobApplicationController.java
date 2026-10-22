package edu.jecrc.portal.modules.placement.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.modules.placement.dto.JobApplicationCreateRequest;
import edu.jecrc.portal.modules.placement.dto.JobApplicationDto;
import edu.jecrc.portal.modules.placement.dto.JobApplicationStatusUpdateRequest;
import edu.jecrc.portal.modules.placement.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Placement - Job Applications")
public class JobApplicationController {

    private final JobApplicationService applicationService;

    @PostMapping("/job-profiles/{jobProfileId}/applications")
    @PreAuthorize("hasAuthority('APPLICATION_SUBMIT')")
    @Operation(summary = "Submit a placement job profile application (with real-time eligibility re-evaluation)")
    public ResponseEntity<ApiResponse<JobApplicationDto>> apply(
            @PathVariable UUID jobProfileId,
            @Valid @RequestBody JobApplicationCreateRequest request,
            Authentication auth
    ) {
        JobApplicationDto dto = applicationService.apply(jobProfileId, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Application submitted successfully", dto));
    }

    @GetMapping("/applications/me")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Get applications submitted by logged-in student")
    public ResponseEntity<ApiResponse<PageResponse<JobApplicationDto>>> getMyApplications(
            Authentication auth,
            @PageableDefault(sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<JobApplicationDto> response = applicationService.getMyApplications(auth.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/applications/{applicationId}")
    @PreAuthorize("hasAnyAuthority('APPLICATION_MANAGE', 'ROLE_STUDENT', 'TPO_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get application details by ID")
    public ResponseEntity<ApiResponse<JobApplicationDto>> getApplication(
            @PathVariable UUID applicationId,
            Authentication auth
    ) {
        boolean isStaff = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                a.getAuthority().equals("ROLE_TPO_ADMIN") ||
                a.getAuthority().equals("ROLE_TPO_COORDINATOR")
        );
        JobApplicationDto dto = applicationService.getApplicationById(applicationId, auth.getName(), isStaff);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/applications/{applicationId}/withdraw")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Withdraw a submitted application")
    public ResponseEntity<ApiResponse<JobApplicationDto>> withdraw(
            @PathVariable UUID applicationId,
            Authentication auth
    ) {
        JobApplicationDto dto = applicationService.withdraw(applicationId, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn successfully", dto));
    }

    @GetMapping("/job-profiles/{jobProfileId}/applications")
    @PreAuthorize("hasAnyAuthority('APPLICATION_MANAGE', 'TPO_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "List and search student applications for a job profile")
    public ResponseEntity<ApiResponse<PageResponse<JobApplicationDto>>> getApplicationsForProfile(
            @PathVariable UUID jobProfileId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query,
            @PageableDefault(sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<JobApplicationDto> response = applicationService.searchApplications(jobProfileId, status, query, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/applications/{applicationId}/status")
    @PreAuthorize("hasAnyAuthority('APPLICATION_MANAGE', 'TPO_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update student job application review status")
    public ResponseEntity<ApiResponse<JobApplicationDto>> updateApplicationStatus(
            @PathVariable UUID applicationId,
            @Valid @RequestBody JobApplicationStatusUpdateRequest request,
            Authentication auth
    ) {
        JobApplicationDto dto = applicationService.updateStatus(applicationId, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Application status updated to " + dto.getApplicationStatus(), dto));
    }
}
