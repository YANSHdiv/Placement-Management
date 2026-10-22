package edu.jecrc.portal.modules.placement.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.entity.StudentQualification;
import edu.jecrc.portal.modules.academic.repository.StudentQualificationRepository;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.placement.dto.EligibilityEvaluationDto;
import edu.jecrc.portal.modules.placement.entity.*;
import edu.jecrc.portal.modules.placement.repository.EligibilityEvaluationRepository;
import edu.jecrc.portal.modules.placement.repository.EligibilityOverrideRepository;
import edu.jecrc.portal.modules.placement.repository.JobProfileRepository;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EligibilityEngineService {

    private final EligibilityEvaluationRepository evaluationRepository;
    private final EligibilityOverrideRepository overrideRepository;
    private final JobProfileRepository jobProfileRepository;
    private final StudentRepository studentRepository;
    private final StudentQualificationRepository qualificationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public EligibilityEvaluation evaluateStudentForJobProfile(UUID studentId, UUID jobProfileId, User evaluatingUser) {
        Student student = studentRepository.findWithDetailsById(studentId)
                .or(() -> studentRepository.findById(studentId))
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        JobProfile jobProfile = jobProfileRepository.findWithDetailsById(jobProfileId)
                .or(() -> jobProfileRepository.findById(jobProfileId))
                .orElseThrow(() -> new ResourceNotFoundException("Job profile not found with id: " + jobProfileId));

        PlacementPolicy policy = jobProfile.getPolicy();
        List<StudentQualification> qualifications = qualificationRepository.findByStudentId(studentId);

        List<String> unmetCriteria = new ArrayList<>();
        List<String> missingVerification = new ArrayList<>();
        Map<String, Object> snapshot = new HashMap<>();

        // 1. Program eligibility
        if (policy.getEligiblePrograms() != null && !policy.getEligiblePrograms().isEmpty()) {
            boolean programEligible = policy.getEligiblePrograms().stream()
                    .anyMatch(p -> p.getId().equals(student.getProgram().getId()));
            snapshot.put("studentProgram", student.getProgram().getProgramName());
            if (!programEligible) {
                unmetCriteria.add("Academic program '" + student.getProgram().getProgramName() + "' is not eligible for this role");
            }
        }

        // 2. Batch eligibility
        if (policy.getEligibleBatches() != null && !policy.getEligibleBatches().isEmpty()) {
            boolean batchEligible = student.getBatch() != null && policy.getEligibleBatches().stream()
                    .anyMatch(b -> b.getId().equals(student.getBatch().getId()));
            snapshot.put("studentBatch", student.getBatch() != null ? student.getBatch().getBatchName() : "None");
            if (!batchEligible) {
                String bName = student.getBatch() != null ? student.getBatch().getBatchName() : "Unassigned";
                unmetCriteria.add("Graduating batch '" + bName + "' is not eligible for this role");
            }
        }

        // 3. CGPA evaluation
        snapshot.put("studentCgpa", student.getCgpa());
        snapshot.put("requiredMinCgpa", policy.getMinCgpa());
        snapshot.put("studentVerificationStatus", student.getVerificationStatus());

        if (policy.getMinCgpa() != null) {
            if (student.getCgpa() == null) {
                missingVerification.add("Student CGPA is unrecorded or pending computation");
            } else if (Boolean.TRUE.equals(policy.getRequiresCgpaVerified()) && !"VERIFIED".equalsIgnoreCase(student.getVerificationStatus())) {
                missingVerification.add("Institutional verification required for academic record (current status: " + student.getVerificationStatus() + ")");
            } else if (student.getCgpa().compareTo(policy.getMinCgpa()) < 0) {
                unmetCriteria.add("Minimum CGPA threshold of " + policy.getMinCgpa() + " not met (Current: " + student.getCgpa() + ")");
            }
        }

        // 4. Class 10 qualification
        Optional<StudentQualification> class10Opt = qualifications.stream()
                .filter(q -> "CLASS_10".equalsIgnoreCase(q.getQualificationType()))
                .findFirst();

        snapshot.put("requiredClass10Percentage", policy.getMinClass10Percentage());
        if (policy.getMinClass10Percentage() != null) {
            if (class10Opt.isEmpty() || class10Opt.get().getPercentageOrCgpa() == null) {
                missingVerification.add("Class 10 marks are unrecorded or missing");
            } else {
                StudentQualification q10 = class10Opt.get();
                snapshot.put("class10Percentage", q10.getPercentageOrCgpa());
                if (Boolean.TRUE.equals(policy.getRequiresAllQualificationsVerified()) && !"VERIFIED".equalsIgnoreCase(q10.getVerificationStatus())) {
                    missingVerification.add("Class 10 qualification requires institutional verification (current status: " + q10.getVerificationStatus() + ")");
                } else if (q10.getPercentageOrCgpa().compareTo(policy.getMinClass10Percentage()) < 0) {
                    unmetCriteria.add("Class 10 minimum percentage of " + policy.getMinClass10Percentage() + "% not met (Actual: " + q10.getPercentageOrCgpa() + "%)");
                }
            }
        }

        // 5. Class 12 / Diploma qualification
        Optional<StudentQualification> class12Opt = qualifications.stream()
                .filter(q -> "CLASS_12".equalsIgnoreCase(q.getQualificationType()))
                .findFirst();
        Optional<StudentQualification> diplomaOpt = qualifications.stream()
                .filter(q -> "DIPLOMA".equalsIgnoreCase(q.getQualificationType()))
                .findFirst();

        snapshot.put("requiredClass12Percentage", policy.getMinClass12Percentage());
        snapshot.put("requiredDiplomaPercentage", policy.getMinDiplomaPercentage());

        if (policy.getMinClass12Percentage() != null || policy.getMinDiplomaPercentage() != null) {
            boolean hasClass12 = class12Opt.isPresent() && class12Opt.get().getPercentageOrCgpa() != null;
            boolean hasDiploma = diplomaOpt.isPresent() && diplomaOpt.get().getPercentageOrCgpa() != null;

            if (!hasClass12 && !hasDiploma) {
                missingVerification.add("Senior secondary (Class 12) or Diploma qualification marks are unrecorded");
            } else if (hasClass12) {
                StudentQualification q12 = class12Opt.get();
                snapshot.put("class12Percentage", q12.getPercentageOrCgpa());
                if (Boolean.TRUE.equals(policy.getRequiresAllQualificationsVerified()) && !"VERIFIED".equalsIgnoreCase(q12.getVerificationStatus())) {
                    missingVerification.add("Class 12 qualification requires institutional verification (current status: " + q12.getVerificationStatus() + ")");
                } else if (policy.getMinClass12Percentage() != null && q12.getPercentageOrCgpa().compareTo(policy.getMinClass12Percentage()) < 0) {
                    unmetCriteria.add("Class 12 minimum percentage of " + policy.getMinClass12Percentage() + "% not met (Actual: " + q12.getPercentageOrCgpa() + "%)");
                }
            } else {
                StudentQualification qDip = diplomaOpt.get();
                snapshot.put("diplomaPercentage", qDip.getPercentageOrCgpa());
                if (Boolean.TRUE.equals(policy.getRequiresAllQualificationsVerified()) && !"VERIFIED".equalsIgnoreCase(qDip.getVerificationStatus())) {
                    missingVerification.add("Diploma qualification requires institutional verification (current status: " + qDip.getVerificationStatus() + ")");
                } else if (policy.getMinDiplomaPercentage() != null && qDip.getPercentageOrCgpa().compareTo(policy.getMinDiplomaPercentage()) < 0) {
                    unmetCriteria.add("Diploma minimum percentage of " + policy.getMinDiplomaPercentage() + "% not met (Actual: " + qDip.getPercentageOrCgpa() + "%)");
                }
            }
        }

        // 6. Active backlogs
        snapshot.put("studentActiveBacklogs", student.getActiveBacklogs());
        snapshot.put("maxActiveBacklogsPermitted", policy.getMaxActiveBacklogs());
        if (student.getActiveBacklogs() != null && student.getActiveBacklogs() > policy.getMaxActiveBacklogs()) {
            unmetCriteria.add("Active backlog count exceeds allowable threshold (Allowed: " + policy.getMaxActiveBacklogs() + ", Current: " + student.getActiveBacklogs() + ")");
        }

        // 7. Historical backlogs
        snapshot.put("studentHistoricalBacklogs", student.getHistoricalBacklogs());
        snapshot.put("maxHistoricalBacklogsPermitted", policy.getMaxHistoricalBacklogs());
        if (student.getHistoricalBacklogs() != null && student.getHistoricalBacklogs() > policy.getMaxHistoricalBacklogs()) {
            unmetCriteria.add("Historical backlog count exceeds allowable threshold (Allowed: " + policy.getMaxHistoricalBacklogs() + ", Total: " + student.getHistoricalBacklogs() + ")");
        }

        // Determine outcome
        String outcome;
        if (!missingVerification.isEmpty()) {
            outcome = "NEEDS_VERIFICATION";
        } else if (!unmetCriteria.isEmpty()) {
            outcome = "INELIGIBLE";
        } else {
            outcome = "ELIGIBLE";
        }

        snapshot.put("evaluatedOutcome", outcome);
        snapshot.put("policyCode", policy.getPolicyCode());
        snapshot.put("policyVersion", policy.getVersionNumber());

        String snapshotJson = writeJson(snapshot);
        String unmetJson = writeJson(unmetCriteria);
        String missingJson = writeJson(missingVerification);

        EligibilityEvaluation evaluation = EligibilityEvaluation.builder()
                .student(student)
                .jobProfile(jobProfile)
                .policy(policy)
                .policyVersion(policy.getVersionNumber())
                .outcome(outcome)
                .criteriaSnapshotJson(snapshotJson)
                .unmetCriteriaJson(unmetJson)
                .missingVerificationJson(missingJson)
                .evaluatedAt(Instant.now())
                .evaluatedByUser(evaluatingUser)
                .build();

        return evaluationRepository.save(evaluation);
    }

    @Transactional(readOnly = true)
    public EligibilityEvaluationDto getEvaluationDto(UUID evaluationId) {
        EligibilityEvaluation eval = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found with id: " + evaluationId));
        return toDto(eval);
    }

    @Transactional(readOnly = true)
    public PageResponse<EligibilityEvaluationDto> getEvaluationsForJobProfile(UUID jobProfileId, String outcome, Pageable pageable) {
        Page<EligibilityEvaluation> page = evaluationRepository.findByJobProfileIdAndOutcome(jobProfileId, outcome, pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    @Transactional(readOnly = true)
    public PageResponse<EligibilityEvaluationDto> getEvaluationsForStudent(UUID studentId, Pageable pageable) {
        Page<EligibilityEvaluation> page = evaluationRepository.findByStudentIdOrderByEvaluatedAtDesc(studentId, pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    @Transactional
    public EligibilityEvaluationDto evaluateAndGetDto(UUID studentId, UUID jobProfileId, User evaluatingUser) {
        EligibilityEvaluation eval = evaluateStudentForJobProfile(studentId, jobProfileId, evaluatingUser);
        return toDto(eval);
    }

    public EligibilityEvaluationDto toDto(EligibilityEvaluation e) {
        Optional<EligibilityOverride> overrideOpt = overrideRepository.findByEvaluationId(e.getId());

        Map<String, Object> snapshot = readJsonMap(e.getCriteriaSnapshotJson());
        List<String> unmet = readJsonList(e.getUnmetCriteriaJson());
        List<String> missing = readJsonList(e.getMissingVerificationJson());

        Student s = e.getStudent();
        JobProfile jp = e.getJobProfile();

        String departmentName = null;
        if (s.getDepartment() != null) {
            try {
                departmentName = s.getDepartment().getDepartmentName();
            } catch (Exception ignored) {}
        }

        String companyName = null;
        if (jp.getDrive() != null && jp.getDrive().getCompany() != null) {
            try {
                companyName = jp.getDrive().getCompany().getCompanyName();
            } catch (Exception ignored) {}
        }

        return EligibilityEvaluationDto.builder()
                .id(e.getId())
                .studentId(s.getId())
                .studentRollNumber(s.getRollNumber())
                .studentName(s.getFirstName() + " " + s.getLastName())
                .studentEmail(s.getUniversityEmail())
                .studentDepartment(departmentName)
                .jobProfileId(jp.getId())
                .jobProfileTitle(jp.getProfileTitle())
                .companyName(companyName)
                .policyId(e.getPolicy().getId())
                .policyCode(e.getPolicy().getPolicyCode())
                .policyVersion(e.getPolicyVersion())
                .outcome(e.getOutcome())
                .criteriaSnapshot(snapshot)
                .unmetCriteria(unmet)
                .missingVerification(missing)
                .evaluatedAt(e.getEvaluatedAt())
                .isOverridden(overrideOpt.isPresent())
                .overrideReason(overrideOpt.map(EligibilityOverride::getOverrideReason).orElse(null))
                .overrideApprovedBy(overrideOpt.map(o -> o.getApprovedByUser().getEmail()).orElse(null))
                .overriddenAt(overrideOpt.map(EligibilityOverride::getOverriddenAt).orElse(null))
                .build();
    }

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize criteria snapshot to JSON", e);
            return "{}";
        }
    }

    private Map<String, Object> readJsonMap(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private List<String> readJsonList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
