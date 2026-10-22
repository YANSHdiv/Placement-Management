package edu.jecrc.portal.modules.interview.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.interview.dto.RoundEvaluationDto;
import edu.jecrc.portal.modules.interview.dto.RoundEvaluationSubmitRequest;
import edu.jecrc.portal.modules.interview.service.EvaluationService;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/round-candidates/{candidateId}/evaluations")
@RequiredArgsConstructor
@Tag(name = "Evaluations", description = "Evaluator candidate scoring, recommendations & qualitative notes")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final UserRepository userRepository;

    private User getCallerUser(Authentication auth) {
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROUND_EVALUATION_SUBMIT')")
    @Operation(summary = "Submit or draft an evaluation for a candidate")
    public ResponseEntity<ApiResponse<RoundEvaluationDto>> submitEvaluation(
            @PathVariable UUID candidateId,
            @Valid @RequestBody RoundEvaluationSubmitRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Evaluation recorded", evaluationService.submitEvaluation(candidateId, req, getCallerUser(auth))));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROUND_EVALUATION_SUBMIT', 'ROUND_CANDIDATE_VIEW')")
    @Operation(summary = "Get evaluations for a candidate (confidential notes restricted)")
    public ResponseEntity<ApiResponse<List<RoundEvaluationDto>>> getEvaluations(
            @PathVariable UUID candidateId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success(evaluationService.getEvaluationsForCandidate(candidateId, getCallerUser(auth))));
    }
}
