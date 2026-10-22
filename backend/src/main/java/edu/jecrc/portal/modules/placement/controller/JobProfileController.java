package edu.jecrc.portal.modules.placement.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.modules.placement.dto.JobProfileDto;
import edu.jecrc.portal.modules.placement.service.PlacementDriveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/job-profiles")
@RequiredArgsConstructor
@Tag(name = "Placement - Job Profiles")
public class JobProfileController {

    private final PlacementDriveService driveService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('DRIVE_VIEW', 'DRIVE_MANAGE')")
    @Operation(summary = "Search and list job profiles")
    public ResponseEntity<ApiResponse<PageResponse<JobProfileDto>>> searchJobProfiles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID driveId,
            @RequestParam(required = false) String employmentType,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<JobProfileDto> response = driveService.searchJobProfiles(query, driveId, employmentType, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{jobProfileId}")
    @PreAuthorize("hasAnyAuthority('DRIVE_VIEW', 'DRIVE_MANAGE')")
    @Operation(summary = "Get job profile by ID")
    public ResponseEntity<ApiResponse<JobProfileDto>> getJobProfile(@PathVariable UUID jobProfileId) {
        JobProfileDto dto = driveService.getJobProfile(jobProfileId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
