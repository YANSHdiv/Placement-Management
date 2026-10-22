package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record NotificationBroadcastRequest(
        UUID recipientUserId,
        String targetAudienceType, // INDIVIDUAL, ALL_STUDENTS, ALL_FACULTY, BATCH, DEPARTMENT
        String targetAudienceId,

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Message is required")
        String message,

        String category, // ACADEMIC, TIMETABLE, ATTENDANCE, RECRUITMENT_DRIVE, INTERVIEW, OFFER, CRT, EVENT, GENERAL
        String relatedEntityType,
        String relatedEntityId,
        String actionUrl
) {}
