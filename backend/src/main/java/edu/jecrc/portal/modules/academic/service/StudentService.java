package edu.jecrc.portal.modules.academic.service;

import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.dto.*;
import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import edu.jecrc.portal.modules.academic.entity.AcademicProgram;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.AcademicBatchRepository;
import edu.jecrc.portal.modules.academic.repository.AcademicProgramRepository;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.user.entity.Department;
import edu.jecrc.portal.modules.user.entity.Role;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final AcademicProgramRepository programRepository;
    private final AcademicBatchRepository batchRepository;
    private final UserRepository userRepository;
    private final AcademicSecurityService academicSecurityService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<StudentSummaryDto> getStudents(
            Integer departmentId,
            Integer programId,
            Integer batchId,
            Integer semester,
            String verificationStatus,
            String search,
            User caller,
            Pageable pageable) {

        Set<String> roles = caller.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet());

        // Students can only see themselves
        if (roles.contains("STUDENT")) {
            Student own = studentRepository.findByUserId(caller.getId())
                    .or(() -> studentRepository.findByUniversityEmail(caller.getEmail()))
                    .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + caller.getEmail()));
            return PageResponse.from(new org.springframework.data.domain.PageImpl<>(
                    List.of(StudentSummaryDto.fromEntity(own)), pageable, 1));
        }

        List<Integer> scopedDeptIds = null;
        if (roles.contains("HOD") || roles.contains("FACULTY")) {
            scopedDeptIds = academicSecurityService.getScopedDepartmentIds(caller.getId());
            if (scopedDeptIds.isEmpty()) {
                return PageResponse.from(Page.empty(pageable));
            }
        }

        final List<Integer> finalScopedDeptIds = scopedDeptIds;

        Specification<Student> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Department scoping for HOD/Faculty
            if (finalScopedDeptIds != null) {
                predicates.add(root.get("department").get("id").in(finalScopedDeptIds));
            } else if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            if (programId != null) {
                predicates.add(cb.equal(root.get("program").get("id"), programId));
            }
            if (batchId != null) {
                predicates.add(cb.equal(root.get("batch").get("id"), batchId));
            }
            if (semester != null) {
                predicates.add(cb.equal(root.get("currentSemester"), semester));
            }
            if (verificationStatus != null && !verificationStatus.isBlank()) {
                predicates.add(cb.equal(root.get("verificationStatus"), verificationStatus.trim().toUpperCase()));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern)
                );
                Predicate idMatch = cb.or(
                        cb.like(cb.lower(root.get("rollNumber")), pattern),
                        cb.like(cb.lower(root.get("universityStudentId")), pattern),
                        cb.like(cb.lower(root.get("universityEmail")), pattern)
                );
                predicates.add(cb.or(nameMatch, idMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Student> page = studentRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(StudentSummaryDto::fromEntity));
    }

    @Transactional(readOnly = true)
    public StudentDetailDto getStudentById(UUID studentId, User caller) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        academicSecurityService.validateStudentAccess(caller, student);
        return StudentDetailDto.fromEntity(student);
    }

    @Transactional(readOnly = true)
    public StudentDetailDto getCurrentStudentProfile(User caller) {
        Student student = studentRepository.findByUserId(caller.getId())
                .or(() -> studentRepository.findByUniversityEmail(caller.getEmail()))
                .orElseThrow(() -> new ResourceNotFoundException("No student academic profile found for current user: " + caller.getEmail()));

        return StudentDetailDto.fromEntity(student);
    }

    @Transactional
    public StudentDetailDto createStudent(StudentCreateRequest req, User caller) {
        // Uniqueness validations
        if (studentRepository.existsByUniversityStudentId(req.getUniversityStudentId())) {
            throw new IllegalArgumentException("Student with University ID '" + req.getUniversityStudentId() + "' already exists.");
        }
        if (studentRepository.existsByRollNumber(req.getRollNumber())) {
            throw new IllegalArgumentException("Student with Roll Number '" + req.getRollNumber() + "' already exists.");
        }
        if (studentRepository.existsByUniversityEmail(req.getUniversityEmail())) {
            throw new IllegalArgumentException("Student with email '" + req.getUniversityEmail() + "' already exists.");
        }
        if (req.getRegistrationNumber() != null && !req.getRegistrationNumber().isBlank()
                && studentRepository.existsByRegistrationNumber(req.getRegistrationNumber())) {
            throw new IllegalArgumentException("Student with Registration Number '" + req.getRegistrationNumber() + "' already exists.");
        }

        AcademicProgram program = programRepository.findById(req.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic program not found with ID: " + req.getProgramId()));

        Department department = program.getDepartment();

        // Check HOD departmental scope
        Set<String> roles = caller.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet());
        if (roles.contains("HOD") && !roles.contains("SUPER_ADMIN") && !roles.contains("TPO_ADMIN")) {
            List<Integer> scopedDeptIds = academicSecurityService.getScopedDepartmentIds(caller.getId());
            if (!scopedDeptIds.contains(department.getId())) {
                throw new AccessDeniedException("Access Denied: You cannot create students outside your assigned department.");
            }
        }

        AcademicBatch batch = batchRepository.findById(req.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic batch not found with ID: " + req.getBatchId()));

        // Optional user link
        User linkedUser = userRepository.findByEmailIgnoreCase(req.getUniversityEmail()).orElse(null);

        Student student = Student.builder()
                .universityStudentId(req.getUniversityStudentId().trim())
                .rollNumber(req.getRollNumber().trim())
                .registrationNumber(req.getRegistrationNumber() != null ? req.getRegistrationNumber().trim() : null)
                .firstName(req.getFirstName().trim())
                .lastName(req.getLastName().trim())
                .universityEmail(req.getUniversityEmail().trim().toLowerCase())
                .personalEmail(req.getPersonalEmail() != null ? req.getPersonalEmail().trim().toLowerCase() : null)
                .phoneNumber(req.getPhoneNumber() != null ? req.getPhoneNumber().trim() : null)
                .dateOfBirth(req.getDateOfBirth())
                .gender(req.getGender())
                .program(program)
                .department(department)
                .batch(batch)
                .currentSemester(req.getCurrentSemester())
                .section(req.getSection() != null ? req.getSection().trim().toUpperCase() : null)
                .admissionDate(req.getAdmissionDate() != null ? req.getAdmissionDate() : java.time.LocalDate.now())
                .academicStatus("ENROLLED")
                .verificationStatus("PENDING")
                .user(linkedUser)
                .build();

        student = studentRepository.save(student);

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "STUDENT_CREATED",
                "Student",
                student.getId().toString(),
                Map.of(
                        "universityStudentId", student.getUniversityStudentId(),
                        "rollNumber", student.getRollNumber(),
                        "departmentCode", department.getDepartmentCode(),
                        "programCode", program.getProgramCode()
                ),
                "127.0.0.1",
                "Web Portal API"
        );

        return StudentDetailDto.fromEntity(student);
    }

    @Transactional
    public StudentDetailDto updateStudent(UUID studentId, StudentUpdateRequest req, User caller) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        academicSecurityService.validateStudentAccess(caller, student);

        Set<String> roles = caller.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet());
        boolean isStudentRole = roles.contains("STUDENT") && !roles.contains("SUPER_ADMIN") && !roles.contains("TPO_ADMIN") && !roles.contains("HOD");

        // Profile fields
        student.setFirstName(req.getFirstName().trim());
        student.setLastName(req.getLastName().trim());
        student.setPersonalEmail(req.getPersonalEmail() != null ? req.getPersonalEmail().trim().toLowerCase() : null);
        student.setPhoneNumber(req.getPhoneNumber() != null ? req.getPhoneNumber().trim() : null);
        student.setDateOfBirth(req.getDateOfBirth());
        student.setGender(req.getGender());

        // Academic fields (only staff may update semester, section, status)
        if (!isStudentRole) {
            if (req.getCurrentSemester() != null) {
                student.setCurrentSemester(req.getCurrentSemester());
            }
            if (req.getSection() != null) {
                student.setSection(req.getSection().trim().toUpperCase());
            }
            if (req.getAcademicStatus() != null) {
                student.setAcademicStatus(req.getAcademicStatus());
            }
        }

        student = studentRepository.save(student);

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "STUDENT_UPDATED",
                "Student",
                student.getId().toString(),
                Map.of("rollNumber", student.getRollNumber()),
                "127.0.0.1",
                "Web Portal API"
        );

        return StudentDetailDto.fromEntity(student);
    }
}
