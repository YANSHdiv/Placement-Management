package edu.jecrc.portal.modules.dashboard.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.modules.dashboard.dto.PlacementAnalyticsDto;
import edu.jecrc.portal.modules.dashboard.service.PlacementAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Placement Analytics", description = "Institution-wide and department-scoped placement analytics and compensation percentiles")
public class PlacementAnalyticsController {

    private final PlacementAnalyticsService analyticsService;

    @GetMapping("/placements")
    @PreAuthorize("hasAnyRole('TPO_ADMIN', 'DIRECTOR', 'SUPER_ADMIN', 'HOD') or hasAuthority('REPORT_INSTITUTION')")
    @Operation(summary = "Get detailed placement funnel analytics, compensation percentiles and tier distribution")
    public ResponseEntity<ApiResponse<PlacementAnalyticsDto>> getPlacementAnalytics(
            @Parameter(description = "Optional Department ID filter") @RequestParam(required = false) Integer departmentId,
            @Parameter(description = "Optional Batch ID filter") @RequestParam(required = false) UUID batchId,
            @Parameter(description = "Optional Academic Year (e.g. 2025-2026)") @RequestParam(required = false) String academicYear
    ) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getPlacementAnalytics(departmentId, batchId, academicYear)));
    }
}
