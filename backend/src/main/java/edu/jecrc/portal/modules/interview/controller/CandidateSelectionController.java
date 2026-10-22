package edu.jecrc.portal.modules.interview.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.interview.dto.*;
import edu.jecrc.portal.modules.interview.service.CandidateSelectionService;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Candidate Selection & Progression", description = "Recruitment round shortlisting, result upload & publishing")
public class CandidateSelectionController {

    private final CandidateSelectionService selectionService;
    private final UserRepository userRepository;

    private User getCallerUser(Authentication auth) {
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @GetMapping("/rounds/{roundId}/candidates")
    @PreAuthorize("hasAuthority('ROUND_CANDIDATE_VIEW')")
    @Operation(summary = "Get paginated candidates roster for an interview round")
    public ResponseEntity<ApiResponse<PageResponse<RoundCandidateDto>>> getCandidatesForRound(
            @PathVariable UUID roundId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 50) Pageable pageable,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success(selectionService.getCandidatesForRound(roundId, status, pageable, getCallerUser(auth))));
    }

    @PostMapping("/rounds/{roundId}/candidates/shortlist")
    @PreAuthorize("hasAuthority('INTERVIEW_ROUND_MANAGE')")
    @Operation(summary = "Shortlist applications into an interview round")
    public ResponseEntity<ApiResponse<List<RoundCandidateDto>>> shortlistCandidates(
            @PathVariable UUID roundId,
            @Valid @RequestBody CandidateShortlistRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Candidates shortlisted for round", selectionService.shortlistCandidates(roundId, req, getCallerUser(auth))));
    }

    @PutMapping("/round-candidates/{candidateId}/result")
    @PreAuthorize("hasAuthority('ROUND_RESULT_UPLOAD')")
    @Operation(summary = "Update an individual candidate's round result")
    public ResponseEntity<ApiResponse<RoundCandidateDto>> updateCandidateResult(
            @PathVariable UUID candidateId,
            @Valid @RequestBody CandidateResultUpdateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Candidate result updated", selectionService.updateCandidateResult(candidateId, req, getCallerUser(auth))));
    }

    @PostMapping("/rounds/{roundId}/evaluations/bulk")
    @PreAuthorize("hasAuthority('ROUND_RESULT_UPLOAD')")
    @Operation(summary = "Bulk upload round candidate results with row-level validation")
    public ResponseEntity<ApiResponse<BulkCandidateResultResponse>> bulkUploadResults(
            @PathVariable UUID roundId,
            @Valid @RequestBody BulkCandidateResultUploadRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Bulk results processed", selectionService.bulkUploadResults(roundId, req, getCallerUser(auth))));
    }

    @PostMapping("/rounds/{roundId}/results/publish")
    @PreAuthorize("hasAuthority('ROUND_RESULT_PUBLISH')")
    @Operation(summary = "Publish round results and advance candidates")
    public ResponseEntity<ApiResponse<InterviewRoundDto>> publishRoundResults(
            @PathVariable UUID roundId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Round results published successfully", selectionService.publishRoundResults(roundId, getCallerUser(auth))));
    }

    @GetMapping("/applications/{applicationId}/timeline")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get candidate status progression history for an application")
    public ResponseEntity<ApiResponse<List<CandidateTimelineDto>>> getApplicationTimeline(
            @PathVariable UUID applicationId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success(selectionService.getApplicationTimeline(applicationId, getCallerUser(auth))));
    }
}
