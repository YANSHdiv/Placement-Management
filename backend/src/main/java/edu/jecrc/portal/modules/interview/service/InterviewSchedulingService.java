package edu.jecrc.portal.modules.interview.service;

import edu.jecrc.portal.common.exception.BadRequestException;
import edu.jecrc.portal.common.exception.ForbiddenException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.interview.dto.*;
import edu.jecrc.portal.modules.interview.entity.CandidateInterviewAppointment;
import edu.jecrc.portal.modules.interview.entity.RoundCandidate;
import edu.jecrc.portal.modules.interview.repository.CandidateInterviewAppointmentRepository;
import edu.jecrc.portal.modules.interview.repository.RoundCandidateRepository;
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
public class InterviewSchedulingService {

    private final CandidateInterviewAppointmentRepository appointmentRepository;
    private final RoundCandidateRepository candidateRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public ConflictCheckResponse checkConflicts(UUID studentId, UUID evaluatorId, Instant startTime, Instant endTime, UUID excludeAppointmentId) {
        if (!endTime.isAfter(startTime)) {
            return ConflictCheckResponse.builder()
                    .hasConflict(true)
                    .message("Invalid time range: End time must be after start time")
                    .build();
        }

        List<CandidateInterviewAppointment> studentConflicts = appointmentRepository
                .findConflictingStudentAppointments(studentId, startTime, endTime, excludeAppointmentId);

        List<CandidateInterviewAppointment> evaluatorConflicts = evaluatorId != null
                ? appointmentRepository.findConflictingEvaluatorAppointments(evaluatorId, startTime, endTime, excludeAppointmentId)
                : List.of();

        boolean hasConflict = !studentConflicts.isEmpty() || !evaluatorConflicts.isEmpty();
        StringBuilder msg = new StringBuilder();

        if (!studentConflicts.isEmpty()) {
            CandidateInterviewAppointment sc = studentConflicts.get(0);
            String compName = sc.getCandidate() != null && sc.getCandidate().getRound() != null
                    && sc.getCandidate().getRound().getJobProfile() != null && sc.getCandidate().getRound().getJobProfile().getDrive() != null
                    && sc.getCandidate().getRound().getJobProfile().getDrive().getCompany() != null
                    ? sc.getCandidate().getRound().getJobProfile().getDrive().getCompany().getCompanyName()
                    : "another drive";
            msg.append("Student conflict detected: The student already has an interview scheduled with ")
                    .append(compName)
                    .append(" from ")
                    .append(sc.getStartTime())
                    .append(" to ")
                    .append(sc.getEndTime());
        }

        if (!evaluatorConflicts.isEmpty()) {
            if (msg.length() > 0) msg.append(". ");
            CandidateInterviewAppointment ec = evaluatorConflicts.get(0);
            msg.append("Evaluator conflict detected: Assigned evaluator already has an interview appointment scheduled from ")
                    .append(ec.getStartTime())
                    .append(" to ")
                    .append(ec.getEndTime());
        }

        return ConflictCheckResponse.builder()
                .hasConflict(hasConflict)
                .message(hasConflict ? msg.toString() : "No scheduling conflicts detected")
                .studentConflicts(studentConflicts.stream().map(InterviewAppointmentDto::fromEntity).collect(Collectors.toList()))
                .evaluatorConflicts(evaluatorConflicts.stream().map(InterviewAppointmentDto::fromEntity).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public InterviewAppointmentDto scheduleAppointment(InterviewAppointmentCreateRequest req, User caller) {
        RoundCandidate candidate = candidateRepository.findWithDetailsById(req.getRoundCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Round candidate not found with id: " + req.getRoundCandidateId()));

        Student student = candidate.getApplication().getStudent();

        User evaluator = null;
        if (req.getEvaluatorUserId() != null) {
            evaluator = userRepository.findById(req.getEvaluatorUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Evaluator not found with id: " + req.getEvaluatorUserId()));
        }

        // Conflict check
        ConflictCheckResponse conflictCheck = checkConflicts(
                student.getId(),
                evaluator != null ? evaluator.getId() : null,
                req.getStartTime(),
                req.getEndTime(),
                null
        );

        if (conflictCheck.isHasConflict()) {
            throw new BadRequestException(conflictCheck.getMessage());
        }

        CandidateInterviewAppointment appointment = CandidateInterviewAppointment.builder()
                .candidate(candidate)
                .evaluatorUser(evaluator)
                .student(student)
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .venueOrMeetingLink(req.getVenueOrMeetingLink())
                .status("SCHEDULED")
                .createdBy(caller)
                .build();

        CandidateInterviewAppointment saved = appointmentRepository.save(appointment);

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "INTERVIEW_APPOINTMENT_SCHEDULED",
                "CandidateInterviewAppointment",
                saved.getId().toString(),
                Map.of("studentId", student.getId().toString(), "startTime", req.getStartTime().toString(), "endTime", req.getEndTime().toString()),
                "127.0.0.1",
                "Web Portal API"
        );

        return InterviewAppointmentDto.fromEntity(saved);
    }

    @Transactional
    public InterviewAppointmentDto updateAppointment(UUID appointmentId, InterviewAppointmentUpdateRequest req, User caller) {
        CandidateInterviewAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview appointment not found with id: " + appointmentId));

        Instant newStart = req.getStartTime() != null ? req.getStartTime() : appointment.getStartTime();
        Instant newEnd = req.getEndTime() != null ? req.getEndTime() : appointment.getEndTime();
        UUID newEvaluatorId = req.getEvaluatorUserId() != null ? req.getEvaluatorUserId()
                : (appointment.getEvaluatorUser() != null ? appointment.getEvaluatorUser().getId() : null);

        // Conflict check
        if (req.getStartTime() != null || req.getEndTime() != null || req.getEvaluatorUserId() != null) {
            ConflictCheckResponse conflictCheck = checkConflicts(
                    appointment.getStudent().getId(),
                    newEvaluatorId,
                    newStart,
                    newEnd,
                    appointmentId
            );

            if (conflictCheck.isHasConflict()) {
                throw new BadRequestException(conflictCheck.getMessage());
            }
        }

        if (req.getEvaluatorUserId() != null) {
            User newEval = userRepository.findById(req.getEvaluatorUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Evaluator not found with id: " + req.getEvaluatorUserId()));
            appointment.setEvaluatorUser(newEval);
        }

        if (req.getStartTime() != null) appointment.setStartTime(req.getStartTime());
        if (req.getEndTime() != null) appointment.setEndTime(req.getEndTime());
        if (req.getVenueOrMeetingLink() != null) appointment.setVenueOrMeetingLink(req.getVenueOrMeetingLink());
        if (req.getStatus() != null) appointment.setStatus(req.getStatus().trim().toUpperCase());
        if (req.getRescheduleReason() != null) appointment.setRescheduleReason(req.getRescheduleReason());

        CandidateInterviewAppointment updated = appointmentRepository.save(appointment);

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "INTERVIEW_APPOINTMENT_UPDATED",
                "CandidateInterviewAppointment",
                updated.getId().toString(),
                Map.of("status", updated.getStatus(), "rescheduleReason", updated.getRescheduleReason() != null ? updated.getRescheduleReason() : ""),
                "127.0.0.1",
                "Web Portal API"
        );

        return InterviewAppointmentDto.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public List<InterviewAppointmentDto> getMyAppointments(String userEmail) {
        Student student = studentRepository.findByUniversityEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ForbiddenException("Current user is not associated with an enrolled student record"));

        return appointmentRepository.findWithDetailsByStudentId(student.getId()).stream()
                .map(InterviewAppointmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterviewAppointmentDto> getAppointmentsForCandidate(UUID roundCandidateId) {
        return appointmentRepository.findByCandidateId(roundCandidateId).stream()
                .map(InterviewAppointmentDto::fromEntity)
                .collect(Collectors.toList());
    }
}
