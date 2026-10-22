package edu.jecrc.portal.modules.academic.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.modules.academic.dto.AcademicBatchDto;
import edu.jecrc.portal.modules.academic.dto.AcademicProgramDto;
import edu.jecrc.portal.modules.academic.dto.DepartmentDto;
import edu.jecrc.portal.modules.academic.service.AcademicMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Academic Master Data", description = "Departments, academic programs, and graduation cohorts")
public class AcademicMasterController {

    private final AcademicMasterService masterService;

    @GetMapping("/departments")
    @Operation(summary = "List all academic departments")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getDepartments() {
        return ResponseEntity.ok(ApiResponse.success(masterService.getAllDepartments()));
    }

    @GetMapping("/programs")
    @Operation(summary = "List academic programs", description = "Filter by departmentId optionally")
    public ResponseEntity<ApiResponse<List<AcademicProgramDto>>> getPrograms(
            @RequestParam(required = false) Integer departmentId) {
        return ResponseEntity.ok(ApiResponse.success(masterService.getAllPrograms(departmentId)));
    }

    @GetMapping("/academic-batches")
    @Operation(summary = "List academic cohorts/batches", description = "Filter by programId optionally")
    public ResponseEntity<ApiResponse<List<AcademicBatchDto>>> getBatches(
            @RequestParam(required = false) Integer programId) {
        return ResponseEntity.ok(ApiResponse.success(masterService.getAllBatches(programId)));
    }
}
