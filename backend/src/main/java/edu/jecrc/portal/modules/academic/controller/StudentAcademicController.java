package edu.jecrc.portal.modules.academic.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.modules.academic.dto.*;
import edu.jecrc.portal.modules.academic.service.StudentAcademicService;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import edu.jecrc.portal.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Student Academics", description = "Qualifications, semester results, backlogs, and academic verification")
public class StudentAcademicController {

    private final StudentAcademicService academicService;
    private final UserRepository userRepository;

    @GetMapping("/{studentId}/qualifications")
    @PreAuthorize("hasAnyAuthority('STUDENT_PROFILE_READ', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD', 'ROLE_FACULTY', 'ROLE_STUDENT')")
    @Operation(summary = "Get student qualifications (10th, 12th, Diploma, etc.)")
    public ResponseEntity<ApiResponse<List<StudentQualificationDto>>> getQualifications(
            @PathVariable UUID studentId,
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        List<StudentQualificationDto> list = academicService.getQualifications(studentId, caller);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/{studentId}/qualifications")
    @PreAuthorize("hasAnyAuthority('STUDENT_PROFILE_READ', 'STUDENT_PROFILE_MANAGE', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD', 'ROLE_STUDENT')")
    @Operation(summary = "Upsert student academic qualifications")
    public ResponseEntity<ApiResponse<List<StudentQualificationDto>>> upsertQualifications(
            @PathVariable UUID studentId,
            @Valid @RequestBody List<StudentQualificationDto> dtos,
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        List<StudentQualificationDto> saved = academicService.upsertQualifications(studentId, dtos, caller);
        return ResponseEntity.ok(ApiResponse.success("Qualifications updated successfully", saved));
    }

    @GetMapping("/{studentId}/semester-results")
    @PreAuthorize("hasAnyAuthority('STUDENT_PROFILE_READ', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD', 'ROLE_FACULTY', 'ROLE_STUDENT')")
    @Operation(summary = "Get student semester results and marksheets")
    public ResponseEntity<ApiResponse<List<SemesterResultDto>>> getSemesterResults(
            @PathVariable UUID studentId,
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        List<SemesterResultDto> list = academicService.getSemesterResults(studentId, caller);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/{studentId}/semester-results")
    @PreAuthorize("hasAnyAuthority('STUDENT_PROFILE_MANAGE', 'RESULT_BULK_IMPORT', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD')")
    @Operation(summary = "Add or update semester result with subject attempts")
    public ResponseEntity<ApiResponse<SemesterResultDto>> addSemesterResult(
            @PathVariable UUID studentId,
            @Valid @RequestBody SemesterResultDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        SemesterResultDto saved = academicService.addOrUpdateSemesterResult(studentId, dto, caller);
        return ResponseEntity.ok(ApiResponse.success("Semester result recorded successfully", saved));
    }

    @GetMapping("/{studentId}/backlogs")
    @PreAuthorize("hasAnyAuthority('STUDENT_PROFILE_READ', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD', 'ROLE_FACULTY', 'ROLE_STUDENT')")
    @Operation(summary = "Get student backlog attempts history (active and cleared)")
    public ResponseEntity<ApiResponse<List<SubjectAttemptDto>>> getBacklogs(
            @PathVariable UUID studentId,
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        List<SubjectAttemptDto> list = academicService.getBacklogs(studentId, caller);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/{studentId}/verify")
    @PreAuthorize("hasAnyAuthority('STUDENT_RECORD_VERIFY', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD')")
    @Operation(summary = "Verify or reject student academic records", description = "Only authorized staff/HOD for their scoped department")
    public ResponseEntity<ApiResponse<StudentDetailDto>> verifyStudentRecord(
            @PathVariable UUID studentId,
            @Valid @RequestBody StudentVerifyRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        StudentDetailDto dto = academicService.verifyStudentRecord(studentId, request, caller);
        return ResponseEntity.ok(ApiResponse.success("Student verification status updated to " + dto.getVerificationStatus(), dto));
    }

    private User getCurrentUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}
