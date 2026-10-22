package edu.jecrc.portal.modules.academic.ops.service;

import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import edu.jecrc.portal.modules.academic.entity.AcademicProgram;
import edu.jecrc.portal.modules.academic.ops.dto.*;
import edu.jecrc.portal.modules.academic.ops.entity.*;
import edu.jecrc.portal.modules.academic.ops.repository.*;
import edu.jecrc.portal.modules.academic.repository.AcademicBatchRepository;
import edu.jecrc.portal.modules.academic.repository.AcademicProgramRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.user.entity.Department;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.DepartmentRepository;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyProfileRepository facultyProfileRepository;
    private final FacultyTeachingAssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final AcademicSubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicBatchRepository batchRepository;
    private final AcademicProgramRepository programRepository;
    private final AuditService auditService;

    @Transactional
    public FacultyProfileDto createFacultyProfile(FacultyProfileCreateRequest req, User caller) {
        if (facultyProfileRepository.findByFacultyCodeIgnoreCase(req.facultyCode()).isPresent()) {
            throw ApiException.conflict("Faculty profile with code already exists: " + req.facultyCode());
        }

        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.userId()));

        if (facultyProfileRepository.findByUserId(req.userId()).isPresent()) {
            throw ApiException.conflict("Faculty profile already exists for user: " + user.getEmail());
        }

        Department dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + req.departmentId()));

        FacultyProfile profile = FacultyProfile.builder()
                .user(user)
                .facultyCode(req.facultyCode().toUpperCase().trim())
                .department(dept)
                .designation(req.designation())
                .specialization(req.specialization())
                .isActive(true)
                .build();

        FacultyProfile saved = facultyProfileRepository.save(profile);

        auditService.logAsync(
                caller != null ? caller.getId() : null,
                caller != null ? caller.getEmail() : "SYSTEM",
                "FACULTY_PROFILE_CREATE",
                "FACULTY_PROFILE",
                saved.getId().toString(),
                Map.of("facultyCode", saved.getFacultyCode(), "department", dept.getDepartmentCode()),
                "127.0.0.1", "API"
        );

        return FacultyProfileDto.from(saved);
    }

    @Transactional(readOnly = true)
    public FacultyProfileDto getFacultyProfile(UUID facultyId) {
        return facultyProfileRepository.findById(facultyId)
                .map(FacultyProfileDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found: " + facultyId));
    }

    @Transactional(readOnly = true)
    public FacultyProfileDto getFacultyProfileByUserId(UUID userId) {
        return facultyProfileRepository.findByUserId(userId)
                .map(FacultyProfileDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user: " + userId));
    }

    @Transactional(readOnly = true)
    public List<FacultyProfileDto> listFacultyProfiles(Integer departmentId) {
        List<FacultyProfile> profiles = departmentId != null
                ? facultyProfileRepository.findByDepartmentIdAndIsActiveTrue(departmentId)
                : facultyProfileRepository.findByIsActiveTrue();
        return profiles.stream().map(FacultyProfileDto::from).toList();
    }

    @Transactional
    public FacultyTeachingAssignmentDto createTeachingAssignment(FacultyTeachingAssignmentCreateRequest req, User caller) {
        FacultyProfile faculty = facultyProfileRepository.findById(req.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + req.facultyId()));

        AcademicSubject subject = subjectRepository.findById(req.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic subject not found: " + req.subjectId()));

        AcademicBatch batch = batchRepository.findById(req.batchId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic batch not found: " + req.batchId()));

        String year = (req.academicYear() != null && !req.academicYear().isBlank()) ? req.academicYear() : "2025-2026";

        if (assignmentRepository.findByFacultyIdAndSubjectIdAndBatchIdAndSectionAndSemesterAndAcademicYear(
                faculty.getId(), subject.getId(), batch.getId(), req.section().toUpperCase().trim(), req.semester(), year).isPresent()) {
            throw ApiException.conflict("Teaching assignment already exists for this faculty and cohort");
        }

        FacultyTeachingAssignment assignment = FacultyTeachingAssignment.builder()
                .faculty(faculty)
                .subject(subject)
                .batch(batch)
                .section(req.section().toUpperCase().trim())
                .semester(req.semester())
                .academicYear(year)
                .isActive(true)
                .build();

        FacultyTeachingAssignment saved = assignmentRepository.save(assignment);

        auditService.logAsync(
                caller != null ? caller.getId() : null,
                caller != null ? caller.getEmail() : "SYSTEM",
                "FACULTY_TEACHING_ASSIGNMENT_CREATE",
                "TEACHING_ASSIGNMENT",
                saved.getId().toString(),
                Map.of("faculty", faculty.getFacultyCode(), "subject", subject.getSubjectCode(), "section", saved.getSection()),
                "127.0.0.1", "API"
        );

        return FacultyTeachingAssignmentDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<FacultyTeachingAssignmentDto> listTeachingAssignments(UUID facultyId, Integer batchId, Integer semester, String section) {
        if (facultyId != null) {
            return assignmentRepository.findByFacultyIdAndIsActiveTrue(facultyId).stream()
                    .map(FacultyTeachingAssignmentDto::from)
                    .toList();
        } else if (batchId != null && semester != null && section != null) {
            return assignmentRepository.findByBatchIdAndSemesterAndSectionAndIsActiveTrue(batchId, semester, section).stream()
                    .map(FacultyTeachingAssignmentDto::from)
                    .toList();
        }
        return assignmentRepository.findAll().stream().map(FacultyTeachingAssignmentDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AcademicSubjectDto> listAcademicSubjects(Integer departmentId, Integer semester) {
        if (departmentId != null) {
            return subjectRepository.findByDepartmentIdAndIsActiveTrue(departmentId).stream()
                    .map(AcademicSubjectDto::from).toList();
        }
        if (semester != null) {
            return subjectRepository.findBySemesterAndIsActiveTrue(semester).stream()
                    .map(AcademicSubjectDto::from).toList();
        }
        return subjectRepository.findByIsActiveTrueOrderBySubjectCodeAsc().stream()
                .map(AcademicSubjectDto::from).toList();
    }

    @Transactional
    public AcademicSubjectDto createAcademicSubject(AcademicSubjectCreateRequest req, User caller) {
        if (subjectRepository.findBySubjectCodeIgnoreCase(req.subjectCode()).isPresent()) {
            throw ApiException.conflict("Subject with code already exists: " + req.subjectCode());
        }

        Department dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + req.departmentId()));

        AcademicProgram prog = null;
        if (req.programId() != null) {
            prog = programRepository.findById(req.programId()).orElse(null);
        }

        AcademicSubject subject = AcademicSubject.builder()
                .department(dept)
                .program(prog)
                .subjectCode(req.subjectCode().toUpperCase().trim())
                .subjectName(req.subjectName().trim())
                .credits(req.credits())
                .semester(req.semester())
                .isActive(true)
                .build();

        AcademicSubject saved = subjectRepository.save(subject);
        return AcademicSubjectDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ClassroomDto> listClassrooms() {
        return classroomRepository.findByIsActiveTrueOrderByRoomNumberAsc().stream()
                .map(ClassroomDto::from).toList();
    }

    @Transactional
    public ClassroomDto createClassroom(ClassroomCreateRequest req, User caller) {
        if (classroomRepository.findByRoomNumberIgnoreCase(req.roomNumber()).isPresent()) {
            throw ApiException.conflict("Classroom already exists: " + req.roomNumber());
        }

        Classroom room = Classroom.builder()
                .roomNumber(req.roomNumber().toUpperCase().trim())
                .buildingName(req.buildingName().trim())
                .capacity(req.capacity())
                .roomType(req.roomType() != null ? req.roomType() : "LECTURE_HALL")
                .latitude(req.latitude())
                .longitude(req.longitude())
                .geofenceRadiusMeters(req.geofenceRadiusMeters() != null ? req.geofenceRadiusMeters() : 60)
                .isActive(true)
                .build();

        Classroom saved = classroomRepository.save(room);
        return ClassroomDto.from(saved);
    }
}
