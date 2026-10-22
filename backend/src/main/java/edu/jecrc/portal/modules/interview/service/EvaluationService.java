package edu.jecrc.portal.modules.interview.service;

import edu.jecrc.portal.common.exception.ForbiddenException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.interview.dto.RoundEvaluationDto;
import edu.jecrc.portal.modules.interview.dto.RoundEvaluationSubmitRequest;
import edu.jecrc.portal.modules.interview.entity.RoundCandidate;
import edu.jecrc.portal.modules.interview.entity.RoundEvaluation;
import edu.jecrc.portal.modules.interview.repository.RoundCandidateRepository;
import edu.jecrc.portal.modules.interview.repository.RoundEvaluationRepository;
import edu.jecrc.portal.modules.interview.repository.RoundEvaluatorRepository;
import edu.jecrc.portal.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final RoundEvaluationRepository evaluationRepository;
    private final RoundCandidateRepository candidateRepository;
    private final RoundEvaluatorRepository evaluatorRepository;
    private final AuditService auditService;

    @Transactional
    public RoundEvaluationDto submitEvaluation(UUID roundCandidateId, RoundEvaluationSubmitRequest req, User caller) {
        RoundCandidate candidate = candidateRepository.findWithDetailsById(roundCandidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Round candidate not found with id: " + roundCandidateId));

        boolean isStaff = caller.getRoles().stream().anyMatch(r ->
                List.of("SUPER_ADMIN", "TPO_ADMIN", "TPO_COORDINATOR").contains(r.getRoleName()));

        boolean isAssigned = evaluatorRepository.existsByRoundIdAndUserId(candidate.getRound().getId(), caller.getId());
        if (!isStaff && !isAssigned) {
            throw new ForbiddenException("Access denied: You are not assigned as an evaluator for this interview round");
        }

        RoundEvaluation evaluation = evaluationRepository
                .findByCandidateIdAndEvaluatorUserId(roundCandidateId, caller.getId())
                .orElseGet(() -> RoundEvaluation.builder()
                        .candidate(candidate)
                        .evaluatorUser(caller)
                        .build());

        evaluation.setScore(req.getScore());
        evaluation.setRecommendation(req.getRecommendation() != null ? req.getRecommendation().trim().toUpperCase() : null);
        evaluation.setQualitativeFeedback(req.getQualitativeFeedback());
        evaluation.setConfidentialNotes(req.getConfidentialNotes());

        boolean isDraft = Boolean.TRUE.equals(req.getIsDraft());
        evaluation.setEvaluationState(isDraft ? "DRAFT" : "SUBMITTED");
        if (!isDraft && evaluation.getSubmittedAt() == null) {
            evaluation.setSubmittedAt(Instant.now());
        }

        RoundEvaluation saved = evaluationRepository.save(evaluation);

        // Score aggregation strategy: when submitted, calculate arithmetic average of all submitted evaluator scores
        if (!isDraft) {
            List<RoundEvaluation> allEvals = evaluationRepository.findByCandidateId(roundCandidateId);
            List<BigDecimal> submittedScores = allEvals.stream()
                    .filter(e -> "SUBMITTED".equalsIgnoreCase(e.getEvaluationState()) && e.getScore() != null)
                    .map(RoundEvaluation::getScore)
                    .collect(Collectors.toList());

            if (!submittedScores.isEmpty()) {
                BigDecimal sum = submittedScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal avg = sum.divide(BigDecimal.valueOf(submittedScores.size()), 2, RoundingMode.HALF_UP);
                candidate.setFinalScore(avg);
                candidateRepository.save(candidate);
            }
        }

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "ROUND_EVALUATION_RECORDED",
                "RoundEvaluation",
                saved.getId().toString(),
                Map.of("candidateId", roundCandidateId.toString(), "state", saved.getEvaluationState(), "score", saved.getScore() != null ? saved.getScore() : "None"),
                "127.0.0.1",
                "Web Portal API"
        );

        return RoundEvaluationDto.fromEntity(saved, true);
    }

    @Transactional(readOnly = true)
    public List<RoundEvaluationDto> getEvaluationsForCandidate(UUID roundCandidateId, User caller) {
        // Students are prohibited from viewing raw confidential evaluations
        boolean isStudent = caller.getRoles().stream().anyMatch(r -> "STUDENT".equalsIgnoreCase(r.getRoleName()));
        if (isStudent) {
            throw new ForbiddenException("Access denied: Private evaluator assessments are restricted from student view");
        }

        boolean isStaff = caller.getRoles().stream().anyMatch(r ->
                List.of("SUPER_ADMIN", "TPO_ADMIN", "TPO_COORDINATOR").contains(r.getRoleName()));

        List<RoundEvaluation> evals = evaluationRepository.findByCandidateId(roundCandidateId);

        if (!isStaff) {
            // Evaluator can only see their own submission
            evals = evals.stream()
                    .filter(e -> e.getEvaluatorUser().getId().equals(caller.getId()))
                    .collect(Collectors.toList());
        }

        return evals.stream()
                .map(e -> RoundEvaluationDto.fromEntity(e, isStaff || e.getEvaluatorUser().getId().equals(caller.getId())))
                .collect(Collectors.toList());
    }
}
