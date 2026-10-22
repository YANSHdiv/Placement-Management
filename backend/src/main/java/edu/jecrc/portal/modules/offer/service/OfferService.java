package edu.jecrc.portal.modules.offer.service;

import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.offer.dto.*;
import edu.jecrc.portal.modules.offer.entity.InstitutionalOfferPolicy;
import edu.jecrc.portal.modules.offer.entity.OfferDecisionHistory;
import edu.jecrc.portal.modules.offer.entity.PlacementOffer;
import edu.jecrc.portal.modules.offer.repository.InstitutionalOfferPolicyRepository;
import edu.jecrc.portal.modules.offer.repository.OfferDecisionHistoryRepository;
import edu.jecrc.portal.modules.offer.repository.PlacementOfferRepository;
import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.entity.JobApplication;
import edu.jecrc.portal.modules.placement.entity.JobProfile;
import edu.jecrc.portal.modules.placement.repository.CompanyRepository;
import edu.jecrc.portal.modules.placement.repository.JobApplicationRepository;
import edu.jecrc.portal.modules.placement.repository.JobProfileRepository;
import edu.jecrc.portal.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferService {

    private final PlacementOfferRepository offerRepository;
    private final OfferDecisionHistoryRepository decisionHistoryRepository;
    private final InstitutionalOfferPolicyRepository policyRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final JobProfileRepository jobProfileRepository;
    private final JobApplicationRepository applicationRepository;
    private final AuditService auditService;

    @Transactional
    public PlacementOfferDto createOffer(PlacementOfferCreateRequest req, User caller) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + req.getStudentId()));

        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + req.getCompanyId()));

        JobProfile jobProfile = null;
        if (req.getJobProfileId() != null) {
            jobProfile = jobProfileRepository.findById(req.getJobProfileId())
                    .orElseThrow(() -> new ResourceNotFoundException("Job profile not found: " + req.getJobProfileId()));
        }

        JobApplication application = null;
        if (req.getApplicationId() != null) {
            application = applicationRepository.findById(req.getApplicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + req.getApplicationId()));

            // Verify student ownership on application
            if (!application.getStudent().getId().equals(student.getId())) {
                throw ApiException.badRequest("Application does not belong to the specified student");
            }

            // Verify application is in an eligible selection state
            if (!"SELECTED".equalsIgnoreCase(application.getApplicationStatus()) &&
                !"IN_ROUNDS".equalsIgnoreCase(application.getApplicationStatus()) &&
                !"SHORTLISTED".equalsIgnoreCase(application.getApplicationStatus())) {
                throw ApiException.badRequest(
                        "Cannot issue offer for application in status: " + application.getApplicationStatus() + ". Candidate must be in a qualified selection state.");
            }

            // Check duplicate non-revoked offer for this application
            Optional<PlacementOffer> existing = offerRepository.findByApplicationId(application.getId());
            if (existing.isPresent() && !"REVOKED".equals(existing.get().getOfferStatus())) {
                throw ApiException.badRequest(
                        "An active offer (" + existing.get().getOfferReferenceNumber() + ") already exists for this application");
            }
        }

        // Evaluate institutional offer policies
        InstitutionalOfferPolicy activePolicy = getActivePolicy();
        validateOfferAgainstPolicy(student, req.getTotalCtcLpa(), activePolicy);

        String offerRef = "JECRC-OFFER-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PlacementOffer offer = PlacementOffer.builder()
                .offerReferenceNumber(offerRef)
                .student(student)
                .application(application)
                .company(company)
                .jobProfile(jobProfile)
                .drive(jobProfile != null ? jobProfile.getDrive() : null)
                .appliedPolicy(activePolicy)
                .effectivePolicyVersion(activePolicy.getVersionNumber())
                .designation(req.getDesignation())
                .employmentType(req.getEmploymentType() != null ? req.getEmploymentType() : "FULL_TIME")
                .totalCtcLpa(req.getTotalCtcLpa())
                .fixedSalaryLpa(req.getFixedSalaryLpa())
                .variableSalaryLpa(req.getVariableSalaryLpa())
                .joiningBonus(req.getJoiningBonus())
                .retentionBonus(req.getRetentionBonus())
                .monthlyStipendInr(req.getMonthlyStipendInr())
                .jobLocation(req.getJobLocation())
                .termsAndConditions(req.getTermsAndConditions())
                .offerLetterDocRef(req.getOfferLetterDocRef())
                .offerStatus("DRAFT")
                .createdBy(caller)
                .build();

        PlacementOffer saved = offerRepository.save(offer);

        recordDecisionHistory(saved, "NONE", "DRAFT", caller, "Initial offer draft created");
        auditService.logAsync(caller.getId(), caller.getEmail(), "OFFER_CREATE", "PLACEMENT_OFFER",
                saved.getId().toString(), Map.of("ref", offerRef, "studentId", student.getUniversityStudentId()),
                "127.0.0.1", "Web Portal API");

        return PlacementOfferDto.fromEntity(saved);
    }

    @Transactional
    public PlacementOfferDto issueOffer(UUID offerId, PlacementOfferIssueRequest req, User caller) {
        PlacementOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + offerId));

        if (!"DRAFT".equals(offer.getOfferStatus())) {
            throw ApiException.badRequest("Only DRAFT offers can be issued. Current status: " + offer.getOfferStatus());
        }

        if (req.getDecisionDeadline() != null && req.getDecisionDeadline().isBefore(Instant.now())) {
            throw ApiException.badRequest("Decision deadline must be set in the future");
        }

        String prev = offer.getOfferStatus();
        offer.setOfferStatus("ISSUED");
        offer.setIssuanceDate(Instant.now());
        offer.setDecisionDeadline(req.getDecisionDeadline());
        offer.setIssuedBy(caller);
        if (req.getOfferLetterDocRef() != null) {
            offer.setOfferLetterDocRef(req.getOfferLetterDocRef());
        }
        if (req.getTermsAndConditions() != null) {
            offer.setTermsAndConditions(req.getTermsAndConditions());
        }

        PlacementOffer saved = offerRepository.save(offer);
        recordDecisionHistory(saved, prev, "ISSUED", caller, "Offer officially issued to candidate with deadline " + req.getDecisionDeadline());
        auditService.logAsync(caller.getId(), caller.getEmail(), "OFFER_ISSUE", "PLACEMENT_OFFER",
                saved.getId().toString(), Map.of("ref", saved.getOfferReferenceNumber()), "127.0.0.1", "Web Portal API");

        return PlacementOfferDto.fromEntity(saved);
    }

    @Transactional
    public PlacementOfferDto recordStudentDecision(UUID offerId, StudentOfferDecisionRequest req, User caller) {
        PlacementOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + offerId));

        // Verify that offer belongs to authenticated student
        Student student = studentRepository.findByUniversityEmailIgnoreCase(caller.getEmail())
                .orElseThrow(() -> ApiException.forbidden("Authenticated user is not registered as a student"));

        if (!offer.getStudent().getId().equals(student.getId())) {
            throw ApiException.forbidden("Access denied: You can only respond to your own placement offers");
        }

        // Check if decision deadline has passed
        if (offer.getDecisionDeadline() != null && Instant.now().isAfter(offer.getDecisionDeadline())) {
            if ("ISSUED".equals(offer.getOfferStatus())) {
                offer.setOfferStatus("EXPIRED");
                offerRepository.save(offer);
                recordDecisionHistory(offer, "ISSUED", "EXPIRED", null, "Decision deadline expired");
            }
            throw ApiException.badRequest("Offer decision deadline has expired on " + offer.getDecisionDeadline());
        }

        if (!"ISSUED".equals(offer.getOfferStatus())) {
            throw ApiException.badRequest(
                    "Offer cannot be responded to in status: " + offer.getOfferStatus() + ". Must be in ISSUED state.");
        }

        String decision = req.getDecision().toUpperCase();
        if (!"ACCEPTED".equals(decision) && !"DECLINED".equals(decision)) {
            throw ApiException.badRequest("Decision must be ACCEPTED or DECLINED");
        }

        String prev = offer.getOfferStatus();

        if ("ACCEPTED".equals(decision)) {
            InstitutionalOfferPolicy policy = getActivePolicy();
            if (!Boolean.TRUE.equals(policy.getAllowMultipleAccepted())) {
                List<PlacementOffer> alreadyAccepted = offerRepository.findByStudentIdAndOfferStatusIn(student.getId(), List.of("ACCEPTED"));
                if (!alreadyAccepted.isEmpty()) {
                    throw ApiException.badRequest(
                            "Institutional placement policy prohibits accepting multiple offers. You have already accepted: " +
                            alreadyAccepted.get(0).getOfferReferenceNumber() + " (" + alreadyAccepted.get(0).getCompany().getCompanyName() + ")");
                }
            }

            offer.setOfferStatus("ACCEPTED");
            offer.setStudentDecision("ACCEPTED");
            offer.setStudentDecisionAt(Instant.now());
            offer.setStudentDecisionRemarks(req.getRemarks());
            offer.setEffectivePolicyVersion(policy.getVersionNumber());

            // If offer is linked to an application, transition application to PLACED
            if (offer.getApplication() != null) {
                JobApplication app = offer.getApplication();
                app.setApplicationStatus("PLACED");
                applicationRepository.save(app);
            }

            // Atomic and auditable withdrawal of other pending applications ONLY if authorized policy requires it
            if (Boolean.TRUE.equals(policy.getAutoWithdrawOtherApplications())) {
                List<JobApplication> otherApps = applicationRepository.findByStudentId(student.getId()).stream()
                        .filter(a -> !"PLACED".equals(a.getApplicationStatus()) &&
                                     !"WITHDRAWN".equals(a.getApplicationStatus()) &&
                                     !"REJECTED".equals(a.getApplicationStatus()) &&
                                     (offer.getApplication() == null || !a.getId().equals(offer.getApplication().getId())))
                        .toList();
                for (JobApplication pendingApp : otherApps) {
                    pendingApp.setApplicationStatus("WITHDRAWN");
                    pendingApp.setStatusChangeReason("Automatically withdrawn upon accepting placement offer " +
                            offer.getOfferReferenceNumber() + " per institutional policy " + policy.getPolicyCode() + " v" + policy.getVersionNumber());
                    pendingApp.setStatusUpdatedAt(Instant.now());
                    applicationRepository.save(pendingApp);
                }
            }
        } else {
            offer.setOfferStatus("DECLINED");
            offer.setStudentDecision("DECLINED");
            offer.setStudentDecisionAt(Instant.now());
            offer.setStudentDecisionRemarks(req.getRemarks());
        }

        PlacementOffer saved = offerRepository.save(offer);
        recordDecisionHistory(saved, prev, decision, caller, req.getRemarks() != null ? req.getRemarks() : "Student recorded " + decision);
        auditService.logAsync(caller.getId(), caller.getEmail(), "OFFER_DECIDE", "PLACEMENT_OFFER",
                saved.getId().toString(), Map.of("ref", saved.getOfferReferenceNumber(), "decision", decision),
                "127.0.0.1", "Web Portal API");

        return PlacementOfferDto.fromEntity(saved);
    }

    @Transactional
    public PlacementOfferDto revokeOffer(UUID offerId, OfferRevocationRequest req, User caller) {
        PlacementOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + offerId));

        if ("REVOKED".equals(offer.getOfferStatus())) {
            throw ApiException.badRequest("Offer is already REVOKED");
        }

        String prev = offer.getOfferStatus();
        offer.setOfferStatus("REVOKED");
        offer.setRevocationReason(req.getReason());
        offer.setRevokedAt(Instant.now());
        offer.setRevokedBy(caller);

        PlacementOffer saved = offerRepository.save(offer);
        recordDecisionHistory(saved, prev, "REVOKED", caller, req.getReason());
        auditService.logAsync(caller.getId(), caller.getEmail(), "OFFER_REVOKE", "PLACEMENT_OFFER",
                saved.getId().toString(), Map.of("ref", saved.getOfferReferenceNumber(), "reason", req.getReason()),
                "127.0.0.1", "Web Portal API");

        return PlacementOfferDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PlacementOfferDto getOfferById(UUID offerId, User caller) {
        PlacementOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + offerId));

        // Enforce student ownership
        boolean isStudent = caller.getRoles().stream().anyMatch(r -> "STUDENT".equalsIgnoreCase(r.getRoleName()));
        if (isStudent && !offer.getStudent().getUniversityEmail().equalsIgnoreCase(caller.getEmail())) {
            throw ApiException.forbidden("Access denied: You are not authorized to view this offer");
        }

        return PlacementOfferDto.fromEntity(offer);
    }

    @Transactional(readOnly = true)
    public List<PlacementOfferDto> getMyOffers(String studentEmail) {
        Student student = studentRepository.findByUniversityEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for email: " + studentEmail));

        return offerRepository.findByStudentId(student.getId()).stream()
                .filter(o -> !"DRAFT".equals(o.getOfferStatus())) // Students only see ISSUED, ACCEPTED, DECLINED, etc.
                .map(PlacementOfferDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PlacementOfferDto> searchOffers(UUID studentId, UUID companyId, String offerStatus, Pageable pageable) {
        return offerRepository.searchOffers(studentId, companyId, offerStatus, pageable)
                .map(PlacementOfferDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<OfferDecisionHistoryDto> getOfferDecisionHistory(UUID offerId, User caller) {
        PlacementOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + offerId));

        boolean isStudent = caller.getRoles().stream().anyMatch(r -> "STUDENT".equalsIgnoreCase(r.getRoleName()));
        if (isStudent && !offer.getStudent().getUniversityEmail().equalsIgnoreCase(caller.getEmail())) {
            throw ApiException.forbidden("Access denied");
        }

        return decisionHistoryRepository.findByOfferIdOrderByCreatedAtDesc(offerId).stream()
                .map(OfferDecisionHistoryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public InstitutionalOfferPolicyDto getActiveOfferPolicy() {
        return InstitutionalOfferPolicyDto.fromEntity(getActivePolicy());
    }

    @Transactional
    public InstitutionalOfferPolicyDto updateOfferPolicy(InstitutionalOfferPolicyUpdateRequest req, User caller) {
        InstitutionalOfferPolicy policy = getActivePolicy();
        if (req.getPolicyName() != null) policy.setPolicyName(req.getPolicyName());
        if (req.getDescription() != null) policy.setDescription(req.getDescription());
        policy.setAllowMultipleOffers(req.getAllowMultipleOffers());
        policy.setAllowMultipleAccepted(req.getAllowMultipleAccepted());
        policy.setMaxActiveOffersPerStudent(req.getMaxActiveOffersPerStudent());
        if (req.getDreamTierMultiplierThreshold() != null) {
            policy.setDreamTierMultiplierThreshold(req.getDreamTierMultiplierThreshold());
        }
        if (req.getSuperDreamCtcThresholdLpa() != null) {
            policy.setSuperDreamCtcThresholdLpa(req.getSuperDreamCtcThresholdLpa());
        }
        policy.setAutoWithdrawOtherApplications(req.getAutoWithdrawOtherApplications());
        if (req.getIsActive() != null) policy.setIsActive(req.getIsActive());

        InstitutionalOfferPolicy saved = policyRepository.save(policy);
        auditService.logAsync(caller.getId(), caller.getEmail(), "OFFER_POLICY_UPDATE", "OFFER_POLICY",
                saved.getId().toString(), Map.of("code", saved.getPolicyCode()), "127.0.0.1", "Web Portal API");

        return InstitutionalOfferPolicyDto.fromEntity(saved);
    }

    private InstitutionalOfferPolicy getActivePolicy() {
        return policyRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc()
                .orElseGet(() -> policyRepository.save(InstitutionalOfferPolicy.builder()
                        .policyCode("JECRC_DEFAULT_OFFER_POLICY")
                        .policyName("JECRC Default Offer Policy")
                        .allowMultipleOffers(true)
                        .allowMultipleAccepted(false)
                        .maxActiveOffersPerStudent(1)
                        .dreamTierMultiplierThreshold(new BigDecimal("1.50"))
                        .superDreamCtcThresholdLpa(new BigDecimal("18.00"))
                        .autoWithdrawOtherApplications(true)
                        .isActive(true)
                        .build()));
    }

    private void validateOfferAgainstPolicy(Student student, BigDecimal newCtcLpa, InstitutionalOfferPolicy policy) {
        List<PlacementOffer> activeOffers = offerRepository.findByStudentIdAndOfferStatusIn(
                student.getId(), List.of("ISSUED", "ACCEPTED"));

        if (!Boolean.TRUE.equals(policy.getAllowMultipleOffers()) && !activeOffers.isEmpty()) {
            throw ApiException.badRequest(
                    "Institutional policy restricts candidates to a single active offer. Student already has an active offer.");
        }

        // If candidate already has an accepted offer, check dream/super-dream threshold
        Optional<PlacementOffer> acceptedOffer = activeOffers.stream()
                .filter(o -> "ACCEPTED".equals(o.getOfferStatus()))
                .findFirst();

        if (acceptedOffer.isPresent()) {
            BigDecimal existingCtc = acceptedOffer.get().getTotalCtcLpa();
            BigDecimal dreamThreshold = existingCtc.multiply(policy.getDreamTierMultiplierThreshold());
            boolean isDream = newCtcLpa.compareTo(dreamThreshold) >= 0;
            boolean isSuperDream = policy.getSuperDreamCtcThresholdLpa() != null &&
                    newCtcLpa.compareTo(policy.getSuperDreamCtcThresholdLpa()) >= 0;

            if (!isDream && !isSuperDream) {
                throw ApiException.badRequest(
                        "Candidate already accepted an offer of ₹" + existingCtc + " LPA. Institutional policy requires a Dream tier upgrade (>= ₹" +
                        dreamThreshold + " LPA or Super Dream >= ₹" + policy.getSuperDreamCtcThresholdLpa() + " LPA). Offered CTC: ₹" + newCtcLpa + " LPA");
            }
        }
    }

    private void recordDecisionHistory(PlacementOffer offer, String prev, String next, User actor, String reason) {
        OfferDecisionHistory history = OfferDecisionHistory.builder()
                .offer(offer)
                .previousStatus(prev)
                .newStatus(next)
                .actorUser(actor)
                .actorEmail(actor != null ? actor.getEmail() : "SYSTEM")
                .actorRole(actor != null && !actor.getRoles().isEmpty() ? actor.getRoles().iterator().next().getRoleName() : "SYSTEM")
                .decisionReason(reason)
                .build();
        decisionHistoryRepository.save(history);
    }
}
