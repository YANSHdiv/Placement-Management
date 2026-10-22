package edu.jecrc.portal.modules.interview.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.interview.dto.*;
import edu.jecrc.portal.modules.interview.service.InterviewRoundService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Interview Rounds", description = "Recruitment interview round configuration & evaluator management")
public class InterviewRoundController {

    private final InterviewRoundService roundService;
    private final UserRepository userRepository;

    private User getCallerUser(Authentication auth) {
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @GetMapping("/job-profiles/{jobProfileId}/rounds")
    @PreAuthorize("hasAnyAuthority('DRIVE_VIEW', 'INTERVIEW_ROUND_MANAGE')")
    @Operation(summary = "List interview rounds for a job profile")
    public ResponseEntity<ApiResponse<List<InterviewRoundDto>>> getRoundsForJobProfile(@PathVariable UUID jobProfileId) {
        return ResponseEntity.ok(ApiResponse.success(roundService.getRoundsForJobProfile(jobProfileId)));
    }

    @PostMapping("/job-profiles/{jobProfileId}/rounds")
    @PreAuthorize("hasAuthority('INTERVIEW_ROUND_MANAGE')")
    @Operation(summary = "Create an interview round for a job profile")
    public ResponseEntity<ApiResponse<InterviewRoundDto>> createRound(
            @PathVariable UUID jobProfileId,
            @Valid @RequestBody InterviewRoundCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Interview round created successfully", roundService.createRound(jobProfileId, req, getCallerUser(auth))));
    }

    @GetMapping("/rounds/{roundId}")
    @PreAuthorize("hasAnyAuthority('DRIVE_VIEW', 'INTERVIEW_ROUND_MANAGE', 'ROUND_CANDIDATE_VIEW')")
    @Operation(summary = "Get interview round details")
    public ResponseEntity<ApiResponse<InterviewRoundDto>> getRoundById(@PathVariable UUID roundId) {
        return ResponseEntity.ok(ApiResponse.success(roundService.getRoundById(roundId)));
    }

    @PutMapping("/rounds/{roundId}")
    @PreAuthorize("hasAuthority('INTERVIEW_ROUND_MANAGE')")
    @Operation(summary = "Update interview round configuration")
    public ResponseEntity<ApiResponse<InterviewRoundDto>> updateRound(
            @PathVariable UUID roundId,
            @Valid @RequestBody InterviewRoundUpdateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Interview round updated", roundService.updateRound(roundId, req, getCallerUser(auth))));
    }

    @GetMapping("/rounds/{roundId}/evaluators")
    @PreAuthorize("hasAnyAuthority('ROUND_EVALUATOR_ASSIGN', 'INTERVIEW_ROUND_MANAGE')")
    @Operation(summary = "List evaluators assigned to an interview round")
    public ResponseEntity<ApiResponse<List<RoundEvaluatorDto>>> getRoundEvaluators(@PathVariable UUID roundId) {
        return ResponseEntity.ok(ApiResponse.success(roundService.getRoundEvaluators(roundId)));
    }

    @PostMapping("/rounds/{roundId}/evaluators")
    @PreAuthorize("hasAuthority('ROUND_EVALUATOR_ASSIGN')")
    @Operation(summary = "Assign an evaluator to an interview round")
    public ResponseEntity<ApiResponse<RoundEvaluatorDto>> assignEvaluator(
            @PathVariable UUID roundId,
            @Valid @RequestBody RoundEvaluatorAssignRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Evaluator assigned", roundService.assignEvaluator(roundId, req, getCallerUser(auth))));
    }

    @DeleteMapping("/rounds/{roundId}/evaluators/{userId}")
    @PreAuthorize("hasAuthority('ROUND_EVALUATOR_ASSIGN')")
    @Operation(summary = "Remove an evaluator from an interview round")
    public ResponseEntity<ApiResponse<Void>> removeEvaluator(
            @PathVariable UUID roundId,
            @PathVariable UUID userId,
            Authentication auth
    ) {
        roundService.removeEvaluator(roundId, userId, getCallerUser(auth));
        return ResponseEntity.ok(ApiResponse.success("Evaluator removed", null));
    }
}
