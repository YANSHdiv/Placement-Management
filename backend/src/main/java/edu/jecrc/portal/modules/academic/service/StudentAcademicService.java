package edu.jecrc.portal.modules.academic.service;

import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.dto.*;
import edu.jecrc.portal.modules.academic.entity.SemesterResult;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.entity.StudentQualification;
import edu.jecrc.portal.modules.academic.entity.SubjectAttempt;
import edu.jecrc.portal.modules.academic.repository.SemesterResultRepository;
import edu.jecrc.portal.modules.academic.repository.StudentQualificationRepository;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.academic.repository.SubjectAttemptRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentAcademicService {

    private final StudentRepository studentRepository;
    private final StudentQualificationRepository qualificationRepository;
    private final SemesterResultRepository semesterResultRepository;
    private final SubjectAttemptRepository subjectAttemptRepository;
    private final AcademicSecurityService academicSecurityService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<StudentQualificationDto> getQualifications(UUID studentId, User caller) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        academicSecurityService.validateStudentAccess(caller, student);

        return qualificationRepository.findByStudentIdOrderByPassingYearAsc(studentId).stream()
                .map(StudentQualificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<StudentQualificationDto> upsertQualifications(UUID studentId, List<StudentQualificationDto> dtos, User caller) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        academicSecurityService.validateStudentAccess(caller, student);

        List<StudentQualification> savedList = new ArrayList<>();

        for (StudentQualificationDto dto : dtos) {
            StudentQualification qualification = qualificationRepository
                    .findByStudentIdAndQualificationType(studentId, dto.getQualificationType().toUpperCase())
                    .orElseGet(() -> StudentQualification.builder()
                            .student(student)
                            .qualificationType(dto.getQualificationType().toUpperCase())
                            .build());

            qualification.setBoardOrUniversity(dto.getBoardOrUniversity().trim());
            qualification.setInstitutionName(dto.getInstitutionName() != null ? dto.getInstitutionName().trim() : null);
            qualification.setPassingYear(dto.getPassingYear());
            qualification.setMarkingScheme(dto.getMarkingScheme() != null ? dto.getMarkingScheme().toUpperCase() : "PERCENTAGE");

            // Maintain distinction between NULL and 0.0 marks
            qualification.setPercentageOrCgpa(dto.getPercentageOrCgpa());
            qualification.setMaximumMarks(dto.getMaximumMarks());
            qualification.setObtainedMarks(dto.getObtainedMarks());
            qualification.setMarksheetDocumentRef(dto.getMarksheetDocumentRef());

            // If updated by student, reset to PENDING
            qualification.setVerificationStatus("PENDING");

            savedList.add(qualificationRepository.save(qualification));
        }

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "QUALIFICATIONS_UPSERTED",
                "Student",
                studentId.toString(),
                Map.of("qualificationsCount", savedList.size()),
                "127.0.0.1",
                "Web Portal API"
        );

        return savedList.stream().map(StudentQualificationDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SemesterResultDto> getSemesterResults(UUID studentId, User caller) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        academicSecurityService.validateStudentAccess(caller, student);

        return semesterResultRepository.findByStudentIdOrderBySemesterNumberAsc(studentId).stream()
                .map(SemesterResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public SemesterResultDto addOrUpdateSemesterResult(UUID studentId, SemesterResultDto dto, User caller) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        academicSecurityService.validateStudentAccess(caller, student);

        SemesterResult result = semesterResultRepository
                .findByStudentIdAndSemesterNumber(studentId, dto.getSemesterNumber())
                .orElseGet(() -> SemesterResult.builder()
                        .student(student)
                        .semesterNumber(dto.getSemesterNumber())
                        .build());

        result.setSgpa(dto.getSgpa());
        result.setCreditsRegistered(dto.getCreditsRegistered());
        result.setCreditsEarned(dto.getCreditsEarned());
        result.setResultStatus(dto.getResultStatus() != null ? dto.getResultStatus().toUpperCase() : "PASSED");
        result.setMarksheetDocumentRef(dto.getMarksheetDocumentRef());

        // Process Subject Attempts if provided
        if (dto.getSubjectAttempts() != null && !dto.getSubjectAttempts().isEmpty()) {
            boolean hasActiveBacklog = false;
            for (SubjectAttemptDto attemptDto : dto.getSubjectAttempts()) {
                SubjectAttempt attempt = SubjectAttempt.builder()
                        .result(result)
                        .student(student)
                        .subjectCode(attemptDto.getSubjectCode().trim().toUpperCase())
                        .subjectName(attemptDto.getSubjectName().trim())
                        .semesterNumber(dto.getSemesterNumber())
                        .attemptNumber(attemptDto.getAttemptNumber() != null ? attemptDto.getAttemptNumber() : 1)
                        .credits(attemptDto.getCredits() != null ? attemptDto.getCredits() : new BigDecimal("3.0"))
                        .grade(attemptDto.getGrade())
                        .marksObtained(attemptDto.getMarksObtained())
                        .maxMarks(attemptDto.getMaxMarks())
                        .isBacklog(Boolean.TRUE.equals(attemptDto.getIsBacklog()))
                        .isCleared(Boolean.TRUE.equals(attemptDto.getIsCleared()))
                        .clearedInSemester(attemptDto.getClearedInSemester())
                        .build();

                if (Boolean.TRUE.equals(attempt.getIsBacklog()) && !Boolean.TRUE.equals(attempt.getIsCleared())) {
                    hasActiveBacklog = true;
                }
                result.getSubjectAttempts().add(attempt);
            }
            result.setHasActiveBacklog(hasActiveBacklog);
        }

        SemesterResult saved = semesterResultRepository.save(result);

        // Recalculate student canonical summary metrics (CGPA, active & historical backlogs)
        recalculateStudentMetrics(student);

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "SEMESTER_RESULT_SAVED",
                "SemesterResult",
                saved.getId().toString(),
                Map.of("studentId", studentId.toString(), "semester", dto.getSemesterNumber()),
                "127.0.0.1",
                "Web Portal API"
        );

        return SemesterResultDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SubjectAttemptDto> getBacklogs(UUID studentId, User caller) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        academicSecurityService.validateStudentAccess(caller, student);

        return subjectAttemptRepository.findByStudentIdAndIsBacklogTrue(studentId).stream()
                .map(SubjectAttemptDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentDetailDto verifyStudentRecord(UUID studentId, StudentVerifyRequest req, User caller) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        academicSecurityService.validateVerificationPrivilege(caller, student);

        String previousStatus = student.getVerificationStatus();
        student.setVerificationStatus(req.getVerificationStatus().trim().toUpperCase());
        student.setVerifiedBy(caller);
        student.setVerifiedAt(Instant.now());
        student.setVerificationRemarks(req.getVerificationRemarks());

        Student updated = studentRepository.save(student);

        auditService.logAsync(
                caller.getId(),
                caller.getEmail(),
                "STUDENT_VERIFIED",
                "Student",
                student.getId().toString(),
                Map.of(
                        "rollNumber", student.getRollNumber(),
                        "previousStatus", previousStatus,
                        "newStatus", updated.getVerificationStatus(),
                        "remarks", req.getVerificationRemarks() != null ? req.getVerificationRemarks() : ""
                ),
                "127.0.0.1",
                "Web Portal API"
        );

        return StudentDetailDto.fromEntity(updated);
    }

    public void recalculateStudentMetrics(Student student) {
        List<SemesterResult> results = semesterResultRepository.findByStudentIdOrderBySemesterNumberAsc(student.getId());
        List<SubjectAttempt> attempts = subjectAttemptRepository.findByStudentIdOrderBySemesterNumberAscAttemptNumberAsc(student.getId());

        // 1. Backlogs
        int activeBacklogs = 0;
        Set<String> historicalBacklogSubjects = new HashSet<>();

        for (SubjectAttempt attempt : attempts) {
            if (Boolean.TRUE.equals(attempt.getIsBacklog())) {
                historicalBacklogSubjects.add(attempt.getSubjectCode());
                if (!Boolean.TRUE.equals(attempt.getIsCleared())) {
                    activeBacklogs++;
                }
            }
        }

        student.setActiveBacklogs(activeBacklogs);
        student.setHistoricalBacklogs(historicalBacklogSubjects.size());

        // 2. CGPA (Average of non-null SGPAs; if all null, CGPA is NULL)
        List<BigDecimal> validSgpas = results.stream()
                .map(SemesterResult::getSgpa)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (validSgpas.isEmpty()) {
            student.setCgpa(null);
        } else {
            BigDecimal sum = validSgpas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avg = sum.divide(BigDecimal.valueOf(validSgpas.size()), 2, RoundingMode.HALF_UP);
            student.setCgpa(avg);
        }

        // 3. Total Credits Earned
        BigDecimal totalCredits = results.stream()
                .map(SemesterResult::getCreditsEarned)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        student.setTotalCreditsEarned(totalCredits);

        studentRepository.save(student);
    }
}
