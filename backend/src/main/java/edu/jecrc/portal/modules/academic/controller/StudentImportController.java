package edu.jecrc.portal.modules.academic.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.modules.academic.dto.ImportCommitRequest;
import edu.jecrc.portal.modules.academic.dto.ImportCommitResponse;
import edu.jecrc.portal.modules.academic.dto.ImportPreviewResponse;
import edu.jecrc.portal.modules.academic.entity.StudentImportRow;
import edu.jecrc.portal.modules.academic.service.StudentImportExportService;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import edu.jecrc.portal.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students/import")
@RequiredArgsConstructor
@Tag(name = "Student Import & Templates", description = "Bulk CSV/Excel student ingestion with two-phase preview and validation")
public class StudentImportController {

    private final StudentImportExportService importService;
    private final UserRepository userRepository;

    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('RESULT_BULK_IMPORT', 'STUDENT_PROFILE_MANAGE', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD')")
    @Operation(summary = "Preview and validate student bulk import file", description = "Validates file without making persistent modifications to student tables")
    public ResponseEntity<ApiResponse<ImportPreviewResponse>> previewImport(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        ImportPreviewResponse preview = importService.previewImport(file, caller);
        return ResponseEntity.ok(ApiResponse.success(preview));
    }

    @PostMapping("/commit")
    @PreAuthorize("hasAnyAuthority('RESULT_BULK_IMPORT', 'STUDENT_PROFILE_MANAGE', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD')")
    @Operation(summary = "Commit validated student bulk import", description = "Transactionally persists valid rows; protects already verified records from overwrite")
    public ResponseEntity<ApiResponse<ImportCommitResponse>> commitImport(
            @Valid @RequestBody ImportCommitRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        ImportCommitResponse response = importService.commitImport(request, caller);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{importId}/errors")
    @PreAuthorize("hasAnyAuthority('RESULT_BULK_IMPORT', 'STUDENT_PROFILE_MANAGE', 'ROLE_SUPER_ADMIN', 'ROLE_TPO_ADMIN', 'ROLE_HOD')")
    @Operation(summary = "Get row-level errors for an import batch")
    public ResponseEntity<ApiResponse<List<StudentImportRow>>> getImportErrors(
            @PathVariable UUID importId,
            @AuthenticationPrincipal UserPrincipal principal) {

        User caller = getCurrentUser(principal);
        List<StudentImportRow> errors = importService.getImportErrors(importId, caller);
        return ResponseEntity.ok(ApiResponse.success(errors));
    }

    @GetMapping("/template")
    @Operation(summary = "Download standard CSV template with sample data")
    public ResponseEntity<byte[]> downloadTemplate() {
        String templateCsv = "university_student_id,roll_number,registration_number,first_name,last_name,university_email,personal_email,phone_number,program_code,graduation_year,current_semester,section,class_10_percentage,class_12_percentage\n" +
                "22JU0201,22BCSE201,JU/2022/CSE/201,Rahul,Verma,rahul.v@jecrcuniversity.edu.in,rahul.personal@gmail.com,+91-9876543220,BTECH_CSE,2026,6,A,88.50,85.20\n" +
                "22JU0202,22BCSE202,JU/2022/CSE/202,Priya,Sharma,priya.s@jecrcuniversity.edu.in,priya.personal@gmail.com,+91-9876543221,BTECH_CSE,2026,6,A,92.00,90.40\n" +
                "22JU0203,22BECE203,JU/2022/ECE/203,Ankit,Gupta,ankit.g@jecrcuniversity.edu.in,ankit.personal@gmail.com,+91-9876543222,BTECH_ECE,2026,6,B,81.00,79.50\n";

        byte[] bytes = templateCsv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=jecrc_student_import_template.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    private User getCurrentUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}
