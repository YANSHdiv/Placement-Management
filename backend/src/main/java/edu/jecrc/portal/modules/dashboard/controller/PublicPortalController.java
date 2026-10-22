package edu.jecrc.portal.modules.dashboard.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.modules.dashboard.dto.PublicOverviewDto;
import edu.jecrc.portal.modules.dashboard.service.PublicPortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Public Portal", description = "Publicly accessible university overview, placement highlights and campus contacts")
public class PublicPortalController {

    private final PublicPortalService publicPortalService;

    @GetMapping("/overview")
    @Operation(summary = "Get public university overview, verified placement statistics, and official contact directory")
    public ResponseEntity<ApiResponse<PublicOverviewDto>> getPublicOverview() {
        return ResponseEntity.ok(ApiResponse.success(publicPortalService.getPublicOverview()));
    }
}
