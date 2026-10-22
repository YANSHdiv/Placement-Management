package edu.jecrc.portal.modules.interview.service;

import edu.jecrc.portal.common.exception.ForbiddenException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.interview.dto.InterviewFeedbackCreateRequest;
import edu.jecrc.portal.modules.interview.dto.InterviewFeedbackDto;
import edu.jecrc.portal.modules.interview.entity.InterviewFeedback;
import edu.jecrc.portal.modules.interview.entity.InterviewRound;
import edu.jecrc.portal.modules.interview.repository.InterviewFeedbackRepository;
import edu.jecrc.portal.modules.interview.repository.InterviewRoundRepository;
import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.entity.JobApplication;
import edu.jecrc.portal.modules.placement.repository.CompanyRepository;
import edu.jecrc.portal.modules.placement.repository.JobApplicationRepository;
import edu.jecrc.portal.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewFeedbackService {

    private final InterviewFeedbackRepository feedbackRepository;
    private final JobApplicationRepository applicationRepository;
    private final InterviewRoundRepository roundRepository;
    private final CompanyRepository companyRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Transactional
    public InterviewFeedbackDto submitFeedback(InterviewFeedbackCreateRequest req, User caller) {
        JobApplication application = null;
        Company company = null;
        InterviewRound round = null;

        if (req.getApplicationId() != null) {
            application = applicationRepository.findById(req.getApplicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + req.getApplicationId()));

            // If caller is student, verify application belongs to caller
            boolean isStudent = caller.getRoles().stream().anyMatch(r -> "STUDENT".equalsIgnoreCase(r.getRoleName()));
            if (isStudent) {
                Student student = studentRepository.findByUserId(caller.getId())
                        .or(() -> studentRepository.findByUniversityEmailIgnoreCase(caller.getEmail()))
                        .orElseThrow(() -> new ForbiddenException("Current user is not associated with an enrolled student record"));

                if (!application.getStudent().getId().equals(student.getId())) {
                    throw new ForbiddenException("Access denied: You can only submit feedback for your own job application");
                }
            }

            company = application.getJobProfile().getDrive().getCompany();
        }

        if (req.getRoundId() != null) {
            round = roundRepository.findById(req.getRoundId())
                    .orElseThrow(() -> new ResourceNotFoundException("Round not found with id: " + req.getRoundId()));
            if (company == null && round.getJobProfile() != null && round.getJobProfile().getDrive() != null) {
                company = round.getJobProfile().getDrive().getCompany();
            }
        }

        if (company == null && req.getCompanyId() != null) {
            company = companyRepository.findById(req.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + req.getCompanyId()));
        }

        InterviewFeedback feedback = InterviewFeedback.builder()
                .feedbackType(req.getFeedbackType().trim().toUpperCase())
                .application(application)
                .round(round)
                .company(company)
                .submittedBy(caller)
                .rating(req.getRating())
                .comments(req.getComments())
                .isConfidentialToStaff(Boolean.TRUE.equals(req.getIsConfidentialToStaff()))
                .moderationStatus("APPROVED")
                .build();

        InterviewFeedback saved = feedbackRepository.save(feedback);

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "INTERVIEW_FEEDBACK_SUBMITTED",
                "InterviewFeedback",
                saved.getId().toString(),
                Map.of("type", saved.getFeedbackType(), "rating", saved.getRating()),
                "127.0.0.1",
                "Web Portal API"
        );

        return InterviewFeedbackDto.fromEntity(saved, true);
    }

    @Transactional(readOnly = true)
    public List<InterviewFeedbackDto> getFeedbackForApplication(UUID applicationId, User caller) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        boolean isStaff = caller.getRoles().stream().anyMatch(r ->
                List.of("SUPER_ADMIN", "TPO_ADMIN", "TPO_COORDINATOR").contains(r.getRoleName()));

        boolean isStudent = caller.getRoles().stream().anyMatch(r -> "STUDENT".equalsIgnoreCase(r.getRoleName()));
        if (isStudent) {
            Student student = studentRepository.findByUserId(caller.getId())
                    .or(() -> studentRepository.findByUniversityEmailIgnoreCase(caller.getEmail()))
                    .orElseThrow(() -> new ForbiddenException("Current user is not associated with an enrolled student record"));

            if (!application.getStudent().getId().equals(student.getId())) {
                throw new ForbiddenException("Access denied: You may only view feedback relating to your own job application");
            }
        }

        return feedbackRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId).stream()
                .filter(f -> isStaff || !f.getIsConfidentialToStaff())
                .map(f -> InterviewFeedbackDto.fromEntity(f, isStaff))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<InterviewFeedbackDto> searchFeedback(UUID companyId, String feedbackType, String moderationStatus, Pageable pageable, User caller) {
        boolean isStaff = caller.getRoles().stream().anyMatch(r ->
                List.of("SUPER_ADMIN", "TPO_ADMIN", "TPO_COORDINATOR").contains(r.getRoleName()));

        Page<InterviewFeedback> page = feedbackRepository.searchFeedback(companyId, feedbackType, moderationStatus, pageable);
        return PageResponse.from(page.map(f -> InterviewFeedbackDto.fromEntity(f, isStaff)));
    }
}
