package edu.jecrc.portal.modules.academic.ops.service;

import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.ops.dto.EventRegistrationDto;
import edu.jecrc.portal.modules.academic.ops.dto.PortalEventCreateRequest;
import edu.jecrc.portal.modules.academic.ops.dto.PortalEventDto;
import edu.jecrc.portal.modules.academic.ops.entity.EventRegistration;
import edu.jecrc.portal.modules.academic.ops.entity.PortalEvent;
import edu.jecrc.portal.modules.academic.ops.repository.EventRegistrationRepository;
import edu.jecrc.portal.modules.academic.ops.repository.PortalEventRepository;
import edu.jecrc.portal.modules.academic.repository.AcademicBatchRepository;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.user.entity.Department;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalEventService {

    private final PortalEventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicBatchRepository batchRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Transactional
    public PortalEventDto createEvent(PortalEventCreateRequest req, User caller) {
        if (!req.startTime().isBefore(req.endTime())) {
            throw ApiException.badRequest("Event start time must be before end time");
        }

        Department dept = null;
        if (req.departmentId() != null) {
            dept = departmentRepository.findById(req.departmentId()).orElse(null);
        }

        AcademicBatch batch = null;
        if (req.batchId() != null) {
            batch = batchRepository.findById(req.batchId()).orElse(null);
        }

        PortalEvent event = PortalEvent.builder()
                .eventTitle(req.eventTitle().trim())
                .eventType(req.eventType().toUpperCase().trim())
                .description(req.description())
                .eventDate(req.eventDate())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .venue(req.venue())
                .onlineMeetingUrl(req.onlineMeetingUrl())
                .targetAudience(req.targetAudience() != null ? req.targetAudience().toUpperCase().trim() : "ALL")
                .department(dept)
                .batch(batch)
                .registrationDeadline(req.registrationDeadline())
                .maxParticipants(req.maxParticipants())
                .organizerName(req.organizerName())
                .status("DRAFT")
                .build();

        PortalEvent saved = eventRepository.save(event);

        auditService.logAsync(
                caller != null ? caller.getId() : null,
                caller != null ? caller.getEmail() : "SYSTEM",
                "PORTAL_EVENT_CREATE",
                "PORTAL_EVENT",
                saved.getId().toString(),
                Map.of("title", saved.getEventTitle(), "type", saved.getEventType()),
                "127.0.0.1", "API"
        );

        return PortalEventDto.from(saved, 0);
    }

    @Transactional
    public PortalEventDto publishEvent(UUID eventId, User caller) {
        PortalEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        event.setStatus("PUBLISHED");
        event.setPublishedAt(Instant.now());
        event.setPublishedBy(caller);

        PortalEvent saved = eventRepository.save(event);

        auditService.logAsync(
                caller != null ? caller.getId() : null,
                caller != null ? caller.getEmail() : "SYSTEM",
                "PORTAL_EVENT_PUBLISH",
                "PORTAL_EVENT",
                saved.getId().toString(),
                Map.of("status", "PUBLISHED"),
                "127.0.0.1", "API"
        );

        return PortalEventDto.from(saved, registrationRepository.countByEventId(eventId));
    }

    @Transactional
    public PortalEventDto cancelEvent(UUID eventId, User caller) {
        PortalEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        event.setStatus("CANCELLED");
        PortalEvent saved = eventRepository.save(event);
        return PortalEventDto.from(saved, registrationRepository.countByEventId(eventId));
    }

    @Transactional(readOnly = true)
    public Page<PortalEventDto> listEvents(String status, String eventType, LocalDate fromDate, Pageable pageable) {
        return eventRepository.findFiltered(status, eventType, fromDate, pageable)
                .map(e -> PortalEventDto.from(e, registrationRepository.countByEventId(e.getId())));
    }

    @Transactional
    public EventRegistrationDto registerStudent(UUID eventId, UUID studentId, User caller) {
        PortalEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        if (!"PUBLISHED".equalsIgnoreCase(event.getStatus())) {
            throw ApiException.badRequest("Cannot register for an unpublished or cancelled event");
        }

        if (event.getRegistrationDeadline() != null && Instant.now().isAfter(event.getRegistrationDeadline())) {
            throw ApiException.badRequest("Registration deadline has passed for this event");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        if (registrationRepository.findByEventIdAndStudentId(eventId, studentId).isPresent()) {
            throw ApiException.conflict("Student is already registered for this event");
        }

        if (event.getMaxParticipants() != null) {
            long currentCount = registrationRepository.countByEventId(eventId);
            if (currentCount >= event.getMaxParticipants()) {
                throw ApiException.conflict("Event registration is at full capacity (" + event.getMaxParticipants() + ")");
            }
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .student(student)
                .registeredAt(Instant.now())
                .attendanceStatus("REGISTERED")
                .build();

        EventRegistration saved = registrationRepository.save(registration);
        return EventRegistrationDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<EventRegistrationDto> listEventRegistrations(UUID eventId) {
        return registrationRepository.findByEventId(eventId).stream()
                .map(EventRegistrationDto::from)
                .toList();
    }
}
