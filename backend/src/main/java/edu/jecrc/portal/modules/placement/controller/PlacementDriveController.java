package edu.jecrc.portal.modules.placement.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.modules.placement.dto.*;
import edu.jecrc.portal.modules.placement.service.PlacementDriveService;
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
@RequestMapping("/api/v1/drives")
@RequiredArgsConstructor
@Tag(name = "Placement - Recruitment Drives")
public class PlacementDriveController {

    private final PlacementDriveService driveService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('DRIVE_VIEW', 'DRIVE_MANAGE')")
    @Operation(summary = "Search and list recruitment drives")
    public ResponseEntity<ApiResponse<PageResponse<PlacementDriveDto>>> searchDrives(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String academicYear,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<PlacementDriveDto> response = driveService.searchDrives(query, status, academicYear, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DRIVE_MANAGE')")
    @Operation(summary = "Create a recruitment drive draft")
    public ResponseEntity<ApiResponse<PlacementDriveDto>> createDrive(
            @Valid @RequestBody PlacementDriveCreateRequest request,
            Authentication auth
    ) {
        PlacementDriveDto dto = driveService.createDrive(request, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Drive draft created successfully", dto));
    }

    @GetMapping("/{driveId}")
    @PreAuthorize("hasAnyAuthority('DRIVE_VIEW', 'DRIVE_MANAGE')")
    @Operation(summary = "Get recruitment drive details")
    public ResponseEntity<ApiResponse<PlacementDriveDto>> getDrive(@PathVariable UUID driveId) {
        PlacementDriveDto dto = driveService.getDrive(driveId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PutMapping("/{driveId}")
    @PreAuthorize("hasAuthority('DRIVE_MANAGE')")
    @Operation(summary = "Update recruitment drive details")
    public ResponseEntity<ApiResponse<PlacementDriveDto>> updateDrive(
            @PathVariable UUID driveId,
            @Valid @RequestBody PlacementDriveUpdateRequest request,
            Authentication auth
    ) {
        PlacementDriveDto dto = driveService.updateDrive(driveId, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Drive updated successfully", dto));
    }

    @PostMapping("/{driveId}/publish")
    @PreAuthorize("hasAuthority('DRIVE_MANAGE')")
    @Operation(summary = "Publish recruitment drive for student visibility")
    public ResponseEntity<ApiResponse<PlacementDriveDto>> publishDrive(
            @PathVariable UUID driveId,
            Authentication auth
    ) {
        PlacementDriveDto dto = driveService.publishDrive(driveId, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Drive published successfully", dto));
    }

    @PostMapping("/{driveId}/close")
    @PreAuthorize("hasAuthority('DRIVE_MANAGE')")
    @Operation(summary = "Close recruitment drive applications")
    public ResponseEntity<ApiResponse<PlacementDriveDto>> closeDrive(
            @PathVariable UUID driveId,
            Authentication auth
    ) {
        PlacementDriveDto dto = driveService.closeDrive(driveId, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Drive closed successfully", dto));
    }

    @PostMapping("/{driveId}/archive")
    @PreAuthorize("hasAuthority('DRIVE_MANAGE')")
    @Operation(summary = "Archive recruitment drive")
    public ResponseEntity<ApiResponse<PlacementDriveDto>> archiveDrive(
            @PathVariable UUID driveId,
            Authentication auth
    ) {
        PlacementDriveDto dto = driveService.archiveDrive(driveId, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Drive archived successfully", dto));
    }

    @PostMapping("/{driveId}/job-profiles")
    @PreAuthorize("hasAuthority('DRIVE_MANAGE')")
    @Operation(summary = "Create and attach a job profile to a drive")
    public ResponseEntity<ApiResponse<JobProfileDto>> createJobProfile(
            @PathVariable UUID driveId,
            @Valid @RequestBody JobProfileCreateRequest request,
            Authentication auth
    ) {
        JobProfileDto dto = driveService.createJobProfile(driveId, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Job profile added to drive successfully", dto));
    }
}
