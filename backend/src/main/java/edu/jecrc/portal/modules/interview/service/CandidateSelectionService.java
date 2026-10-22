package edu.jecrc.portal.modules.interview.service;

import edu.jecrc.portal.common.exception.BadRequestException;
import edu.jecrc.portal.common.exception.ForbiddenException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.interview.dto.*;
import edu.jecrc.portal.modules.interview.entity.CandidateStatusHistory;
import edu.jecrc.portal.modules.interview.entity.InterviewRound;
import edu.jecrc.portal.modules.interview.entity.RoundCandidate;
import edu.jecrc.portal.modules.interview.repository.CandidateStatusHistoryRepository;
import edu.jecrc.portal.modules.interview.repository.InterviewRoundRepository;
import edu.jecrc.portal.modules.interview.repository.RoundCandidateRepository;
import edu.jecrc.portal.modules.interview.repository.RoundEvaluatorRepository;
import edu.jecrc.portal.modules.placement.entity.JobApplication;
import edu.jecrc.portal.modules.placement.repository.JobApplicationRepository;
import edu.jecrc.portal.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateSelectionService {

    private final InterviewRoundRepository roundRepository;
    private final RoundCandidateRepository roundCandidateRepository;
    private final RoundEvaluatorRepository evaluatorRepository;
    private final JobApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final CandidateStatusHistoryRepository statusHistoryRepository;
    private final AuditService auditService;

    @Transactional
    public List<RoundCandidateDto> shortlistCandidates(UUID roundId, CandidateShortlistRequest req, User caller) {
        InterviewRound round = roundRepository.findWithDetailsById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview round not found with id: " + roundId));

        List<RoundCandidate> createdCandidates = new ArrayList<>();
        UUID jobProfileId = round.getJobProfile().getId();

        // If not round 1, check previous round progression
        Optional<InterviewRound> prevRoundOpt = Optional.empty();
        if (round.getSequenceOrder() > 1) {
            prevRoundOpt = roundRepository.findByJobProfileIdAndSequenceOrder(jobProfileId, round.getSequenceOrder() - 1);
        }

        for (UUID appId : req.getApplicationIds()) {
            JobApplication app = applicationRepository.findById(appId)
                    .orElseThrow(() -> new ResourceNotFoundException("Job application not found with id: " + appId));

            if (!app.getJobProfile().getId().equals(jobProfileId)) {
                throw new BadRequestException("Application " + appId + " does not belong to the job profile of this interview round");
            }

            if ("WITHDRAWN".equalsIgnoreCase(app.getApplicationStatus())) {
                throw new BadRequestException("Application " + appId + " has been withdrawn by candidate and cannot be shortlisted");
            }

            if (prevRoundOpt.isPresent()) {
                InterviewRound prevRound = prevRoundOpt.get();
                Optional<RoundCandidate> prevCandidateOpt = roundCandidateRepository.findByRoundIdAndApplicationId(prevRound.getId(), appId);
                if (prevCandidateOpt.isEmpty() || !"ADVANCED".equalsIgnoreCase(prevCandidateOpt.get().getCandidateRoundStatus())) {
                    throw new BadRequestException("Candidate " + app.getStudent().getFullName() + " was not marked ADVANCED in the previous round (" + prevRound.getRoundName() + ")");
                }
            }

            if (roundCandidateRepository.existsByRoundIdAndApplicationId(roundId, appId)) {
                continue; // Already scheduled for this round
            }

            RoundCandidate candidate = RoundCandidate.builder()
                    .round(round)
                    .application(app)
                    .candidateRoundStatus("SCHEDULED")
                    .tpoDecisionRemarks(req.getRemarks())
                    .build();

            RoundCandidate saved = roundCandidateRepository.save(candidate);
            createdCandidates.add(saved);

            // Update application status
            String oldStatus = app.getApplicationStatus();
            app.setApplicationStatus("IN_ROUNDS");
            app.setStatusUpdatedAt(Instant.now());
            app.setStatusUpdatedBy(caller);
            app.setStatusChangeReason("Shortlisted for Round: " + round.getRoundName());
            applicationRepository.save(app);

            // Record history
            statusHistoryRepository.save(CandidateStatusHistory.builder()
                    .application(app)
                    .round(round)
                    .fromStatus(oldStatus)
                    .toStatus("IN_ROUNDS")
                    .changedBy(caller)
                    .remarks("Shortlisted for Round " + round.getSequenceOrder() + ": " + round.getRoundName())
                    .build());
        }

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "CANDIDATES_SHORTLISTED_FOR_ROUND",
                "InterviewRound",
                roundId.toString(),
                Map.of("roundName", round.getRoundName(), "shortlistedCount", createdCandidates.size()),
                "127.0.0.1",
                "Web Portal API"
        );

        return createdCandidates.stream().map(RoundCandidateDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<RoundCandidateDto> getCandidatesForRound(UUID roundId, String status, Pageable pageable, User caller) {
        // Enforce evaluator access isolation
        boolean isStaff = caller.getRoles().stream().anyMatch(r ->
                List.of("SUPER_ADMIN", "TPO_ADMIN", "TPO_COORDINATOR").contains(r.getRoleName()));

        if (!isStaff) {
            boolean isAssignedEvaluator = evaluatorRepository.existsByRoundIdAndUserId(roundId, caller.getId());
            if (!isAssignedEvaluator) {
                throw new ForbiddenException("Access denied: You are not an assigned evaluator for this interview round");
            }
        }

        Page<RoundCandidate> page = roundCandidateRepository.findByRoundIdAndStatusPageable(roundId, status, pageable);
        return PageResponse.from(page.map(RoundCandidateDto::fromEntity));
    }

    @Transactional
    public RoundCandidateDto updateCandidateResult(UUID candidateId, CandidateResultUpdateRequest req, User caller) {
        RoundCandidate candidate = roundCandidateRepository.findWithDetailsById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Round candidate not found with id: " + candidateId));

        String oldStatus = candidate.getCandidateRoundStatus();
        candidate.setCandidateRoundStatus(req.getCandidateRoundStatus().trim().toUpperCase());
        if (req.getFinalScore() != null) {
            candidate.setFinalScore(req.getFinalScore());
        }
        if (req.getRemarks() != null) {
            candidate.setTpoDecisionRemarks(req.getRemarks());
        }

        RoundCandidate updated = roundCandidateRepository.save(candidate);

        // Record history
        statusHistoryRepository.save(CandidateStatusHistory.builder()
                .application(candidate.getApplication())
                .round(candidate.getRound())
                .fromStatus(oldStatus)
                .toStatus(updated.getCandidateRoundStatus())
                .changedBy(caller)
                .remarks(req.getRemarks() != null ? req.getRemarks() : "Round result updated")
                .build());

        return RoundCandidateDto.fromEntity(updated);
    }

    @Transactional
    public BulkCandidateResultResponse bulkUploadResults(UUID roundId, BulkCandidateResultUploadRequest req, User caller) {
        InterviewRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview round not found with id: " + roundId));

        List<RoundCandidate> roundCandidates = roundCandidateRepository.findWithDetailsByRoundId(roundId);
        Map<UUID, RoundCandidate> appMap = roundCandidates.stream()
                .collect(Collectors.toMap(c -> c.getApplication().getId(), c -> c, (c1, c2) -> c1));
        Map<String, RoundCandidate> rollMap = roundCandidates.stream()
                .filter(c -> c.getApplication().getStudent() != null)
                .collect(Collectors.toMap(c -> c.getApplication().getStudent().getUniversityStudentId(), c -> c, (c1, c2) -> c1));

        List<BulkCandidateResultResponse.RowError> errors = new ArrayList<>();
        int successCount = 0;

        if (req.getResults() != null) {
            for (BulkCandidateResultUploadRequest.ResultRow row : req.getResults()) {
                RoundCandidate match = null;
                if (row.getApplicationId() != null) {
                    match = appMap.get(row.getApplicationId());
                } else if (row.getUniversityStudentId() != null && !row.getUniversityStudentId().isBlank()) {
                    match = rollMap.get(row.getUniversityStudentId().trim());
                }

                if (match == null) {
                    errors.add(BulkCandidateResultResponse.RowError.builder()
                            .rowNumber(row.getRowNumber())
                            .identifier(row.getApplicationId() != null ? row.getApplicationId().toString() : row.getUniversityStudentId())
                            .errorMessage("Candidate not found in this round's roster")
                            .build());
                    continue;
                }

                if (row.getStatus() == null || row.getStatus().isBlank()) {
                    errors.add(BulkCandidateResultResponse.RowError.builder()
                            .rowNumber(row.getRowNumber())
                            .identifier(match.getApplication().getStudent().getUniversityStudentId())
                            .errorMessage("Status is required")
                            .build());
                    continue;
                }

                String upperStatus = row.getStatus().trim().toUpperCase();
                if (!List.of("SCHEDULED", "IN_PROGRESS", "ADVANCED", "ELIMINATED", "ON_HOLD", "ABSENT", "EXEMPTED").contains(upperStatus)) {
                    errors.add(BulkCandidateResultResponse.RowError.builder()
                            .rowNumber(row.getRowNumber())
                            .identifier(match.getApplication().getStudent().getUniversityStudentId())
                            .errorMessage("Invalid status: " + upperStatus)
                            .build());
                    continue;
                }

                String prevStatus = match.getCandidateRoundStatus();
                match.setCandidateRoundStatus(upperStatus);
                if (row.getScore() != null) {
                    match.setFinalScore(row.getScore());
                }
                if (row.getRemarks() != null) {
                    match.setTpoDecisionRemarks(row.getRemarks());
                }
                roundCandidateRepository.save(match);
                successCount++;

                statusHistoryRepository.save(CandidateStatusHistory.builder()
                        .application(match.getApplication())
                        .round(round)
                        .fromStatus(prevStatus)
                        .toStatus(upperStatus)
                        .changedBy(caller)
                        .remarks(row.getRemarks() != null ? row.getRemarks() : "Bulk result upload")
                        .build());
            }
        }

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "BULK_ROUND_RESULTS_UPLOADED",
                "InterviewRound",
                roundId.toString(),
                Map.of("totalRows", req.getResults() != null ? req.getResults().size() : 0, "successful", successCount, "failed", errors.size()),
                "127.0.0.1",
                "Web Portal API"
        );

        return BulkCandidateResultResponse.builder()
                .totalRows(req.getResults() != null ? req.getResults().size() : 0)
                .successfulCount(successCount)
                .failedCount(errors.size())
                .errors(errors)
                .build();
    }

    @Transactional
    public InterviewRoundDto publishRoundResults(UUID roundId, User caller) {
        InterviewRound round = roundRepository.findWithDetailsById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview round not found with id: " + roundId));

        round.setIsResultsPublished(true);
        round.setResultsPublishedAt(Instant.now());
        round.setResultsPublishedBy(caller);
        round.setPublicationStatus("COMPLETED");
        roundRepository.save(round);

        List<RoundCandidate> candidates = roundCandidateRepository.findWithDetailsByRoundId(roundId);

        // Check if this was the last round in the job profile
        List<InterviewRound> allRounds = roundRepository.findByJobProfileIdOrderBySequenceOrderAsc(round.getJobProfile().getId());
        boolean isFinalRound = allRounds.stream().allMatch(r -> r.getSequenceOrder() <= round.getSequenceOrder());

        for (RoundCandidate c : candidates) {
            c.setIsPublished(true);
            c.setPublishedAt(Instant.now());
            c.setPublishedBy(caller);
            roundCandidateRepository.save(c);

            JobApplication app = c.getApplication();
            String oldAppStatus = app.getApplicationStatus();

            if ("ADVANCED".equalsIgnoreCase(c.getCandidateRoundStatus())) {
                if (isFinalRound) {
                    app.setApplicationStatus("SELECTED");
                    app.setStatusUpdatedAt(Instant.now());
                    app.setStatusUpdatedBy(caller);
                    app.setStatusChangeReason("Selected in final recruitment round: " + round.getRoundName());
                    applicationRepository.save(app);

                    statusHistoryRepository.save(CandidateStatusHistory.builder()
                            .application(app)
                            .round(round)
                            .fromStatus(oldAppStatus)
                            .toStatus("SELECTED")
                            .changedBy(caller)
                            .remarks("Final Selection in " + round.getRoundName())
                            .build());
                }
            } else if ("ELIMINATED".equalsIgnoreCase(c.getCandidateRoundStatus())) {
                app.setApplicationStatus("REJECTED");
                app.setStatusUpdatedAt(Instant.now());
                app.setStatusUpdatedBy(caller);
                app.setStatusChangeReason("Eliminated in round: " + round.getRoundName());
                applicationRepository.save(app);

                statusHistoryRepository.save(CandidateStatusHistory.builder()
                        .application(app)
                        .round(round)
                        .fromStatus(oldAppStatus)
                        .toStatus("REJECTED")
                        .changedBy(caller)
                        .remarks("Eliminated in " + round.getRoundName())
                        .build());
            }
        }

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "ROUND_RESULTS_PUBLISHED",
                "InterviewRound",
                roundId.toString(),
                Map.of("roundName", round.getRoundName(), "candidatesCount", candidates.size(), "isFinalRound", isFinalRound),
                "127.0.0.1",
                "Web Portal API"
        );

        return InterviewRoundDto.fromEntity(round);
    }

    @Transactional(readOnly = true)
    public List<CandidateTimelineDto> getApplicationTimeline(UUID applicationId, User caller) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found with id: " + applicationId));

        // If caller is student, verify ownership
        boolean isStudent = caller.getRoles().stream().anyMatch(r -> "STUDENT".equalsIgnoreCase(r.getRoleName()));
        if (isStudent) {
            Student student = studentRepository.findByUserId(caller.getId())
                    .or(() -> studentRepository.findByUniversityEmailIgnoreCase(caller.getEmail()))
                    .orElseThrow(() -> new ForbiddenException("Access denied: Not associated with student profile"));

            if (!app.getStudent().getId().equals(student.getId())) {
                throw new ForbiddenException("Access denied: You may only view your own application timeline");
            }
        }

        return statusHistoryRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId).stream()
                .map(CandidateTimelineDto::fromEntity)
                .collect(Collectors.toList());
    }
}
