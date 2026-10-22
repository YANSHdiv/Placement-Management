package edu.jecrc.portal.modules.interview.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.interview.dto.InterviewFeedbackCreateRequest;
import edu.jecrc.portal.modules.interview.dto.InterviewFeedbackDto;
import edu.jecrc.portal.modules.interview.service.InterviewFeedbackService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Interview Feedback", description = "Student, evaluator and TPO recruitment feedback workflows")
public class InterviewFeedbackController {

    private final InterviewFeedbackService feedbackService;
    private final UserRepository userRepository;

    private User getCallerUser(Authentication auth) {
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @PostMapping("/interview-feedback")
    @PreAuthorize("hasAuthority('INTERVIEW_FEEDBACK_SUBMIT')")
    @Operation(summary = "Submit recruitment feedback")
    public ResponseEntity<ApiResponse<InterviewFeedbackDto>> submitFeedback(
            @Valid @RequestBody InterviewFeedbackCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Feedback submitted successfully", feedbackService.submitFeedback(req, getCallerUser(auth))));
    }

    @GetMapping("/applications/{applicationId}/feedback")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get feedback entries associated with a job application")
    public ResponseEntity<ApiResponse<List<InterviewFeedbackDto>>> getFeedbackForApplication(
            @PathVariable UUID applicationId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success(feedbackService.getFeedbackForApplication(applicationId, getCallerUser(auth))));
    }

    @GetMapping("/interview-feedback")
    @PreAuthorize("hasAuthority('INTERVIEW_FEEDBACK_VIEW')")
    @Operation(summary = "Search and view aggregate recruitment feedback entries")
    public ResponseEntity<ApiResponse<PageResponse<InterviewFeedbackDto>>> searchFeedback(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) String feedbackType,
            @RequestParam(required = false) String moderationStatus,
            @PageableDefault(size = 30) Pageable pageable,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success(feedbackService.searchFeedback(companyId, feedbackType, moderationStatus, pageable, getCallerUser(auth))));
    }
}
