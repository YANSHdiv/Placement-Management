package edu.jecrc.portal.modules.placement.service;

import edu.jecrc.portal.common.exception.BadRequestException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.placement.dto.EligibilityEvaluationDto;
import edu.jecrc.portal.modules.placement.dto.EligibilityOverrideRequest;
import edu.jecrc.portal.modules.placement.entity.EligibilityEvaluation;
import edu.jecrc.portal.modules.placement.entity.EligibilityOverride;
import edu.jecrc.portal.modules.placement.repository.EligibilityEvaluationRepository;
import edu.jecrc.portal.modules.placement.repository.EligibilityOverrideRepository;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EligibilityOverrideService {

    private final EligibilityEvaluationRepository evaluationRepository;
    private final EligibilityOverrideRepository overrideRepository;
    private final UserRepository userRepository;
    private final EligibilityEngineService eligibilityEngineService;
    private final AuditService auditService;

    @Transactional
    public EligibilityEvaluationDto overrideEvaluation(UUID evaluationId, EligibilityOverrideRequest req, String userEmail) {
        if (req.getReason() == null || req.getReason().trim().isBlank()) {
            throw new BadRequestException("Mandatory override justification reason must be provided");
        }

        EligibilityEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found with id: " + evaluationId));

        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Create or update override
        EligibilityOverride override = overrideRepository.findByEvaluationId(evaluationId)
                .orElseGet(() -> EligibilityOverride.builder()
                        .evaluation(evaluation)
                        .originalOutcome(evaluation.getOutcome())
                        .build());

        override.setApprovedByUser(user);
        override.setOverrideReason(req.getReason().trim());
        override.setOverrideOutcome("ELIGIBLE");
        override.setOverriddenAt(Instant.now());

        overrideRepository.save(override);

        auditService.logAsync(
                user.getId(),
                userEmail,
                "OVERRIDE_ELIGIBILITY",
                "EligibilityOverride",
                override.getId() != null ? override.getId().toString() : evaluation.getId().toString(),
                Map.of(
                        "evaluationId", evaluation.getId().toString(),
                        "originalOutcome", evaluation.getOutcome(),
                        "reason", req.getReason()
                ),
                null,
                null
        );

        return eligibilityEngineService.toDto(evaluation);
    }
}
