package edu.jecrc.portal.modules.placement.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.modules.placement.dto.PlacementPolicyCreateRequest;
import edu.jecrc.portal.modules.placement.dto.PlacementPolicyDto;
import edu.jecrc.portal.modules.placement.service.PlacementPolicyService;
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
@RequestMapping("/api/v1/placement-policies")
@RequiredArgsConstructor
@Tag(name = "Placement - Eligibility Policy Management")
public class PlacementPolicyController {

    private final PlacementPolicyService policyService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('POLICY_MANAGE', 'DRIVE_VIEW', 'TPO_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Search and list placement eligibility policies")
    public ResponseEntity<ApiResponse<PageResponse<PlacementPolicyDto>>> searchPolicies(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<PlacementPolicyDto> response = policyService.searchPolicies(query, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('POLICY_MANAGE')")
    @Operation(summary = "Create a new placement eligibility policy in DRAFT status")
    public ResponseEntity<ApiResponse<PlacementPolicyDto>> createPolicy(
            @Valid @RequestBody PlacementPolicyCreateRequest request,
            Authentication auth
    ) {
        PlacementPolicyDto dto = policyService.createPolicy(request, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Policy draft created successfully", dto));
    }

    @GetMapping("/{policyId}")
    @PreAuthorize("hasAnyAuthority('POLICY_MANAGE', 'DRIVE_VIEW', 'TPO_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get policy details by ID")
    public ResponseEntity<ApiResponse<PlacementPolicyDto>> getPolicy(@PathVariable UUID policyId) {
        PlacementPolicyDto dto = policyService.getPolicy(policyId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/{policyId}/publish")
    @PreAuthorize("hasAuthority('POLICY_MANAGE')")
    @Operation(summary = "Publish a policy making its criteria immutable")
    public ResponseEntity<ApiResponse<PlacementPolicyDto>> publishPolicy(
            @PathVariable UUID policyId,
            Authentication auth
    ) {
        PlacementPolicyDto dto = policyService.publishPolicy(policyId, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Policy published successfully", dto));
    }

    @PostMapping("/{policyId}/new-version")
    @PreAuthorize("hasAuthority('POLICY_MANAGE')")
    @Operation(summary = "Create an iterated new version from an existing policy")
    public ResponseEntity<ApiResponse<PlacementPolicyDto>> createNewVersion(
            @PathVariable UUID policyId,
            @Valid @RequestBody PlacementPolicyCreateRequest request,
            Authentication auth
    ) {
        PlacementPolicyDto dto = policyService.createNewVersion(policyId, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("New policy version created in draft status", dto));
    }
}
