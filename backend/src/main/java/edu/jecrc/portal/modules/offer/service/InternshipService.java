package edu.jecrc.portal.modules.offer.service;

import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.offer.dto.*;
import edu.jecrc.portal.modules.offer.entity.InternshipEvaluation;
import edu.jecrc.portal.modules.offer.entity.InternshipProgressLog;
import edu.jecrc.portal.modules.offer.entity.InternshipRecord;
import edu.jecrc.portal.modules.offer.entity.PlacementOffer;
import edu.jecrc.portal.modules.offer.repository.InternshipEvaluationRepository;
import edu.jecrc.portal.modules.offer.repository.InternshipProgressLogRepository;
import edu.jecrc.portal.modules.offer.repository.InternshipRecordRepository;
import edu.jecrc.portal.modules.offer.repository.PlacementOfferRepository;
import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.entity.JobApplication;
import edu.jecrc.portal.modules.placement.entity.JobProfile;
import edu.jecrc.portal.modules.placement.repository.CompanyRepository;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternshipService {

    private final InternshipRecordRepository internshipRepository;
    private final InternshipProgressLogRepository logRepository;
    private final InternshipEvaluationRepository evaluationRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final JobProfileRepository jobProfileRepository;
    private final JobApplicationRepository applicationRepository;
    private final PlacementOfferRepository offerRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public InternshipRecordDto createInternship(InternshipCreateRequest req, User caller) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + req.getStudentId()));

        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + req.getCompanyId()));

        if (req.getStartDate().isAfter(req.getExpectedEndDate())) {
            throw ApiException.badRequest("Start date cannot be after expected end date");
        }

        JobProfile jobProfile = null;
        if (req.getJobProfileId() != null) {
            jobProfile = jobProfileRepository.findById(req.getJobProfileId()).orElse(null);
        }

        JobApplication application = null;
        if (req.getApplicationId() != null) {
            application = applicationRepository.findById(req.getApplicationId()).orElse(null);
        }

        PlacementOffer offer = null;
        if (req.getPlacementOfferId() != null) {
            offer = offerRepository.findById(req.getPlacementOfferId()).orElse(null);
        }

        User mentor = null;
        if (req.getFacultyMentorId() != null) {
            mentor = userRepository.findById(req.getFacultyMentorId()).orElse(null);
        }

        InternshipRecord record = InternshipRecord.builder()
                .student(student)
                .company(company)
                .jobProfile(jobProfile)
                .application(application)
                .placementOffer(offer)
                .internshipTitle(req.getInternshipTitle())
                .internshipType(req.getInternshipType() != null ? req.getInternshipType() : "SUMMER")
                .startDate(req.getStartDate())
                .expectedEndDate(req.getExpectedEndDate())
                .monthlyStipendInr(req.getMonthlyStipendInr())
                .currency(req.getCurrency() != null ? req.getCurrency() : "INR")
                .workArrangement(req.getWorkArrangement() != null ? req.getWorkArrangement() : "ON_SITE")
                .location(req.getLocation())
                .appointmentDocRef(req.getAppointmentDocRef())
                .facultyMentor(mentor)
                .industryMentorName(req.getIndustryMentorName())
                .industryMentorEmail(req.getIndustryMentorEmail())
                .industryMentorPhone(req.getIndustryMentorPhone())
                .internshipStatus("ACTIVE") // Initialized directly to active
                .hasPpoPotential(Boolean.TRUE.equals(req.getHasPpoPotential()))
                .build();

        InternshipRecord saved = internshipRepository.save(record);
        auditService.logAsync(caller.getId(), caller.getEmail(), "INTERNSHIP_CREATE", "INTERNSHIP_RECORD",
                saved.getId().toString(), Map.of("studentId", student.getUniversityStudentId(), "company", company.getCompanyName()),
                "127.0.0.1", "Web Portal API");

        return InternshipRecordDto.fromEntity(saved);
    }

    @Transactional
    public InternshipRecordDto assignMentor(UUID internshipId, InternshipMentorAssignRequest req, User caller) {
        InternshipRecord record = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found: " + internshipId));

        User mentor = userRepository.findById(req.getFacultyMentorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty user not found: " + req.getFacultyMentorUserId()));

        record.setFacultyMentor(mentor);
        if (req.getIndustryMentorName() != null) record.setIndustryMentorName(req.getIndustryMentorName());
        if (req.getIndustryMentorEmail() != null) record.setIndustryMentorEmail(req.getIndustryMentorEmail());
        if (req.getIndustryMentorPhone() != null) record.setIndustryMentorPhone(req.getIndustryMentorPhone());

        InternshipRecord saved = internshipRepository.save(record);
        auditService.logAsync(caller.getId(), caller.getEmail(), "INTERNSHIP_MENTOR_ASSIGN", "INTERNSHIP_RECORD",
                saved.getId().toString(), Map.of("mentorEmail", mentor.getEmail()), "127.0.0.1", "Web Portal API");

        return InternshipRecordDto.fromEntity(saved);
    }

    @Transactional
    public InternshipProgressLogDto submitProgressLog(UUID internshipId, InternshipProgressLogCreateRequest req, User caller) {
        InternshipRecord record = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found: " + internshipId));

        // Ownership: Caller must be the intern student
        Student student = studentRepository.findByUniversityEmailIgnoreCase(caller.getEmail())
                .orElseThrow(() -> ApiException.forbidden("Authenticated user is not registered as a student"));

        if (!record.getStudent().getId().equals(student.getId())) {
            throw ApiException.forbidden("Access denied: You can only submit logs for your own internship");
        }

        if ("COMPLETED".equals(record.getInternshipStatus()) || "TERMINATED".equals(record.getInternshipStatus())) {
            throw ApiException.badRequest("Cannot submit logs for an internship that is already " + record.getInternshipStatus());
        }

        int nextLogNumber = logRepository.countByInternshipId(internshipId) + 1;

        InternshipProgressLog logEntry = InternshipProgressLog.builder()
                .internship(record)
                .logNumber(nextLogNumber)
                .reportingPeriodStart(req.getReportingPeriodStart())
                .reportingPeriodEnd(req.getReportingPeriodEnd())
                .summaryOfWork(req.getSummaryOfWork())
                .learningOutcomes(req.getLearningOutcomes())
                .challengesEncountered(req.getChallengesEncountered())
                .evidenceAttachmentRef(req.getEvidenceAttachmentRef())
                .submissionStatus("SUBMITTED")
                .build();

        InternshipProgressLog saved = logRepository.save(logEntry);
        auditService.logAsync(caller.getId(), caller.getEmail(), "INTERNSHIP_LOG_SUBMIT", "INTERNSHIP_LOG",
                saved.getId().toString(), Map.of("logNumber", nextLogNumber), "127.0.0.1", "Web Portal API");

        return InternshipProgressLogDto.fromEntity(saved);
    }

    @Transactional
    public InternshipProgressLogDto reviewProgressLog(UUID internshipId, UUID logId, InternshipProgressLogReviewRequest req, User caller) {
        InternshipProgressLog logEntry = logRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress log not found: " + logId));

        InternshipRecord internship = logEntry.getInternship();

        // Access check: caller must be assigned faculty mentor or TPO admin
        boolean isTpo = caller.getRoles().stream().anyMatch(r -> List.of("SUPER_ADMIN", "TPO_ADMIN", "TPO_COORDINATOR").contains(r.getRoleName()));
        boolean isMentor = internship.getFacultyMentor() != null && internship.getFacultyMentor().getId().equals(caller.getId());

        if (!isTpo && !isMentor) {
            throw ApiException.forbidden("Access denied: You are not the assigned mentor for this intern");
        }

        logEntry.setSubmissionStatus(req.getSubmissionStatus());
        logEntry.setMentorFeedback(req.getMentorFeedback());
        logEntry.setMentorReviewedAt(Instant.now());
        logEntry.setMentorReviewer(caller);

        InternshipProgressLog saved = logRepository.save(logEntry);
        auditService.logAsync(caller.getId(), caller.getEmail(), "INTERNSHIP_LOG_REVIEW", "INTERNSHIP_LOG",
                saved.getId().toString(), Map.of("status", req.getSubmissionStatus()), "127.0.0.1", "Web Portal API");

        return InternshipProgressLogDto.fromEntity(saved);
    }

    @Transactional
    public InternshipEvaluationDto submitEvaluation(UUID internshipId, InternshipEvaluationCreateRequest req, User caller) {
        InternshipRecord internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found: " + internshipId));

        boolean isTpo = caller.getRoles().stream().anyMatch(r -> List.of("SUPER_ADMIN", "TPO_ADMIN", "TPO_COORDINATOR").contains(r.getRoleName()));
        boolean isMentor = internship.getFacultyMentor() != null && internship.getFacultyMentor().getId().equals(caller.getId());

        if (!isTpo && !isMentor) {
            throw ApiException.forbidden("Access denied: You are not authorized to evaluate this intern");
        }

        InternshipEvaluation eval = InternshipEvaluation.builder()
                .internship(internship)
                .evaluatorUser(caller)
                .evaluationType(req.getEvaluationType() != null ? req.getEvaluationType() : "MID_TERM")
                .technicalScore(req.getTechnicalScore())
                .communicationScore(req.getCommunicationScore())
                .professionalismScore(req.getProfessionalismScore())
                .overallScore(req.getOverallScore())
                .writtenFeedback(req.getWrittenFeedback())
                .confidentialStaffNotes(req.getConfidentialStaffNotes())
                .recommendForPpo(Boolean.TRUE.equals(req.getRecommendForPpo()))
                .build();

        InternshipEvaluation saved = evaluationRepository.save(eval);
        auditService.logAsync(caller.getId(), caller.getEmail(), "INTERNSHIP_EVALUATE", "INTERNSHIP_EVALUATION",
                saved.getId().toString(), Map.of("type", saved.getEvaluationType(), "overallScore", saved.getOverallScore()),
                "127.0.0.1", "Web Portal API");

        return InternshipEvaluationDto.fromEntity(saved, false);
    }

    @Transactional
    public InternshipRecordDto completeInternship(UUID internshipId, InternshipCompletionRequest req, User caller) {
        InternshipRecord record = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found: " + internshipId));

        if (!"ACTIVE".equals(record.getInternshipStatus()) && !"ACCEPTED".equals(record.getInternshipStatus())) {
            throw ApiException.badRequest("Internship cannot be marked completed from status: " + record.getInternshipStatus());
        }

        if (req.getActualEndDate().isBefore(record.getStartDate())) {
            throw ApiException.badRequest("Actual end date cannot be before start date");
        }

        record.setInternshipStatus(req.getStatus() != null ? req.getStatus() : "COMPLETED");
        record.setActualEndDate(req.getActualEndDate());
        record.setCompletedAt(Instant.now());
        record.setCompletionRemarks(req.getCompletionRemarks());

        InternshipRecord saved = internshipRepository.save(record);
        auditService.logAsync(caller.getId(), caller.getEmail(), "INTERNSHIP_COMPLETE", "INTERNSHIP_RECORD",
                saved.getId().toString(), Map.of("status", saved.getInternshipStatus()), "127.0.0.1", "Web Portal API");

        return InternshipRecordDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public InternshipRecordDto getInternshipById(UUID internshipId, User caller) {
        InternshipRecord record = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found: " + internshipId));

        boolean isStudent = caller.getRoles().stream().anyMatch(r -> "STUDENT".equalsIgnoreCase(r.getRoleName()));
        if (isStudent && !record.getStudent().getUniversityEmail().equalsIgnoreCase(caller.getEmail())) {
            throw ApiException.forbidden("Access denied");
        }

        return InternshipRecordDto.fromEntity(record);
    }

    @Transactional(readOnly = true)
    public List<InternshipRecordDto> getMyInternships(String studentEmail) {
        Student student = studentRepository.findByUniversityEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found: " + studentEmail));

        return internshipRepository.findByStudentId(student.getId()).stream()
                .map(InternshipRecordDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InternshipRecordDto> getAssignedInternshipsForFaculty(UUID facultyUserId) {
        return internshipRepository.findByFacultyMentorId(facultyUserId).stream()
                .map(InternshipRecordDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<InternshipRecordDto> searchInternships(UUID studentId, UUID companyId, String status, UUID facultyMentorId, Pageable pageable) {
        return internshipRepository.searchInternships(studentId, companyId, status, facultyMentorId, pageable)
                .map(InternshipRecordDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<InternshipProgressLogDto> getProgressLogs(UUID internshipId, User caller) {
        InternshipRecord record = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found: " + internshipId));

        boolean isStudent = caller.getRoles().stream().anyMatch(r -> "STUDENT".equalsIgnoreCase(r.getRoleName()));
        if (isStudent && !record.getStudent().getUniversityEmail().equalsIgnoreCase(caller.getEmail())) {
            throw ApiException.forbidden("Access denied");
        }

        return logRepository.findByInternshipIdOrderByLogNumberAsc(internshipId).stream()
                .map(InternshipProgressLogDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InternshipEvaluationDto> getEvaluations(UUID internshipId, User caller) {
        InternshipRecord record = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found: " + internshipId));

        boolean isStudent = caller.getRoles().stream().anyMatch(r -> "STUDENT".equalsIgnoreCase(r.getRoleName()));
        if (isStudent && !record.getStudent().getUniversityEmail().equalsIgnoreCase(caller.getEmail())) {
            throw ApiException.forbidden("Access denied");
        }

        return evaluationRepository.findByInternshipIdOrderByEvaluatedAtDesc(internshipId).stream()
                .map(e -> InternshipEvaluationDto.fromEntity(e, isStudent))
                .toList();
    }
}
