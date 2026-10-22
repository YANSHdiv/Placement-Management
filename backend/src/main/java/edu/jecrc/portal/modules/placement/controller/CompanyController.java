package edu.jecrc.portal.modules.placement.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.modules.placement.dto.CompanyCreateRequest;
import edu.jecrc.portal.modules.placement.dto.CompanyDto;
import edu.jecrc.portal.modules.placement.dto.CompanyUpdateRequest;
import edu.jecrc.portal.modules.placement.service.CompanyService;
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
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Placement - Company Management")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('COMPANY_VIEW', 'COMPANY_MANAGE')")
    @Operation(summary = "Search and list companies")
    public ResponseEntity<ApiResponse<PageResponse<CompanyDto>>> searchCompanies(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "companyName", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication auth
    ) {
        boolean isStudent = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        PageResponse<CompanyDto> response = companyService.searchCompanies(query, status, pageable, !isStudent);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COMPANY_MANAGE')")
    @Operation(summary = "Create a new recruitment company partner")
    public ResponseEntity<ApiResponse<CompanyDto>> createCompany(
            @Valid @RequestBody CompanyCreateRequest request,
            Authentication auth
    ) {
        CompanyDto dto = companyService.createCompany(request, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Company created successfully", dto));
    }

    @GetMapping("/{companyId}")
    @PreAuthorize("hasAnyAuthority('COMPANY_VIEW', 'COMPANY_MANAGE')")
    @Operation(summary = "Get company details by ID")
    public ResponseEntity<ApiResponse<CompanyDto>> getCompany(
            @PathVariable UUID companyId,
            Authentication auth
    ) {
        boolean isStudent = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        CompanyDto dto = companyService.getCompany(companyId, !isStudent);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PutMapping("/{companyId}")
    @PreAuthorize("hasAuthority('COMPANY_MANAGE')")
    @Operation(summary = "Update company details")
    public ResponseEntity<ApiResponse<CompanyDto>> updateCompany(
            @PathVariable UUID companyId,
            @Valid @RequestBody CompanyUpdateRequest request,
            Authentication auth
    ) {
        CompanyDto dto = companyService.updateCompany(companyId, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Company updated successfully", dto));
    }

    @PostMapping("/{companyId}/archive")
    @PreAuthorize("hasAuthority('COMPANY_MANAGE')")
    @Operation(summary = "Archive a company")
    public ResponseEntity<ApiResponse<CompanyDto>> archiveCompany(
            @PathVariable UUID companyId,
            Authentication auth
    ) {
        CompanyDto dto = companyService.archiveCompany(companyId, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Company archived successfully", dto));
    }
}
