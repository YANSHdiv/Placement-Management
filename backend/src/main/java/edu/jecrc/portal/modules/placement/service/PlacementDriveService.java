package edu.jecrc.portal.modules.placement.service;

import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.BadRequestException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.placement.dto.*;
import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.entity.JobProfile;
import edu.jecrc.portal.modules.placement.entity.PlacementDrive;
import edu.jecrc.portal.modules.placement.entity.PlacementPolicy;
import edu.jecrc.portal.modules.placement.repository.CompanyRepository;
import edu.jecrc.portal.modules.placement.repository.JobProfileRepository;
import edu.jecrc.portal.modules.placement.repository.PlacementDriveRepository;
import edu.jecrc.portal.modules.placement.repository.PlacementPolicyRepository;
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
public class PlacementDriveService {

    private final PlacementDriveRepository driveRepository;
    private final JobProfileRepository jobProfileRepository;
    private final CompanyRepository companyRepository;
    private final PlacementPolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public PlacementDriveDto createDrive(PlacementDriveCreateRequest req, String userEmail) {
        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + req.getCompanyId()));

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);

        PlacementDrive drive = PlacementDrive.builder()
                .company(company)
                .driveTitle(req.getDriveTitle().trim())
                .academicYear(req.getAcademicYear().trim())
                .driveStatus("DRAFT")
                .registrationStartDate(req.getRegistrationStartDate())
                .registrationDeadline(req.getRegistrationDeadline())
                .driveDate(req.getDriveDate())
                .recruitmentSchedule(req.getRecruitmentSchedule())
                .venue(req.getVenue())
                .reportingTime(req.getReportingTime())
                .instructions(req.getInstructions())
                .contactDetails(req.getContactDetails())
                .createdBy(user)
                .updatedBy(user)
                .build();

        drive = driveRepository.save(drive);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "CREATE_PLACEMENT_DRIVE",
                "PlacementDrive",
                drive.getId().toString(),
                Map.of("driveTitle", drive.getDriveTitle(), "company", company.getCompanyName()),
                null,
                null
        );

        return toDto(drive);
    }

    @Transactional
    public PlacementDriveDto updateDrive(UUID driveId, PlacementDriveUpdateRequest req, String userEmail) {
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found with id: " + driveId));

        if ("ARCHIVED".equals(drive.getDriveStatus()) || "CLOSED".equals(drive.getDriveStatus())) {
            throw new BadRequestException("Cannot modify drive in " + drive.getDriveStatus() + " status");
        }

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);

        drive.setDriveTitle(req.getDriveTitle().trim());
        drive.setAcademicYear(req.getAcademicYear().trim());
        drive.setRegistrationStartDate(req.getRegistrationStartDate());
        drive.setRegistrationDeadline(req.getRegistrationDeadline());
        drive.setDriveDate(req.getDriveDate());
        drive.setRecruitmentSchedule(req.getRecruitmentSchedule());
        drive.setVenue(req.getVenue());
        drive.setReportingTime(req.getReportingTime());
        drive.setInstructions(req.getInstructions());
        drive.setContactDetails(req.getContactDetails());
        drive.setUpdatedBy(user);

        drive = driveRepository.save(drive);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "UPDATE_PLACEMENT_DRIVE",
                "PlacementDrive",
                drive.getId().toString(),
                Map.of("driveTitle", drive.getDriveTitle()),
                null,
                null
        );

        return toDto(drive);
    }

    @Transactional
    public PlacementDriveDto publishDrive(UUID driveId, String userEmail) {
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found with id: " + driveId));

        List<JobProfile> profiles = jobProfileRepository.findByDriveId(driveId);
        if (profiles.isEmpty()) {
            throw new BadRequestException("Cannot publish drive without at least one job profile");
        }

        for (JobProfile jp : profiles) {
            if (!"PUBLISHED".equals(jp.getPolicy().getPolicyStatus())) {
                throw new BadRequestException("Job profile '" + jp.getProfileTitle() + "' has non-published policy '" + jp.getPolicy().getPolicyCode() + "'. Publish policy first.");
            }
        }

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);

        drive.setDriveStatus("PUBLISHED");
        drive.setPublishedAt(Instant.now());
        drive.setPublishedBy(user);
        drive.setUpdatedBy(user);

        drive = driveRepository.save(drive);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "PUBLISH_PLACEMENT_DRIVE",
                "PlacementDrive",
                drive.getId().toString(),
                Map.of("driveTitle", drive.getDriveTitle()),
                null,
                null
        );

        return toDto(drive);
    }

    @Transactional
    public PlacementDriveDto closeDrive(UUID driveId, String userEmail) {
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found with id: " + driveId));

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);

        drive.setDriveStatus("CLOSED");
        drive.setUpdatedBy(user);
        drive = driveRepository.save(drive);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "CLOSE_PLACEMENT_DRIVE",
                "PlacementDrive",
                drive.getId().toString(),
                Map.of("driveTitle", drive.getDriveTitle()),
                null,
                null
        );

        return toDto(drive);
    }

    @Transactional
    public PlacementDriveDto archiveDrive(UUID driveId, String userEmail) {
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found with id: " + driveId));

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);

        drive.setDriveStatus("ARCHIVED");
        drive.setUpdatedBy(user);
        drive = driveRepository.save(drive);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "ARCHIVE_PLACEMENT_DRIVE",
                "PlacementDrive",
                drive.getId().toString(),
                Map.of("driveTitle", drive.getDriveTitle()),
                null,
                null
        );

        return toDto(drive);
    }

    @Transactional(readOnly = true)
    public PlacementDriveDto getDrive(UUID driveId) {
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found with id: " + driveId));
        return toDto(drive);
    }

    @Transactional(readOnly = true)
    public PageResponse<PlacementDriveDto> searchDrives(String query, String status, String academicYear, Pageable pageable) {
        Page<PlacementDrive> page = driveRepository.searchDrives(query, status, academicYear, pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    // Job Profile operations
    @Transactional
    public JobProfileDto createJobProfile(UUID driveId, JobProfileCreateRequest req, String userEmail) {
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found with id: " + driveId));

        if ("ARCHIVED".equals(drive.getDriveStatus()) || "CLOSED".equals(drive.getDriveStatus())) {
            throw new BadRequestException("Cannot add job profile to a " + drive.getDriveStatus() + " drive");
        }

        PlacementPolicy policy = policyRepository.findById(req.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + req.getPolicyId()));

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);

        JobProfile profile = JobProfile.builder()
                .drive(drive)
                .policy(policy)
                .profileTitle(req.getProfileTitle().trim())
                .jobDescription(req.getJobDescription())
                .employmentType(req.getEmploymentType())
                .location(req.getLocation())
                .workMode(req.getWorkMode() != null ? req.getWorkMode() : "ON_SITE")
                .requiredSkills(req.getRequiredSkills())
                .totalCtcLpa(req.getTotalCtcLpa())
                .fixedPayLpa(req.getFixedPayLpa())
                .variablePayLpa(req.getVariablePayLpa())
                .retentionBonusLpa(req.getRetentionBonusLpa())
                .monthlyStipendInr(req.getMonthlyStipendInr())
                .ppoEligible(req.getPpoEligible() != null ? req.getPpoEligible() : false)
                .selectionProcess(req.getSelectionProcess())
                .createdBy(user)
                .updatedBy(user)
                .build();

        profile = jobProfileRepository.save(profile);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "CREATE_JOB_PROFILE",
                "JobProfile",
                profile.getId().toString(),
                Map.of("profileTitle", profile.getProfileTitle(), "driveTitle", drive.getDriveTitle()),
                null,
                null
        );

        return toJobProfileDto(profile);
    }

    @Transactional(readOnly = true)
    public JobProfileDto getJobProfile(UUID jobProfileId) {
        JobProfile profile = jobProfileRepository.findById(jobProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Job profile not found with id: " + jobProfileId));
        return toJobProfileDto(profile);
    }

    @Transactional(readOnly = true)
    public PageResponse<JobProfileDto> searchJobProfiles(String query, UUID driveId, String employmentType, Pageable pageable) {
        Page<JobProfile> page = jobProfileRepository.searchJobProfiles(query, driveId, employmentType, pageable);
        return PageResponse.from(page.map(this::toJobProfileDto));
    }

    public PlacementDriveDto toDto(PlacementDrive d) {
        int profileCount = (d.getJobProfiles() != null) ? d.getJobProfiles().size() : 0;
        return PlacementDriveDto.builder()
                .id(d.getId())
                .companyId(d.getCompany().getId())
                .companyName(d.getCompany().getCompanyName())
                .companyCode(d.getCompany().getCompanyCode())
                .driveTitle(d.getDriveTitle())
                .academicYear(d.getAcademicYear())
                .driveStatus(d.getDriveStatus())
                .registrationStartDate(d.getRegistrationStartDate())
                .registrationDeadline(d.getRegistrationDeadline())
                .driveDate(d.getDriveDate())
                .recruitmentSchedule(d.getRecruitmentSchedule())
                .venue(d.getVenue())
                .reportingTime(d.getReportingTime())
                .instructions(d.getInstructions())
                .contactDetails(d.getContactDetails())
                .publishedAt(d.getPublishedAt())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .jobProfilesCount(profileCount)
                .build();
    }

    public JobProfileDto toJobProfileDto(JobProfile j) {
        return JobProfileDto.builder()
                .id(j.getId())
                .driveId(j.getDrive().getId())
                .driveTitle(j.getDrive().getDriveTitle())
                .companyName(j.getDrive().getCompany().getCompanyName())
                .companyCode(j.getDrive().getCompany().getCompanyCode())
                .policyId(j.getPolicy().getId())
                .policyCode(j.getPolicy().getPolicyCode())
                .policyName(j.getPolicy().getPolicyName())
                .policyVersion(j.getPolicy().getVersionNumber())
                .profileTitle(j.getProfileTitle())
                .jobDescription(j.getJobDescription())
                .employmentType(j.getEmploymentType())
                .location(j.getLocation())
                .workMode(j.getWorkMode())
                .requiredSkills(j.getRequiredSkills())
                .totalCtcLpa(j.getTotalCtcLpa())
                .fixedPayLpa(j.getFixedPayLpa())
                .variablePayLpa(j.getVariablePayLpa())
                .retentionBonusLpa(j.getRetentionBonusLpa())
                .monthlyStipendInr(j.getMonthlyStipendInr())
                .ppoEligible(j.getPpoEligible())
                .selectionProcess(j.getSelectionProcess())
                .createdAt(j.getCreatedAt())
                .updatedAt(j.getUpdatedAt())
                .build();
    }
}
