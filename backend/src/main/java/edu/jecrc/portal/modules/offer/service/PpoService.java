package edu.jecrc.portal.modules.offer.service;

import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.offer.dto.*;
import edu.jecrc.portal.modules.offer.entity.InternshipRecord;
import edu.jecrc.portal.modules.offer.entity.PlacementOffer;
import edu.jecrc.portal.modules.offer.entity.PpoRecord;
import edu.jecrc.portal.modules.offer.repository.InternshipRecordRepository;
import edu.jecrc.portal.modules.offer.repository.PlacementOfferRepository;
import edu.jecrc.portal.modules.offer.repository.PpoRecordRepository;
import edu.jecrc.portal.modules.placement.entity.JobProfile;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PpoService {

    private final PpoRecordRepository ppoRepository;
    private final InternshipRecordRepository internshipRepository;
    private final PlacementOfferRepository offerRepository;
    private final JobProfileRepository jobProfileRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Transactional
    public PpoRecordDto initiatePpoReview(UUID internshipId, PpoReviewCreateRequest req, User caller) {
        InternshipRecord internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found: " + internshipId));

        Optional<PpoRecord> existing = ppoRepository.findByInternshipId(internshipId);
        if (existing.isPresent()) {
            PpoRecord ppo = existing.get();
            if (req.getRecommendationNotes() != null) {
                ppo.setRecommendationNotes(req.getRecommendationNotes());
            }
            if (req.getMinimumEvaluationScoreRequired() != null) {
                ppo.setMinimumEvaluationScoreRequired(req.getMinimumEvaluationScoreRequired());
            }
            if (req.getTargetJobProfileId() != null) {
                JobProfile profile = jobProfileRepository.findById(req.getTargetJobProfileId()).orElse(null);
                ppo.setTargetJobProfile(profile);
            }
            ppo.setPpoStatus("RECOMMENDED");
            ppo.setRecommendedBy(caller);
            PpoRecord saved = ppoRepository.save(ppo);
            return PpoRecordDto.fromEntity(saved);
        }

        JobProfile profile = null;
        if (req.getTargetJobProfileId() != null) {
            profile = jobProfileRepository.findById(req.getTargetJobProfileId()).orElse(null);
        } else if (internship.getJobProfile() != null) {
            profile = internship.getJobProfile();
        }

        PpoRecord ppo = PpoRecord.builder()
                .internship(internship)
                .student(internship.getStudent())
                .company(internship.getCompany())
                .targetJobProfile(profile)
                .ppoStatus("RECOMMENDED")
                .minimumEvaluationScoreRequired(req.getMinimumEvaluationScoreRequired())
                .recommendationNotes(req.getRecommendationNotes())
                .recommendedBy(caller)
                .build();

        PpoRecord saved = ppoRepository.save(ppo);
        auditService.logAsync(caller.getId(), caller.getEmail(), "PPO_INITIATE", "PPO_RECORD",
                saved.getId().toString(), Map.of("studentId", internship.getStudent().getUniversityStudentId()),
                "127.0.0.1", "Web Portal API");

        return PpoRecordDto.fromEntity(saved);
    }

    @Transactional
    public PpoRecordDto recordPpoDecision(UUID reviewId, PpoDecisionRequest req, User caller) {
        PpoRecord ppo = ppoRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("PPO record not found: " + reviewId));

        String decision = req.getDecision().toUpperCase();
        ppo.setDecisionRemarks(req.getDecisionRemarks());
        ppo.setDecidedBy(caller);
        ppo.setDecidedAt(Instant.now());

        if ("APPROVED".equals(decision)) {
            ppo.setPpoStatus("APPROVED");

            // If CTC terms are supplied, seamlessly create a formal placement offer in the standard workflow
            if (req.getTotalCtcLpa() != null && req.getTotalCtcLpa().compareTo(BigDecimal.ZERO) > 0) {
                String offerRef = "JECRC-PPO-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                PlacementOffer offer = PlacementOffer.builder()
                        .offerReferenceNumber(offerRef)
                        .student(ppo.getStudent())
                        .company(ppo.getCompany())
                        .jobProfile(ppo.getTargetJobProfile())
                        .drive(ppo.getTargetJobProfile() != null ? ppo.getTargetJobProfile().getDrive() : null)
                        .designation(ppo.getTargetJobProfile() != null ? ppo.getTargetJobProfile().getProfileTitle() : "Software Engineer (PPO)")
                        .employmentType("FULL_TIME")
                        .totalCtcLpa(req.getTotalCtcLpa())
                        .fixedSalaryLpa(req.getFixedSalaryLpa())
                        .variableSalaryLpa(req.getVariableSalaryLpa())
                        .joiningBonus(req.getJoiningBonus())
                        .retentionBonus(req.getRetentionBonus())
                        .jobLocation(req.getJobLocation() != null ? req.getJobLocation() : ppo.getInternship().getLocation())
                        .termsAndConditions(req.getTermsAndConditions())
                        .offerStatus("ISSUED")
                        .issuanceDate(Instant.now())
                        .decisionDeadline(req.getDecisionDeadline() != null ? req.getDecisionDeadline() : Instant.now().plusSeconds(86400 * 7))
                        .createdBy(caller)
                        .issuedBy(caller)
                        .build();

                PlacementOffer savedOffer = offerRepository.save(offer);
                ppo.setResultingOffer(savedOffer);
                ppo.setPpoStatus("OFFER_ISSUED");

                auditService.logAsync(caller.getId(), caller.getEmail(), "PPO_OFFER_ISSUED", "PLACEMENT_OFFER",
                        savedOffer.getId().toString(), Map.of("ref", offerRef, "studentId", ppo.getStudent().getUniversityStudentId()),
                        "127.0.0.1", "Web Portal API");
            }
        } else if ("DECLINED".equals(decision)) {
            ppo.setPpoStatus("DECLINED");
        } else if ("RECOMMENDED".equals(decision)) {
            ppo.setPpoStatus("RECOMMENDED");
        } else {
            throw ApiException.badRequest("Invalid PPO decision: " + req.getDecision());
        }

        PpoRecord saved = ppoRepository.save(ppo);
        auditService.logAsync(caller.getId(), caller.getEmail(), "PPO_DECISION", "PPO_RECORD",
                saved.getId().toString(), Map.of("status", saved.getPpoStatus()), "127.0.0.1", "Web Portal API");

        return PpoRecordDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<PpoRecordDto> searchPpoReviews(UUID companyId, String ppoStatus, UUID studentId, Pageable pageable) {
        return ppoRepository.searchPpoRecords(companyId, ppoStatus, studentId, pageable)
                .map(PpoRecordDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public PpoRecordDto getPpoByInternshipId(UUID internshipId, User caller) {
        PpoRecord ppo = ppoRepository.findByInternshipId(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("No PPO record found for internship: " + internshipId));

        boolean isStudent = caller.getRoles().stream().anyMatch(r -> "STUDENT".equalsIgnoreCase(r.getRoleName()));
        if (isStudent && !ppo.getStudent().getUniversityEmail().equalsIgnoreCase(caller.getEmail())) {
            throw ApiException.forbidden("Access denied");
        }

        return PpoRecordDto.fromEntity(ppo);
    }

    @Transactional(readOnly = true)
    public List<PpoRecordDto> getMyPpoRecords(String studentEmail) {
        Student student = studentRepository.findByUniversityEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        return ppoRepository.findByStudentId(student.getId()).stream()
                .map(PpoRecordDto::fromEntity)
                .toList();
    }
}
