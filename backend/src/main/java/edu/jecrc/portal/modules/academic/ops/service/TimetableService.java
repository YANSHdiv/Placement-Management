package edu.jecrc.portal.modules.academic.ops.service;

import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import edu.jecrc.portal.modules.academic.ops.dto.TimetableSlotCreateRequest;
import edu.jecrc.portal.modules.academic.ops.dto.TimetableSlotDto;
import edu.jecrc.portal.modules.academic.ops.dto.TimetableSlotUpdateRequest;
import edu.jecrc.portal.modules.academic.ops.entity.AcademicSubject;
import edu.jecrc.portal.modules.academic.ops.entity.Classroom;
import edu.jecrc.portal.modules.academic.ops.entity.FacultyProfile;
import edu.jecrc.portal.modules.academic.ops.entity.TimetableSlot;
import edu.jecrc.portal.modules.academic.ops.repository.AcademicSubjectRepository;
import edu.jecrc.portal.modules.academic.ops.repository.ClassroomRepository;
import edu.jecrc.portal.modules.academic.ops.repository.FacultyProfileRepository;
import edu.jecrc.portal.modules.academic.ops.repository.TimetableSlotRepository;
import edu.jecrc.portal.modules.academic.repository.AcademicBatchRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableSlotRepository timetableSlotRepository;
    private final AcademicBatchRepository batchRepository;
    private final AcademicSubjectRepository subjectRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final ClassroomRepository classroomRepository;
    private final AuditService auditService;

    @Transactional
    public TimetableSlotDto createSlot(TimetableSlotCreateRequest req, User caller) {
        if (!req.startTime().isBefore(req.endTime())) {
            throw ApiException.badRequest("Slot start time must be strictly before end time");
        }

        AcademicBatch batch = batchRepository.findById(req.batchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + req.batchId()));

        AcademicSubject subject = subjectRepository.findById(req.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic subject not found: " + req.subjectId()));

        FacultyProfile faculty = facultyProfileRepository.findById(req.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + req.facultyId()));

        Classroom classroom = classroomRepository.findById(req.classroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found: " + req.classroomId()));

        String year = (req.academicYear() != null && !req.academicYear().isBlank()) ? req.academicYear() : "2025-2026";
        String status = (req.status() != null && !req.status().isBlank()) ? req.status().toUpperCase() : "PUBLISHED";
        String day = req.dayOfWeek().toUpperCase().trim();

        TimetableSlot slot = TimetableSlot.builder()
                .batch(batch)
                .semester(req.semester())
                .section(req.section().toUpperCase().trim())
                .subject(subject)
                .faculty(faculty)
                .classroom(classroom)
                .dayOfWeek(day)
                .startTime(req.startTime())
                .endTime(req.endTime())
                .academicYear(year)
                .status(status)
                .isActive(true)
                .createdBy(caller)
                .build();

        try {
            TimetableSlot saved = timetableSlotRepository.saveAndFlush(slot);

            auditService.logAsync(
                    caller != null ? caller.getId() : null,
                    caller != null ? caller.getEmail() : "SYSTEM",
                    "TIMETABLE_SLOT_CREATE",
                    "TIMETABLE_SLOT",
                    saved.getId().toString(),
                    Map.of(
                            "classroom", classroom.getRoomNumber(),
                            "faculty", faculty.getFacultyCode(),
                            "cohort", batch.getBatchName() + " Sem " + req.semester() + " Sec " + req.section(),
                            "day", day,
                            "interval", req.startTime() + " - " + req.endTime()
                    ),
                    "127.0.0.1", "API"
            );

            return TimetableSlotDto.from(saved);
        } catch (DataIntegrityViolationException ex) {
            String errorMsg = ex.getMessage() != null ? ex.getMessage() : "";
            if (errorMsg.contains("excl_no_classroom_overlap")) {
                throw ApiException.conflict("Classroom conflict: Room " + classroom.getRoomNumber() +
                        " is already reserved for an overlapping session on " + day + " (" + req.startTime() + " - " + req.endTime() + ")");
            } else if (errorMsg.contains("excl_no_faculty_overlap")) {
                throw ApiException.conflict("Faculty conflict: " + faculty.getFacultyCode() +
                        " is already assigned to another session during " + day + " (" + req.startTime() + " - " + req.endTime() + ")");
            } else if (errorMsg.contains("excl_no_section_overlap")) {
                throw ApiException.conflict("Student section conflict: Batch " + batch.getBatchName() +
                        " Sem " + req.semester() + " Sec " + req.section() +
                        " already has a scheduled class during " + day + " (" + req.startTime() + " - " + req.endTime() + ")");
            }
            throw ApiException.conflict("Timetable conflict detected: Overlapping slot prevents scheduling (" + ex.getRootCause() + ")");
        }
    }

    @Transactional
    public TimetableSlotDto updateSlot(UUID slotId, TimetableSlotUpdateRequest req, User caller) {
        TimetableSlot slot = timetableSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable slot not found: " + slotId));

        if (req.classroomId() != null) {
            Classroom room = classroomRepository.findById(req.classroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classroom not found: " + req.classroomId()));
            slot.setClassroom(room);
        }
        if (req.facultyId() != null) {
            FacultyProfile fac = facultyProfileRepository.findById(req.facultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + req.facultyId()));
            slot.setFaculty(fac);
        }
        if (req.subjectId() != null) {
            AcademicSubject sub = subjectRepository.findById(req.subjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + req.subjectId()));
            slot.setSubject(sub);
        }
        if (req.dayOfWeek() != null && !req.dayOfWeek().isBlank()) {
            slot.setDayOfWeek(req.dayOfWeek().toUpperCase().trim());
        }
        if (req.startTime() != null) {
            slot.setStartTime(req.startTime());
        }
        if (req.endTime() != null) {
            slot.setEndTime(req.endTime());
        }
        if (slot.getStartTime().isAfter(slot.getEndTime()) || slot.getStartTime().equals(slot.getEndTime())) {
            throw ApiException.badRequest("Slot start time must be strictly before end time");
        }
        if (req.status() != null && !req.status().isBlank()) {
            slot.setStatus(req.status().toUpperCase().trim());
        }
        if (req.isActive() != null) {
            slot.setIsActive(req.isActive());
        }

        try {
            TimetableSlot updated = timetableSlotRepository.saveAndFlush(slot);
            return TimetableSlotDto.from(updated);
        } catch (DataIntegrityViolationException ex) {
            throw ApiException.conflict("Timetable update causes a scheduling conflict with another existing slot");
        }
    }

    @Transactional
    public void deleteSlot(UUID slotId, User caller) {
        TimetableSlot slot = timetableSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable slot not found: " + slotId));
        slot.setIsActive(false);
        slot.setStatus("CANCELLED");
        timetableSlotRepository.save(slot);
    }

    @Transactional(readOnly = true)
    public List<TimetableSlotDto> listCohortSlots(Integer batchId, Integer semester, String section, String academicYear) {
        String year = (academicYear != null && !academicYear.isBlank()) ? academicYear : "2025-2026";
        return timetableSlotRepository.findByBatchIdAndSemesterAndSectionAndAcademicYearAndIsActiveTrue(
                batchId, semester, section.toUpperCase().trim(), year
        ).stream().map(TimetableSlotDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TimetableSlotDto> listFacultySlots(UUID facultyId, String academicYear) {
        String year = (academicYear != null && !academicYear.isBlank()) ? academicYear : "2025-2026";
        return timetableSlotRepository.findByFacultyIdAndAcademicYearAndIsActiveTrue(facultyId, year)
                .stream().map(TimetableSlotDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TimetableSlotDto> listClassroomSlots(Integer classroomId, String academicYear) {
        String year = (academicYear != null && !academicYear.isBlank()) ? academicYear : "2025-2026";
        return timetableSlotRepository.findByClassroomIdAndAcademicYearAndIsActiveTrue(classroomId, year)
                .stream().map(TimetableSlotDto::from).toList();
    }
}
