package edu.jecrc.portal.modules.academic.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.modules.academic.dto.StudentCreateRequest;
import edu.jecrc.portal.modules.academic.dto.StudentDetailDto;
import edu.jecrc.portal.modules.academic.dto.StudentSummaryDto;
import edu.jecrc.portal.modules.academic.dto.StudentUpdateRequest;
import edu.jecrc.portal.modules.academic.service.StudentImportExportService;
import edu.jecrc.portal.modules.academic.service.StudentService;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import edu.jecrc.portal.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student academic profiles, registry search, and exports")
public class StudentController {

    private final StudentService studentService;
    private final StudentImportExportService importExportService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('STUDENT_PROFILE_READ', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD', 'ROLE_FACULTY', 'ROLE_STUDENT')")
    @Operation(summary = "Search and list students", description = "Scoped by caller role and assigned department permissions")
    public ResponseEntity<ApiResponse<PageResponse<StudentSummaryDto>>> getStudents(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer programId,
            @RequestParam(required = false) Integer batchId,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String verificationStatus,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "rollNumber", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        PageResponse<StudentSummaryDto> response = studentService.getStudents(
                departmentId, programId, batchId, semester, verificationStatus, search, caller, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('STUDENT_PROFILE_MANAGE', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD')")
    @Operation(summary = "Create student record", description = "Create new student master profile with departmental scoping checks")
    public ResponseEntity<ApiResponse<StudentDetailDto>> createStudent(
            @Valid @RequestBody StudentCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        StudentDetailDto dto = studentService.createStudent(request, caller);
        return ResponseEntity.ok(ApiResponse.success("Student created successfully", dto));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current authenticated student's academic profile")
    public ResponseEntity<ApiResponse<StudentDetailDto>> getMe(
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        StudentDetailDto dto = studentService.getCurrentStudentProfile(caller);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/{studentId}")
    @PreAuthorize("hasAnyAuthority('STUDENT_PROFILE_READ', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD', 'ROLE_FACULTY', 'ROLE_STUDENT')")
    @Operation(summary = "Get student detail by ID", description = "Protected by student self-ownership or staff departmental scope")
    public ResponseEntity<ApiResponse<StudentDetailDto>> getStudentById(
            @PathVariable UUID studentId,
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        StudentDetailDto dto = studentService.getStudentById(studentId, caller);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PutMapping("/{studentId}")
    @PreAuthorize("hasAnyAuthority('STUDENT_PROFILE_READ', 'STUDENT_PROFILE_MANAGE', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD', 'ROLE_STUDENT')")
    @Operation(summary = "Update student profile", description = "Students can edit personal info; staff can update academic fields")
    public ResponseEntity<ApiResponse<StudentDetailDto>> updateStudent(
            @PathVariable UUID studentId,
            @Valid @RequestBody StudentUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        StudentDetailDto dto = studentService.updateStudent(studentId, request, caller);
        return ResponseEntity.ok(ApiResponse.success("Student updated successfully", dto));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('STUDENT_EXPORT', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD')")
    @Operation(summary = "Export student records to Excel", description = "Generates Excel (.xlsx) file with departmental scoping applied")
    public ResponseEntity<byte[]> exportStudents(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer programId,
            @RequestParam(required = false) Integer batchId,
            @AuthenticationPrincipal UserPrincipal principal) throws IOException {

        User caller = getCurrentUser(principal);
        byte[] excelBytes = importExportService.exportStudentsToExcel(departmentId, programId, batchId, caller);

        String filename = "jecrc_students_" + System.currentTimeMillis() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    private User getCurrentUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}
