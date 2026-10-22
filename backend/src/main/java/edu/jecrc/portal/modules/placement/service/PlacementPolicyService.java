package edu.jecrc.portal.modules.placement.service;

import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.BadRequestException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import edu.jecrc.portal.modules.academic.entity.AcademicProgram;
import edu.jecrc.portal.modules.academic.repository.AcademicBatchRepository;
import edu.jecrc.portal.modules.academic.repository.AcademicProgramRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.placement.dto.PlacementPolicyCreateRequest;
import edu.jecrc.portal.modules.placement.dto.PlacementPolicyDto;
import edu.jecrc.portal.modules.placement.entity.PlacementPolicy;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlacementPolicyService {

    private final PlacementPolicyRepository policyRepository;
    private final AcademicProgramRepository programRepository;
    private final AcademicBatchRepository batchRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public PlacementPolicyDto createPolicy(PlacementPolicyCreateRequest req, String userEmail) {
        String code = req.getPolicyCode().trim().toUpperCase();
        if (policyRepository.findByPolicyCodeAndVersionNumber(code, 1).isPresent()) {
            throw new BadRequestException("Policy with code '" + code + "' version 1 already exists. Use new-version to iterate.");
        }

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);

        Set<AcademicProgram> programs = resolvePrograms(req.getEligibleProgramIds());
        Set<AcademicBatch> batches = resolveBatches(req.getEligibleBatchIds());

        PlacementPolicy policy = PlacementPolicy.builder()
                .policyCode(code)
                .policyName(req.getPolicyName().trim())
                .versionNumber(1)
                .policyStatus("DRAFT")
                .minCgpa(req.getMinCgpa())
                .minClass10Percentage(req.getMinClass10Percentage())
                .minClass12Percentage(req.getMinClass12Percentage())
                .minDiplomaPercentage(req.getMinDiplomaPercentage())
                .maxActiveBacklogs(req.getMaxActiveBacklogs() != null ? req.getMaxActiveBacklogs() : 0)
                .maxHistoricalBacklogs(req.getMaxHistoricalBacklogs() != null ? req.getMaxHistoricalBacklogs() : 0)
                .allowAcademicGaps(req.getAllowAcademicGaps() != null ? req.getAllowAcademicGaps() : true)
                .maxAcademicGapYears(req.getMaxAcademicGapYears() != null ? req.getMaxAcademicGapYears() : 0)
                .requiresAllQualificationsVerified(req.getRequiresAllQualificationsVerified() != null ? req.getRequiresAllQualificationsVerified() : true)
                .requiresCgpaVerified(req.getRequiresCgpaVerified() != null ? req.getRequiresCgpaVerified() : true)
                .criteriaPayloadJson(req.getCriteriaPayloadJson())
                .eligiblePrograms(programs)
                .eligibleBatches(batches)
                .createdBy(user)
                .updatedBy(user)
                .build();

        policy = policyRepository.save(policy);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "CREATE_PLACEMENT_POLICY",
                "PlacementPolicy",
                policy.getId().toString(),
                Map.of("policyCode", code, "version", 1),
                null,
                null
        );

        return toDto(policy);
    }

    @Transactional
    public PlacementPolicyDto publishPolicy(UUID policyId, String userEmail) {
        PlacementPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Placement policy not found with id: " + policyId));

        if ("PUBLISHED".equals(policy.getPolicyStatus())) {
            return toDto(policy);
        }

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);

        policy.setPolicyStatus("PUBLISHED");
        policy.setPublishedAt(Instant.now());
        policy.setPublishedBy(user);
        policy.setUpdatedBy(user);

        policy = policyRepository.save(policy);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "PUBLISH_PLACEMENT_POLICY",
                "PlacementPolicy",
                policy.getId().toString(),
                Map.of("policyCode", policy.getPolicyCode(), "version", policy.getVersionNumber()),
                null,
                null
        );

        return toDto(policy);
    }

    @Transactional
    public PlacementPolicyDto createNewVersion(UUID basePolicyId, PlacementPolicyCreateRequest req, String userEmail) {
        PlacementPolicy basePolicy = policyRepository.findById(basePolicyId)
                .orElseThrow(() -> new ResourceNotFoundException("Base policy not found with id: " + basePolicyId));

        String code = basePolicy.getPolicyCode();
        Integer maxVersion = policyRepository.findMaxVersionNumberByPolicyCode(code);
        int nextVersion = (maxVersion != null ? maxVersion : basePolicy.getVersionNumber()) + 1;

        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);

        Set<AcademicProgram> programs = (req.getEligibleProgramIds() != null && !req.getEligibleProgramIds().isEmpty())
                ? resolvePrograms(req.getEligibleProgramIds())
                : new HashSet<>(basePolicy.getEligiblePrograms());

        Set<AcademicBatch> batches = (req.getEligibleBatchIds() != null && !req.getEligibleBatchIds().isEmpty())
                ? resolveBatches(req.getEligibleBatchIds())
                : new HashSet<>(basePolicy.getEligibleBatches());

        PlacementPolicy newVersionPolicy = PlacementPolicy.builder()
                .policyCode(code)
                .policyName(req.getPolicyName() != null ? req.getPolicyName().trim() : basePolicy.getPolicyName())
                .versionNumber(nextVersion)
                .policyStatus("DRAFT")
                .minCgpa(req.getMinCgpa() != null ? req.getMinCgpa() : basePolicy.getMinCgpa())
                .minClass10Percentage(req.getMinClass10Percentage() != null ? req.getMinClass10Percentage() : basePolicy.getMinClass10Percentage())
                .minClass12Percentage(req.getMinClass12Percentage() != null ? req.getMinClass12Percentage() : basePolicy.getMinClass12Percentage())
                .minDiplomaPercentage(req.getMinDiplomaPercentage() != null ? req.getMinDiplomaPercentage() : basePolicy.getMinDiplomaPercentage())
                .maxActiveBacklogs(req.getMaxActiveBacklogs() != null ? req.getMaxActiveBacklogs() : basePolicy.getMaxActiveBacklogs())
                .maxHistoricalBacklogs(req.getMaxHistoricalBacklogs() != null ? req.getMaxHistoricalBacklogs() : basePolicy.getMaxHistoricalBacklogs())
                .allowAcademicGaps(req.getAllowAcademicGaps() != null ? req.getAllowAcademicGaps() : basePolicy.getAllowAcademicGaps())
                .maxAcademicGapYears(req.getMaxAcademicGapYears() != null ? req.getMaxAcademicGapYears() : basePolicy.getMaxAcademicGapYears())
                .requiresAllQualificationsVerified(req.getRequiresAllQualificationsVerified() != null ? req.getRequiresAllQualificationsVerified() : basePolicy.getRequiresAllQualificationsVerified())
                .requiresCgpaVerified(req.getRequiresCgpaVerified() != null ? req.getRequiresCgpaVerified() : basePolicy.getRequiresCgpaVerified())
                .criteriaPayloadJson(req.getCriteriaPayloadJson() != null ? req.getCriteriaPayloadJson() : basePolicy.getCriteriaPayloadJson())
                .eligiblePrograms(programs)
                .eligibleBatches(batches)
                .createdBy(user)
                .updatedBy(user)
                .build();

        newVersionPolicy = policyRepository.save(newVersionPolicy);

        auditService.logAsync(
                user != null ? user.getId() : null,
                userEmail,
                "CREATE_POLICY_VERSION",
                "PlacementPolicy",
                newVersionPolicy.getId().toString(),
                Map.of("policyCode", code, "version", nextVersion, "parentPolicyId", basePolicyId.toString()),
                null,
                null
        );

        return toDto(newVersionPolicy);
    }

    @Transactional(readOnly = true)
    public PlacementPolicyDto getPolicy(UUID policyId) {
        PlacementPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Placement policy not found with id: " + policyId));
        return toDto(policy);
    }

    @Transactional(readOnly = true)
    public PageResponse<PlacementPolicyDto> searchPolicies(String query, String status, Pageable pageable) {
        Page<PlacementPolicy> page = policyRepository.searchPolicies(query, status, pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    private Set<AcademicProgram> resolvePrograms(List<Integer> programIds) {
        if (programIds == null || programIds.isEmpty()) return new HashSet<>();
        return new HashSet<>(programRepository.findAllById(programIds));
    }

    private Set<AcademicBatch> resolveBatches(List<Integer> batchIds) {
        if (batchIds == null || batchIds.isEmpty()) return new HashSet<>();
        return new HashSet<>(batchRepository.findAllById(batchIds));
    }

    public PlacementPolicyDto toDto(PlacementPolicy p) {
        List<Integer> progIds = p.getEligiblePrograms().stream().map(AcademicProgram::getId).toList();
        List<String> progNames = p.getEligiblePrograms().stream().map(AcademicProgram::getProgramName).toList();
        List<Integer> batchIds = p.getEligibleBatches().stream().map(AcademicBatch::getId).toList();
        List<String> batchNames = p.getEligibleBatches().stream().map(AcademicBatch::getBatchName).toList();

        return PlacementPolicyDto.builder()
                .id(p.getId())
                .policyCode(p.getPolicyCode())
                .policyName(p.getPolicyName())
                .versionNumber(p.getVersionNumber())
                .policyStatus(p.getPolicyStatus())
                .minCgpa(p.getMinCgpa())
                .minClass10Percentage(p.getMinClass10Percentage())
                .minClass12Percentage(p.getMinClass12Percentage())
                .minDiplomaPercentage(p.getMinDiplomaPercentage())
                .maxActiveBacklogs(p.getMaxActiveBacklogs())
                .maxHistoricalBacklogs(p.getMaxHistoricalBacklogs())
                .allowAcademicGaps(p.getAllowAcademicGaps())
                .maxAcademicGapYears(p.getMaxAcademicGapYears())
                .requiresAllQualificationsVerified(p.getRequiresAllQualificationsVerified())
                .requiresCgpaVerified(p.getRequiresCgpaVerified())
                .criteriaPayloadJson(p.getCriteriaPayloadJson())
                .eligibleProgramIds(progIds)
                .eligibleProgramNames(progNames)
                .eligibleBatchIds(batchIds)
                .eligibleBatchNames(batchNames)
                .publishedAt(p.getPublishedAt())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
