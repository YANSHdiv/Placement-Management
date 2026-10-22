package edu.jecrc.portal.modules.placement.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.BadRequestException;
import edu.jecrc.portal.common.exception.ForbiddenException;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.placement.dto.EligibilityEvaluationDto;
import edu.jecrc.portal.modules.placement.dto.EligibilityOverrideRequest;
import edu.jecrc.portal.modules.placement.entity.EligibilityEvaluation;
import edu.jecrc.portal.modules.placement.service.EligibilityEngineService;
import edu.jecrc.portal.modules.placement.service.EligibilityOverrideService;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Placement - Eligibility Policy Engine & Overrides")
public class EligibilityEvaluationController {

    private final EligibilityEngineService eligibilityEngineService;
    private final EligibilityOverrideService overrideService;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @PostMapping("/job-profiles/{jobProfileId}/eligibility-evaluations")
    @PreAuthorize("hasAnyAuthority('ELIGIBILITY_EVALUATE', 'TPO_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Execute real-time eligibility evaluation for a student against a job profile")
    public ResponseEntity<ApiResponse<EligibilityEvaluationDto>> evaluateEligibility(
            @PathVariable UUID jobProfileId,
            @RequestParam(required = false) UUID studentId,
            Authentication auth
    ) {
        User user = userRepository.findByEmailIgnoreCase(auth.getName()).orElse(null);

        UUID targetStudentId = studentId;
        if (targetStudentId == null) {
            // If studentId not specified, lookup the authenticated student's record
            Student self = studentRepository.findByUniversityEmailIgnoreCase(auth.getName())
                    .or(() -> user != null ? studentRepository.findByUserId(user.getId()) : java.util.Optional.empty())
                    .orElseThrow(() -> new BadRequestException("Must specify studentId when evaluating as administrative personnel"));
            targetStudentId = self.getId();
        } else {
            // If evaluating for a specified student, ensure caller has TPO/Admin privilege
            boolean isStaff = auth.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                    a.getAuthority().equals("ROLE_TPO_ADMIN") ||
                    a.getAuthority().equals("ROLE_TPO_COORDINATOR")
            );
            if (!isStaff) {
                // Student trying to evaluate someone else
                Student self = studentRepository.findByUniversityEmailIgnoreCase(auth.getName()).orElse(null);
                if (self == null || !self.getId().equals(targetStudentId)) {
                    throw new ForbiddenException("Students may only evaluate eligibility for their own profile");
                }
            }
        }

        EligibilityEvaluationDto evalDto = eligibilityEngineService.evaluateAndGetDto(targetStudentId, jobProfileId, user);
        return ResponseEntity.ok(ApiResponse.success("Eligibility evaluated successfully", evalDto));
    }

    @GetMapping("/job-profiles/{jobProfileId}/eligibility-evaluations")
    @PreAuthorize("hasAnyAuthority('TPO_ADMIN', 'SUPER_ADMIN', 'TPO_COORDINATOR')")
    @Operation(summary = "List candidate eligibility evaluations for a job profile")
    public ResponseEntity<ApiResponse<PageResponse<EligibilityEvaluationDto>>> getEvaluationsForJobProfile(
            @PathVariable UUID jobProfileId,
            @RequestParam(required = false) String outcome,
            @PageableDefault(sort = "evaluatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<EligibilityEvaluationDto> response = eligibilityEngineService.getEvaluationsForJobProfile(jobProfileId, outcome, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/eligibility-evaluations/me")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Get historical eligibility evaluations for logged-in student")
    public ResponseEntity<ApiResponse<PageResponse<EligibilityEvaluationDto>>> getMyEvaluations(
            Authentication auth,
            @PageableDefault(sort = "evaluatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Student self = studentRepository.findByUniversityEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new BadRequestException("No student record found for " + auth.getName()));

        PageResponse<EligibilityEvaluationDto> response = eligibilityEngineService.getEvaluationsForStudent(self.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/eligibility-evaluations/{evaluationId}")
    @PreAuthorize("hasAnyAuthority('ELIGIBILITY_EVALUATE', 'TPO_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get an individual evaluation record by ID")
    public ResponseEntity<ApiResponse<EligibilityEvaluationDto>> getEvaluationById(
            @PathVariable UUID evaluationId,
            Authentication auth
    ) {
        EligibilityEvaluationDto dto = eligibilityEngineService.getEvaluationDto(evaluationId);

        boolean isStaff = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                a.getAuthority().equals("ROLE_TPO_ADMIN") ||
                a.getAuthority().equals("ROLE_TPO_COORDINATOR")
        );

        if (!isStaff && !dto.getStudentEmail().equalsIgnoreCase(auth.getName())) {
            throw new ForbiddenException("Access denied to another student's eligibility evaluation");
        }

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/eligibility-evaluations/{evaluationId}/override")
    @PreAuthorize("hasAuthority('ELIGIBILITY_OVERRIDE')")
    @Operation(summary = "TPO Administrator override for eligibility evaluation with mandatory justification")
    public ResponseEntity<ApiResponse<EligibilityEvaluationDto>> overrideEvaluation(
            @PathVariable UUID evaluationId,
            @Valid @RequestBody EligibilityOverrideRequest request,
            Authentication auth
    ) {
        EligibilityEvaluationDto dto = overrideService.overrideEvaluation(evaluationId, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Eligibility exception successfully recorded", dto));
    }
}
