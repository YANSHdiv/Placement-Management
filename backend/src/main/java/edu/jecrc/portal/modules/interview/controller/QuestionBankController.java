package edu.jecrc.portal.modules.interview.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.interview.dto.InterviewQuestionCreateRequest;
import edu.jecrc.portal.modules.interview.dto.InterviewQuestionDto;
import edu.jecrc.portal.modules.interview.dto.InterviewQuestionUpdateRequest;
import edu.jecrc.portal.modules.interview.service.QuestionBankService;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interview-questions")
@RequiredArgsConstructor
@Tag(name = "Question Bank", description = "Interview questions repository, categorization & evaluator guidance")
public class QuestionBankController {

    private final QuestionBankService questionBankService;
    private final UserRepository userRepository;

    private User getCallerUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByEmailIgnoreCase(auth.getName()).orElse(null);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('QUESTION_BANK_MANAGE', 'DRIVE_VIEW', 'ROUND_CANDIDATE_VIEW')")
    @Operation(summary = "Search interview questions with category, difficulty, company filters")
    public ResponseEntity<ApiResponse<PageResponse<InterviewQuestionDto>>> searchQuestions(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) Boolean isArchived,
            @PageableDefault(size = 30) Pageable pageable,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                questionBankService.searchQuestions(query, category, difficulty, questionType, companyId, isArchived, pageable, getCallerUser(auth))
        ));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('QUESTION_BANK_MANAGE')")
    @Operation(summary = "Create an interview question in the question bank")
    public ResponseEntity<ApiResponse<InterviewQuestionDto>> createQuestion(
            @Valid @RequestBody InterviewQuestionCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Question created", questionBankService.createQuestion(req, getCallerUser(auth))));
    }

    @PutMapping("/{questionId}")
    @PreAuthorize("hasAuthority('QUESTION_BANK_MANAGE')")
    @Operation(summary = "Update an interview question")
    public ResponseEntity<ApiResponse<InterviewQuestionDto>> updateQuestion(
            @PathVariable UUID questionId,
            @Valid @RequestBody InterviewQuestionUpdateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Question updated", questionBankService.updateQuestion(questionId, req, getCallerUser(auth))));
    }
}
