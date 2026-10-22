package edu.jecrc.portal.modules.placement.service;

import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.BadRequestException;
import edu.jecrc.portal.common.exception.ForbiddenException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.placement.dto.JobApplicationCreateRequest;
import edu.jecrc.portal.modules.placement.dto.JobApplicationDto;
import edu.jecrc.portal.modules.placement.dto.JobApplicationStatusUpdateRequest;
import edu.jecrc.portal.modules.placement.entity.*;
import edu.jecrc.portal.modules.placement.repository.EligibilityOverrideRepository;
import edu.jecrc.portal.modules.placement.repository.JobApplicationRepository;
import edu.jecrc.portal.modules.placement.repository.JobProfileRepository;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobProfileRepository jobProfileRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final EligibilityEngineService eligibilityEngineService;
    private final EligibilityOverrideRepository overrideRepository;
    private final AuditService auditService;

    @Transactional
    public JobApplicationDto apply(UUID jobProfileId, JobApplicationCreateRequest req, String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Student student = studentRepository.findByUserId(user.getId())
                .or(() -> studentRepository.findByUniversityEmailIgnoreCase(userEmail))
                .orElseThrow(() -> new BadRequestException("Current user account is not associated with an enrolled student record"));

        JobProfile jobProfile = jobProfileRepository.findById(jobProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Job profile not found with id: " + jobProfileId));

        PlacementDrive drive = jobProfile.getDrive();
        if (!"PUBLISHED".equalsIgnoreCase(drive.getDriveStatus()) && !"IN_PROGRESS".equalsIgnoreCase(drive.getDriveStatus())) {
            throw new BadRequestException("Applications cannot be submitted for drive in status: " + drive.getDriveStatus());
        }

        if (drive.getRegistrationDeadline() != null && Instant.now().isAfter(drive.getRegistrationDeadline())) {
            throw new BadRequestException("Registration deadline for this recruitment drive has passed");
        }

        if (applicationRepository.existsByJobProfileIdAndStudentId(jobProfileId, student.getId())) {
            throw new BadRequestException("An application has already been submitted for this job profile");
        }

        // Double-check real-time eligibility evaluation inside transaction
        EligibilityEvaluation freshEvaluation = eligibilityEngineService.evaluateStudentForJobProfile(student.getId(), jobProfileId, user);

        if (!"ELIGIBLE".equalsIgnoreCase(freshEvaluation.getOutcome())) {
            // Check if there is an approved override for this student and profile
            Optional<EligibilityOverride> overrideOpt = overrideRepository.findByEvaluationStudentId(student.getId()).stream()
                    .filter(o -> o.getEvaluation().getJobProfile().getId().equals(jobProfileId) && "ELIGIBLE".equalsIgnoreCase(o.getOverrideOutcome()))
                    .findFirst();

            if (overrideOpt.isEmpty()) {
                throw new BadRequestException("Student is not eligible for this job profile (Evaluation Outcome: " + freshEvaluation.getOutcome() + ", Unmet: " + freshEvaluation.getUnmetCriteriaJson() + ", Missing: " + freshEvaluation.getMissingVerificationJson() + ")");
            }
        }

        JobApplication application = JobApplication.builder()
                .jobProfile(jobProfile)
                .student(student)
                .evaluation(freshEvaluation)
                .applicationStatus("APPLIED")
                .resumeVersionTag(req.getResumeVersionTag().trim())
                .studentRemarks(req.getStudentRemarks())
                .appliedAt(Instant.now())
                .statusUpdatedAt(Instant.now())
                .statusUpdatedBy(user)
                .build();

        application = applicationRepository.save(application);

        auditService.logAsync(
                user.getId(),
                userEmail,
                "SUBMIT_JOB_APPLICATION",
                "JobApplication",
                application.getId().toString(),
                Map.of("jobProfileTitle", jobProfile.getProfileTitle(), "studentRoll", student.getRollNumber()),
                null,
                null
        );

        return toDto(application);
    }

    @Transactional
    public JobApplicationDto withdraw(UUID applicationId, String userEmail) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);

        // Security check: only the owning student can withdraw
        if (user != null) {
            boolean isOwner = application.getStudent().getUniversityEmail().equalsIgnoreCase(userEmail)
                    || (application.getStudent().getUser() != null && application.getStudent().getUser().getId().equals(user.getId()));
            if (!isOwner) {
                throw new ForbiddenException("You cannot withdraw another student's application");
            }
        }

        if (!"APPLIED".equalsIgnoreCase(application.getApplicationStatus())) {
            throw new BadRequestException("Only applications in APPLIED status can be withdrawn. Current status: " + application.getApplicationStatus());
        }

        application.setApplicationStatus("WITHDRAWN");
        application.setStatusUpdatedAt(Instant.now());
        application.setStatusUpdatedBy(user);
        application.setStatusChangeReason("Withdrawn by applicant");

        application = applicationRepository.save(application);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "WITHDRAW_JOB_APPLICATION",
                "JobApplication",
                application.getId().toString(),
                Map.of("status", "WITHDRAWN"),
                null,
                null
        );

        return toDto(application);
    }

    @Transactional
    public JobApplicationDto updateStatus(UUID applicationId, JobApplicationStatusUpdateRequest req, String userEmail) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);

        String newStatus = req.getStatus().trim().toUpperCase();
        application.setApplicationStatus(newStatus);
        application.setStatusUpdatedAt(Instant.now());
        application.setStatusUpdatedBy(user);
        application.setStatusChangeReason(req.getReason());

        application = applicationRepository.save(application);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "UPDATE_APPLICATION_STATUS",
                "JobApplication",
                application.getId().toString(),
                Map.of("newStatus", newStatus, "reason", req.getReason() != null ? req.getReason() : ""),
                null,
                null
        );

        return toDto(application);
    }

    @Transactional(readOnly = true)
    public JobApplicationDto getApplicationById(UUID applicationId, String userEmail, boolean isTpoOrAdmin) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        if (!isTpoOrAdmin) {
            boolean isOwner = application.getStudent().getUniversityEmail().equalsIgnoreCase(userEmail);
            if (!isOwner) {
                throw new ForbiddenException("Access denied to another student's application");
            }
        }

        return toDto(application);
    }

    @Transactional(readOnly = true)
    public PageResponse<JobApplicationDto> getMyApplications(String userEmail, Pageable pageable) {
        Student student = studentRepository.findByUniversityEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new BadRequestException("No student profile linked to account " + userEmail));

        Page<JobApplication> page = applicationRepository.findByStudentIdOrderByAppliedAtDesc(student.getId(), pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    @Transactional(readOnly = true)
    public PageResponse<JobApplicationDto> searchApplications(UUID jobProfileId, String status, String query, Pageable pageable) {
        Page<JobApplication> page = applicationRepository.searchApplications(jobProfileId, status, query, pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    public JobApplicationDto toDto(JobApplication a) {
        Student s = a.getStudent();
        JobProfile jp = a.getJobProfile();
        EligibilityEvaluation eval = a.getEvaluation();

        boolean isOverridden = eval != null && overrideRepository.findByEvaluationId(eval.getId()).isPresent();

        return JobApplicationDto.builder()
                .id(a.getId())
                .jobProfileId(jp.getId())
                .jobProfileTitle(jp.getProfileTitle())
                .companyName(jp.getDrive().getCompany().getCompanyName())
                .driveTitle(jp.getDrive().getDriveTitle())
                .studentId(s.getId())
                .studentRollNumber(s.getRollNumber())
                .studentName(s.getFirstName() + " " + s.getLastName())
                .studentEmail(s.getUniversityEmail())
                .evaluationId(eval != null ? eval.getId() : null)
                .evaluationOutcome(eval != null ? eval.getOutcome() : null)
                .isOverridden(isOverridden)
                .applicationStatus(a.getApplicationStatus())
                .resumeVersionTag(a.getResumeVersionTag())
                .studentRemarks(a.getStudentRemarks())
                .appliedAt(a.getAppliedAt())
                .statusUpdatedAt(a.getStatusUpdatedAt())
                .statusChangeReason(a.getStatusChangeReason())
                .build();
    }
}
