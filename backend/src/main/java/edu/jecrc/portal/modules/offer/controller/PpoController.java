package edu.jecrc.portal.modules.offer.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.offer.dto.PpoDecisionRequest;
import edu.jecrc.portal.modules.offer.dto.PpoRecordDto;
import edu.jecrc.portal.modules.offer.dto.PpoReviewCreateRequest;
import edu.jecrc.portal.modules.offer.service.PpoService;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@Tag(name = "PPO Reviews", description = "Pre-Placement Offer (PPO) tracking, recommendations, and conversion workflows")
public class PpoController {

    private final PpoService ppoService;
    private final UserRepository userRepository;

    private User getCallerUser(Authentication auth) {
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @PostMapping("/internships/{internshipId}/ppo-reviews")
    @PreAuthorize("hasAuthority('PPO_REVIEW')")
    @Operation(summary = "Initiate or recommend an intern for Pre-Placement Offer (PPO)")
    public ResponseEntity<ApiResponse<PpoRecordDto>> initiatePpoReview(
            @PathVariable UUID internshipId,
            @Valid @RequestBody PpoReviewCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("PPO review initiated successfully", ppoService.initiatePpoReview(internshipId, req, getCallerUser(auth))));
    }

    @GetMapping("/ppo-reviews")
    @PreAuthorize("hasAuthority('PPO_REVIEW')")
    @Operation(summary = "Search PPO candidate reviews across companies and status")
    public ResponseEntity<ApiResponse<PageResponse<PpoRecordDto>>> searchPpoReviews(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) String ppoStatus,
            @RequestParam(required = false) UUID studentId,
            @PageableDefault(size = 30) Pageable pageable
    ) {
        Page<PpoRecordDto> page = ppoService.searchPpoReviews(companyId, ppoStatus, studentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/internships/{internshipId}/ppo")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get PPO tracking record for an internship")
    public ResponseEntity<ApiResponse<PpoRecordDto>> getPpoByInternshipId(
            @PathVariable UUID internshipId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success(ppoService.getPpoByInternshipId(internshipId, getCallerUser(auth))));
    }

    @PostMapping("/ppo-reviews/{reviewId}/decision")
    @PreAuthorize("hasAuthority('PPO_APPROVE')")
    @Operation(summary = "Authorized TPO records PPO approval/decline and generates formal placement offer")
    public ResponseEntity<ApiResponse<PpoRecordDto>> recordPpoDecision(
            @PathVariable UUID reviewId,
            @Valid @RequestBody PpoDecisionRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("PPO decision recorded successfully", ppoService.recordPpoDecision(reviewId, req, getCallerUser(auth))));
    }

    @GetMapping("/ppo-reviews/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Student views own PPO conversion statuses")
    public ResponseEntity<ApiResponse<List<PpoRecordDto>>> getMyPpoRecords(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(ppoService.getMyPpoRecords(auth.getName())));
    }
}
