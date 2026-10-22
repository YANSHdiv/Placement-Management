package edu.jecrc.portal.modules.interview.service;

import edu.jecrc.portal.common.exception.BadRequestException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.interview.dto.*;
import edu.jecrc.portal.modules.interview.entity.InterviewRound;
import edu.jecrc.portal.modules.interview.entity.RoundEvaluator;
import edu.jecrc.portal.modules.interview.repository.InterviewRoundRepository;
import edu.jecrc.portal.modules.interview.repository.RoundEvaluatorRepository;
import edu.jecrc.portal.modules.placement.entity.JobProfile;
import edu.jecrc.portal.modules.placement.repository.JobProfileRepository;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewRoundService {

    private final InterviewRoundRepository roundRepository;
    private final RoundEvaluatorRepository evaluatorRepository;
    private final JobProfileRepository jobProfileRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public InterviewRoundDto createRound(UUID jobProfileId, InterviewRoundCreateRequest req, User caller) {
        JobProfile jobProfile = jobProfileRepository.findById(jobProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Job profile not found with id: " + jobProfileId));

        if (roundRepository.existsByJobProfileIdAndSequenceOrder(jobProfileId, req.getSequenceOrder())) {
            throw new BadRequestException("A round with sequence order " + req.getSequenceOrder() + " already exists for this job profile");
        }

        InterviewRound round = InterviewRound.builder()
                .jobProfile(jobProfile)
                .roundName(req.getRoundName().trim())
                .roundType(req.getRoundType().trim().toUpperCase())
                .sequenceOrder(req.getSequenceOrder())
                .instructions(req.getInstructions())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .venueOrMeetingUrl(req.getVenueOrMeetingUrl())
                .candidateSchedulingMode(req.getCandidateSchedulingMode() != null ? req.getCandidateSchedulingMode().trim().toUpperCase() : "SCHEDULED_SLOTS")
                .evaluationCriteria(req.getEvaluationCriteria())
                .maxScore(req.getMaxScore())
                .scoringScaleDescription(req.getScoringScaleDescription())
                .publicationStatus("DRAFT")
                .isResultsPublished(false)
                .createdBy(caller)
                .build();

        InterviewRound saved = roundRepository.save(round);

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "INTERVIEW_ROUND_CREATED",
                "InterviewRound",
                saved.getId().toString(),
                Map.of("roundName", saved.getRoundName(), "sequence", saved.getSequenceOrder(), "profileId", jobProfileId.toString()),
                "127.0.0.1",
                "Web Portal API"
        );

        return InterviewRoundDto.fromEntity(saved);
    }

    @Transactional
    public InterviewRoundDto updateRound(UUID roundId, InterviewRoundUpdateRequest req, User caller) {
        InterviewRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview round not found with id: " + roundId));

        if (Boolean.TRUE.equals(round.getIsResultsPublished())) {
            log.warn("Round {} results are already published. Modifications are strictly audited.", roundId);
        }

        if (req.getSequenceOrder() != null && !req.getSequenceOrder().equals(round.getSequenceOrder())) {
            if (roundRepository.existsByJobProfileIdAndSequenceOrder(round.getJobProfile().getId(), req.getSequenceOrder())) {
                throw new BadRequestException("A round with sequence order " + req.getSequenceOrder() + " already exists for this job profile");
            }
            round.setSequenceOrder(req.getSequenceOrder());
        }

        if (req.getRoundName() != null && !req.getRoundName().isBlank()) {
            round.setRoundName(req.getRoundName().trim());
        }
        if (req.getRoundType() != null && !req.getRoundType().isBlank()) {
            round.setRoundType(req.getRoundType().trim().toUpperCase());
        }
        if (req.getInstructions() != null) {
            round.setInstructions(req.getInstructions());
        }
        if (req.getStartTime() != null) {
            round.setStartTime(req.getStartTime());
        }
        if (req.getEndTime() != null) {
            round.setEndTime(req.getEndTime());
        }
        if (req.getVenueOrMeetingUrl() != null) {
            round.setVenueOrMeetingUrl(req.getVenueOrMeetingUrl().trim());
        }
        if (req.getCandidateSchedulingMode() != null) {
            round.setCandidateSchedulingMode(req.getCandidateSchedulingMode().trim().toUpperCase());
        }
        if (req.getEvaluationCriteria() != null) {
            round.setEvaluationCriteria(req.getEvaluationCriteria());
        }
        if (req.getMaxScore() != null) {
            round.setMaxScore(req.getMaxScore());
        }
        if (req.getScoringScaleDescription() != null) {
            round.setScoringScaleDescription(req.getScoringScaleDescription().trim());
        }
        if (req.getPublicationStatus() != null) {
            round.setPublicationStatus(req.getPublicationStatus().trim().toUpperCase());
        }

        round.setUpdatedBy(caller);
        InterviewRound updated = roundRepository.save(round);

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "INTERVIEW_ROUND_UPDATED",
                "InterviewRound",
                updated.getId().toString(),
                Map.of("roundName", updated.getRoundName(), "status", updated.getPublicationStatus()),
                "127.0.0.1",
                "Web Portal API"
        );

        return InterviewRoundDto.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public List<InterviewRoundDto> getRoundsForJobProfile(UUID jobProfileId) {
        return roundRepository.findByJobProfileIdOrderBySequenceOrderAsc(jobProfileId).stream()
                .map(InterviewRoundDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InterviewRoundDto getRoundById(UUID roundId) {
        InterviewRound round = roundRepository.findWithDetailsById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview round not found with id: " + roundId));
        return InterviewRoundDto.fromEntity(round);
    }

    @Transactional
    public RoundEvaluatorDto assignEvaluator(UUID roundId, RoundEvaluatorAssignRequest req, User caller) {
        InterviewRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview round not found with id: " + roundId));

        User evaluatorUser = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator user not found with id: " + req.getUserId()));

        if (evaluatorRepository.existsByRoundIdAndUserId(roundId, req.getUserId())) {
            throw new BadRequestException("Evaluator is already assigned to this interview round");
        }

        RoundEvaluator evaluator = RoundEvaluator.builder()
                .round(round)
                .user(evaluatorUser)
                .roleOrDesignation(req.getRoleOrDesignation() != null ? req.getRoleOrDesignation().trim() : "Evaluator")
                .assignedBy(caller)
                .build();

        RoundEvaluator saved = evaluatorRepository.save(evaluator);

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "ROUND_EVALUATOR_ASSIGNED",
                "RoundEvaluator",
                saved.getId().toString(),
                Map.of("roundId", roundId.toString(), "evaluatorEmail", evaluatorUser.getEmail()),
                "127.0.0.1",
                "Web Portal API"
        );

        return RoundEvaluatorDto.fromEntity(saved);
    }

    @Transactional
    public void removeEvaluator(UUID roundId, UUID userId, User caller) {
        if (!evaluatorRepository.existsByRoundIdAndUserId(roundId, userId)) {
            throw new ResourceNotFoundException("Evaluator assignment not found for round " + roundId + " and user " + userId);
        }

        evaluatorRepository.deleteByRoundIdAndUserId(roundId, userId);

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "ROUND_EVALUATOR_REMOVED",
                "RoundEvaluator",
                roundId.toString(),
                Map.of("removedUserId", userId.toString()),
                "127.0.0.1",
                "Web Portal API"
        );
    }

    @Transactional(readOnly = true)
    public List<RoundEvaluatorDto> getRoundEvaluators(UUID roundId) {
        return evaluatorRepository.findWithUsersByRoundId(roundId).stream()
                .map(RoundEvaluatorDto::fromEntity)
                .collect(Collectors.toList());
    }
}
