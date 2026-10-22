package edu.jecrc.portal.modules.offer.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.offer.dto.*;
import edu.jecrc.portal.modules.offer.service.OfferService;
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
@Tag(name = "Offers", description = "Placement offer creation, issuance, student decisions, and institutional policies")
public class OfferController {

    private final OfferService offerService;
    private final UserRepository userRepository;

    private User getCallerUser(Authentication auth) {
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @PostMapping("/offers")
    @PreAuthorize("hasAuthority('OFFER_CREATE')")
    @Operation(summary = "Create/draft a placement offer for a selected candidate")
    public ResponseEntity<ApiResponse<PlacementOfferDto>> createOffer(
            @Valid @RequestBody PlacementOfferCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Offer drafted successfully", offerService.createOffer(req, getCallerUser(auth))));
    }

    @GetMapping("/offers")
    @PreAuthorize("hasAuthority('OFFER_VIEW')")
    @Operation(summary = "Search placement offers with student, company, and status filters")
    public ResponseEntity<ApiResponse<PageResponse<PlacementOfferDto>>> searchOffers(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) String offerStatus,
            @PageableDefault(size = 30) Pageable pageable
    ) {
        Page<PlacementOfferDto> page = offerService.searchOffers(studentId, companyId, offerStatus, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/offers/{offerId}")
    @PreAuthorize("hasAuthority('OFFER_VIEW')")
    @Operation(summary = "Get placement offer details")
    public ResponseEntity<ApiResponse<PlacementOfferDto>> getOfferById(
            @PathVariable UUID offerId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success(offerService.getOfferById(offerId, getCallerUser(auth))));
    }

    @PostMapping("/offers/{offerId}/issue")
    @PreAuthorize("hasAuthority('OFFER_ISSUE')")
    @Operation(summary = "Officially issue an offer to a student with a decision deadline")
    public ResponseEntity<ApiResponse<PlacementOfferDto>> issueOffer(
            @PathVariable UUID offerId,
            @Valid @RequestBody PlacementOfferIssueRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Offer issued to candidate successfully", offerService.issueOffer(offerId, req, getCallerUser(auth))));
    }

    @PostMapping("/offers/{offerId}/decision")
    @PreAuthorize("hasAuthority('OFFER_DECIDE')")
    @Operation(summary = "Candidate responds to an offer (ACCEPT or DECLINE)")
    public ResponseEntity<ApiResponse<PlacementOfferDto>> recordStudentDecision(
            @PathVariable UUID offerId,
            @Valid @RequestBody StudentOfferDecisionRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Offer decision recorded successfully", offerService.recordStudentDecision(offerId, req, getCallerUser(auth))));
    }

    @PostMapping("/offers/{offerId}/revoke")
    @PreAuthorize("hasAuthority('OFFER_REVOKE')")
    @Operation(summary = "Revoke or cancel an issued or drafted offer")
    public ResponseEntity<ApiResponse<PlacementOfferDto>> revokeOffer(
            @PathVariable UUID offerId,
            @Valid @RequestBody OfferRevocationRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Offer revoked successfully", offerService.revokeOffer(offerId, req, getCallerUser(auth))));
    }

    @GetMapping("/offers/me")
    @PreAuthorize("hasAuthority('OFFER_VIEW')")
    @Operation(summary = "Get issued/decided offers for the authenticated student")
    public ResponseEntity<ApiResponse<List<PlacementOfferDto>>> getMyOffers(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(offerService.getMyOffers(auth.getName())));
    }

    @GetMapping("/offers/{offerId}/history")
    @PreAuthorize("hasAuthority('OFFER_VIEW')")
    @Operation(summary = "View immutable audit history of an offer's decision transitions")
    public ResponseEntity<ApiResponse<List<OfferDecisionHistoryDto>>> getOfferDecisionHistory(
            @PathVariable UUID offerId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success(offerService.getOfferDecisionHistory(offerId, getCallerUser(auth))));
    }

    @GetMapping("/offer-policies/active")
    @PreAuthorize("hasAnyAuthority('OFFER_VIEW', 'OFFER_POLICY_MANAGE')")
    @Operation(summary = "Get active institutional multiple-offer and CTC threshold policy")
    public ResponseEntity<ApiResponse<InstitutionalOfferPolicyDto>> getActivePolicy() {
        return ResponseEntity.ok(ApiResponse.success(offerService.getActiveOfferPolicy()));
    }

    @PutMapping("/offer-policies/active")
    @PreAuthorize("hasAuthority('OFFER_POLICY_MANAGE')")
    @Operation(summary = "Update institutional multiple-offer rules and tier thresholds")
    public ResponseEntity<ApiResponse<InstitutionalOfferPolicyDto>> updatePolicy(
            @Valid @RequestBody InstitutionalOfferPolicyUpdateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Institutional offer policy updated", offerService.updateOfferPolicy(req, getCallerUser(auth))));
    }
}
