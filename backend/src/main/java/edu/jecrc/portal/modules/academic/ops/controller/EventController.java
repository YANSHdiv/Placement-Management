package edu.jecrc.portal.modules.academic.ops.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.ops.dto.EventRegistrationDto;
import edu.jecrc.portal.modules.academic.ops.dto.PortalEventCreateRequest;
import edu.jecrc.portal.modules.academic.ops.dto.PortalEventDto;
import edu.jecrc.portal.modules.academic.ops.service.PortalEventService;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events & Announcements", description = "Workshops, seminars, pre-placement talks, and student registrations")
public class EventController {

    private final PortalEventService eventService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    private User getCallerUser(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EVENT_MANAGE')")
    @Operation(summary = "Draft a new campus or academic event")
    public ResponseEntity<ApiResponse<PortalEventDto>> createEvent(
            @Valid @RequestBody PortalEventCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Event created successfully",
                eventService.createEvent(req, getCallerUser(auth))));
    }

    @PutMapping("/{eventId}/publish")
    @PreAuthorize("hasAuthority('EVENT_PUBLISH')")
    @Operation(summary = "Officially publish event to targeted audience")
    public ResponseEntity<ApiResponse<PortalEventDto>> publishEvent(
            @PathVariable UUID eventId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Event published successfully",
                eventService.publishEvent(eventId, getCallerUser(auth))));
    }

    @PutMapping("/{eventId}/cancel")
    @PreAuthorize("hasAuthority('EVENT_MANAGE')")
    @Operation(summary = "Cancel an event")
    public ResponseEntity<ApiResponse<PortalEventDto>> cancelEvent(
            @PathVariable UUID eventId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Event cancelled",
                eventService.cancelEvent(eventId, getCallerUser(auth))));
    }

    @GetMapping
    @Operation(summary = "List events and announcements")
    public ResponseEntity<PageResponse<PortalEventDto>> listEvents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) LocalDate fromDate,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<PortalEventDto> page = eventService.listEvents(status, eventType, fromDate, pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    @PostMapping("/{eventId}/register")
    @Operation(summary = "Register student for an event")
    public ResponseEntity<ApiResponse<EventRegistrationDto>> register(
            @PathVariable UUID eventId,
            @RequestParam(required = false) UUID studentId,
            Authentication auth
    ) {
        User user = getCallerUser(auth);
        UUID targetStudentId = studentId;
        if (targetStudentId == null) {
            Student student = studentRepository.findByUserId(user.getId())
                    .orElseThrow(() -> ApiException.notFound("Student record not linked to user: " + user.getEmail()));
            targetStudentId = student.getId();
        }
        return ResponseEntity.ok(ApiResponse.success("Registered for event",
                eventService.registerStudent(eventId, targetStudentId, user)));
    }

    @GetMapping("/{eventId}/registrations")
    @Operation(summary = "List student registrations for an event")
    public ResponseEntity<ApiResponse<List<EventRegistrationDto>>> listRegistrations(@PathVariable UUID eventId) {
        return ResponseEntity.ok(ApiResponse.success("Event registrations retrieved",
                eventService.listEventRegistrations(eventId)));
    }
}
