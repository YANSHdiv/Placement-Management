package edu.jecrc.portal.modules.interview.service;

import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.interview.dto.InterviewQuestionCreateRequest;
import edu.jecrc.portal.modules.interview.dto.InterviewQuestionDto;
import edu.jecrc.portal.modules.interview.dto.InterviewQuestionUpdateRequest;
import edu.jecrc.portal.modules.interview.entity.InterviewQuestion;
import edu.jecrc.portal.modules.interview.repository.InterviewQuestionRepository;
import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.entity.JobProfile;
import edu.jecrc.portal.modules.placement.repository.CompanyRepository;
import edu.jecrc.portal.modules.placement.repository.JobProfileRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionBankService {

    private final InterviewQuestionRepository questionRepository;
    private final CompanyRepository companyRepository;
    private final JobProfileRepository jobProfileRepository;
    private final AuditService auditService;

    @Transactional
    public InterviewQuestionDto createQuestion(InterviewQuestionCreateRequest req, User caller) {
        Company company = null;
        if (req.getCompanyId() != null) {
            company = companyRepository.findById(req.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + req.getCompanyId()));
        }

        JobProfile jobProfile = null;
        if (req.getJobProfileId() != null) {
            jobProfile = jobProfileRepository.findById(req.getJobProfileId())
                    .orElseThrow(() -> new ResourceNotFoundException("Job profile not found with id: " + req.getJobProfileId()));
        }

        InterviewQuestion question = InterviewQuestion.builder()
                .company(company)
                .jobProfile(jobProfile)
                .roundType(req.getRoundType())
                .title(req.getTitle().trim())
                .questionText(req.getQuestionText().trim())
                .questionType(req.getQuestionType().trim().toUpperCase())
                .category(req.getCategory().trim())
                .difficulty(req.getDifficulty() != null ? req.getDifficulty().trim().toUpperCase() : "MEDIUM")
                .tags(req.getTags())
                .expectedAnswerGuidance(req.getExpectedAnswerGuidance())
                .isConfidential(Boolean.TRUE.equals(req.getIsConfidential()))
                .isArchived(false)
                .createdBy(caller)
                .build();

        InterviewQuestion saved = questionRepository.save(question);

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "INTERVIEW_QUESTION_CREATED",
                "InterviewQuestion",
                saved.getId().toString(),
                Map.of("title", saved.getTitle(), "category", saved.getCategory()),
                "127.0.0.1",
                "Web Portal API"
        );

        return InterviewQuestionDto.fromEntity(saved, true);
    }

    @Transactional
    public InterviewQuestionDto updateQuestion(UUID questionId, InterviewQuestionUpdateRequest req, User caller) {
        InterviewQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview question not found with id: " + questionId));

        if (req.getTitle() != null && !req.getTitle().isBlank()) question.setTitle(req.getTitle().trim());
        if (req.getQuestionText() != null && !req.getQuestionText().isBlank()) question.setQuestionText(req.getQuestionText().trim());
        if (req.getQuestionType() != null) question.setQuestionType(req.getQuestionType().trim().toUpperCase());
        if (req.getCategory() != null) question.setCategory(req.getCategory().trim());
        if (req.getDifficulty() != null) question.setDifficulty(req.getDifficulty().trim().toUpperCase());
        if (req.getTags() != null) question.setTags(req.getTags());
        if (req.getExpectedAnswerGuidance() != null) question.setExpectedAnswerGuidance(req.getExpectedAnswerGuidance());
        if (req.getIsConfidential() != null) question.setIsConfidential(req.getIsConfidential());
        if (req.getIsArchived() != null) question.setIsArchived(req.getIsArchived());
        if (req.getRoundType() != null) question.setRoundType(req.getRoundType());

        if (req.getCompanyId() != null) {
            Company company = companyRepository.findById(req.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + req.getCompanyId()));
            question.setCompany(company);
        }

        InterviewQuestion updated = questionRepository.save(question);

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "INTERVIEW_QUESTION_UPDATED",
                "InterviewQuestion",
                updated.getId().toString(),
                Map.of("title", updated.getTitle()),
                "127.0.0.1",
                "Web Portal API"
        );

        return InterviewQuestionDto.fromEntity(updated, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<InterviewQuestionDto> searchQuestions(
            String query, String category, String difficulty, String questionType, UUID companyId, Boolean isArchived,
            Pageable pageable, User caller
    ) {
        boolean isStaff = caller.getRoles().stream().anyMatch(r ->
                List.of("SUPER_ADMIN", "TPO_ADMIN", "TPO_COORDINATOR", "FACULTY").contains(r.getRoleName()));

        Page<InterviewQuestion> page = questionRepository.searchQuestions(
                query, category, difficulty, questionType, companyId, isArchived != null ? isArchived : false, pageable
        );

        return PageResponse.from(page.map(q -> InterviewQuestionDto.fromEntity(q, isStaff)));
    }
}
