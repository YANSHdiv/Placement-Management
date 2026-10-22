package edu.jecrc.portal.modules.dashboard.service;

import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.dashboard.dto.PlacementAnalyticsDto;
import edu.jecrc.portal.modules.offer.entity.PlacementOffer;
import edu.jecrc.portal.modules.offer.repository.PlacementOfferRepository;
import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.entity.JobApplication;
import edu.jecrc.portal.modules.placement.repository.JobApplicationRepository;
import edu.jecrc.portal.modules.user.entity.Department;
import edu.jecrc.portal.modules.user.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlacementAnalyticsService {

    private final StudentRepository studentRepository;
    private final JobApplicationRepository applicationRepository;
    private final PlacementOfferRepository offerRepository;
    private final DepartmentRepository departmentRepository;

    private static final BigDecimal REGULAR_LIMIT = BigDecimal.valueOf(10.00);
    private static final BigDecimal DREAM_LIMIT = BigDecimal.valueOf(25.00);
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneId.systemDefault());

    @Transactional(readOnly = true)
    public PlacementAnalyticsDto getPlacementAnalytics(Integer departmentId, UUID batchId, String academicYear) {
        // 1. Fetch Students in scope
        List<Student> allStudents = studentRepository.findAll();
        List<Student> scopedStudents = allStudents.stream()
                .filter(s -> departmentId == null || (s.getDepartment() != null && s.getDepartment().getId().equals(departmentId)))
                .filter(s -> batchId == null || (s.getBatch() != null && s.getBatch().getId().equals(batchId)))
                .collect(Collectors.toList());

        Set<UUID> scopedStudentIds = scopedStudents.stream().map(Student::getId).collect(Collectors.toSet());

        // 2. Fetch Applications in scope
        List<JobApplication> allApps = applicationRepository.findAll();
        List<JobApplication> scopedApps = allApps.stream()
                .filter(a -> scopedStudentIds.contains(a.getStudent().getId()))
                .collect(Collectors.toList());

        // 3. Fetch Offers in scope
        List<PlacementOffer> allOffers = offerRepository.findAll();
        List<PlacementOffer> scopedOffers = allOffers.stream()
                .filter(o -> scopedStudentIds.contains(o.getStudent().getId()))
                .collect(Collectors.toList());

        // Metrics Computation
        long totalStudents = scopedStudents.size();
        long eligibleStudents = scopedStudents.stream()
                .filter(s -> "VERIFIED".equalsIgnoreCase(s.getVerificationStatus()))
                .filter(s -> s.getCgpa() != null && s.getCgpa().compareTo(BigDecimal.valueOf(6.00)) >= 0)
                .filter(s -> s.getActiveBacklogs() == null || s.getActiveBacklogs() == 0)
                .count();

        long participatingStudents = scopedApps.stream()
                .map(a -> a.getStudent().getId())
                .distinct()
                .count();

        long selectedCandidates = scopedApps.stream()
                .filter(a -> "SELECTED".equalsIgnoreCase(a.getApplicationStatus()) || "PLACED".equalsIgnoreCase(a.getApplicationStatus()))
                .map(a -> a.getStudent().getId())
                .distinct()
                .count();

        List<PlacementOffer> validIssuedOffers = scopedOffers.stream()
                .filter(o -> List.of("ISSUED", "ACCEPTED", "DECLINED", "EXPIRED").contains(o.getOfferStatus().toUpperCase()))
                .collect(Collectors.toList());
        long totalOffersIssued = validIssuedOffers.size();

        List<PlacementOffer> acceptedOffers = scopedOffers.stream()
                .filter(o -> "ACCEPTED".equalsIgnoreCase(o.getOfferStatus()))
                .collect(Collectors.toList());

        long uniqueStudentsPlaced = acceptedOffers.stream()
                .map(o -> o.getStudent().getId())
                .distinct()
                .count();

        double placementRateOnEligible = eligibleStudents > 0
                ? Math.round(((double) uniqueStudentsPlaced / eligibleStudents) * 10000.0) / 100.0
                : (totalStudents > 0 ? Math.round(((double) uniqueStudentsPlaced / totalStudents) * 10000.0) / 100.0 : 0.0);

        double placementRateOnTotal = totalStudents > 0
                ? Math.round(((double) uniqueStudentsPlaced / totalStudents) * 10000.0) / 100.0
                : 0.0;

        // Compensation calculations over accepted offers (fallback to valid issued offers if none accepted yet)
        List<PlacementOffer> salaryPool = acceptedOffers.stream().filter(o -> o.getTotalCtcLpa() != null).toList();
        if (salaryPool.isEmpty()) {
            salaryPool = validIssuedOffers.stream().filter(o -> o.getTotalCtcLpa() != null).toList();
        }

        BigDecimal highestCtc = null;
        BigDecimal lowestCtc = null;
        BigDecimal avgCtc = null;
        BigDecimal medianCtc = null;

        if (!salaryPool.isEmpty()) {
            List<BigDecimal> sortedSalaries = salaryPool.stream()
                    .map(PlacementOffer::getTotalCtcLpa)
                    .sorted()
                    .collect(Collectors.toList());

            highestCtc = sortedSalaries.get(sortedSalaries.size() - 1);
            lowestCtc = sortedSalaries.get(0);

            BigDecimal sum = sortedSalaries.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            avgCtc = sum.divide(BigDecimal.valueOf(sortedSalaries.size()), 2, RoundingMode.HALF_UP);

            int size = sortedSalaries.size();
            if (size % 2 == 1) {
                medianCtc = sortedSalaries.get(size / 2);
            } else {
                medianCtc = sortedSalaries.get(size / 2 - 1).add(sortedSalaries.get(size / 2))
                        .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            }
        }

        // Tier Breakdown
        long regularCount = 0;
        long dreamCount = 0;
        long superDreamCount = 0;

        List<PlacementOffer> tierSource = !acceptedOffers.isEmpty() ? acceptedOffers : validIssuedOffers;
        for (PlacementOffer off : tierSource) {
            if (off.getTotalCtcLpa() != null) {
                if (off.getTotalCtcLpa().compareTo(REGULAR_LIMIT) < 0) {
                    regularCount++;
                } else if (off.getTotalCtcLpa().compareTo(DREAM_LIMIT) < 0) {
                    dreamCount++;
                } else {
                    superDreamCount++;
                }
            }
        }

        // Department breakdown
        List<Department> allDepts = departmentRepository.findAll();
        List<PlacementAnalyticsDto.DepartmentAnalyticsDto> deptStats = new ArrayList<>();

        for (Department dept : allDepts) {
            if (departmentId != null && !dept.getId().equals(departmentId)) {
                continue;
            }

            List<Student> dStudents = allStudents.stream()
                    .filter(s -> s.getDepartment() != null && s.getDepartment().getId().equals(dept.getId()))
                    .filter(s -> batchId == null || (s.getBatch() != null && s.getBatch().getId().equals(batchId)))
                    .collect(Collectors.toList());

            Set<UUID> dStudentIds = dStudents.stream().map(Student::getId).collect(Collectors.toSet());

            long dTotal = dStudents.size();
            long dEligible = dStudents.stream()
                    .filter(s -> "VERIFIED".equalsIgnoreCase(s.getVerificationStatus()))
                    .filter(s -> s.getCgpa() != null && s.getCgpa().compareTo(BigDecimal.valueOf(6.00)) >= 0)
                    .filter(s -> s.getActiveBacklogs() == null || s.getActiveBacklogs() == 0)
                    .count();

            List<PlacementOffer> dAcceptedOffers = allOffers.stream()
                    .filter(o -> dStudentIds.contains(o.getStudent().getId()))
                    .filter(o -> "ACCEPTED".equalsIgnoreCase(o.getOfferStatus()))
                    .collect(Collectors.toList());

            long dPlaced = dAcceptedOffers.stream().map(o -> o.getStudent().getId()).distinct().count();

            double dRate = dEligible > 0
                    ? Math.round(((double) dPlaced / dEligible) * 10000.0) / 100.0
                    : (dTotal > 0 ? Math.round(((double) dPlaced / dTotal) * 10000.0) / 100.0 : 0.0);

            BigDecimal dHighest = dAcceptedOffers.stream()
                    .map(PlacementOffer::getTotalCtcLpa)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(null);

            BigDecimal dAvg = null;
            List<BigDecimal> dCtcs = dAcceptedOffers.stream()
                    .map(PlacementOffer::getTotalCtcLpa)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!dCtcs.isEmpty()) {
                dAvg = dCtcs.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(dCtcs.size()), 2, RoundingMode.HALF_UP);
            }

            deptStats.add(PlacementAnalyticsDto.DepartmentAnalyticsDto.builder()
                    .departmentId(dept.getId())
                    .departmentCode(dept.getDepartmentCode())
                    .departmentName(dept.getDepartmentName())
                    .totalStudents(dTotal)
                    .eligibleStudents(dEligible)
                    .placedStudents(dPlaced)
                    .placementRate(dRate)
                    .highestCtc(dHighest)
                    .averageCtc(dAvg)
                    .build());
        }

        // Top Recruiters
        Map<Company, List<PlacementOffer>> byCompany = validIssuedOffers.stream()
                .filter(o -> o.getCompany() != null)
                .collect(Collectors.groupingBy(PlacementOffer::getCompany));

        List<PlacementAnalyticsDto.TopRecruiterAnalyticsDto> topRecruiters = byCompany.entrySet().stream()
                .map(entry -> {
                    Company company = entry.getKey();
                    List<PlacementOffer> cOffers = entry.getValue();
                    long offCount = cOffers.size();
                    long accCount = cOffers.stream().filter(o -> "ACCEPTED".equalsIgnoreCase(o.getOfferStatus())).count();

                    BigDecimal cHighest = cOffers.stream()
                            .map(PlacementOffer::getTotalCtcLpa)
                            .filter(Objects::nonNull)
                            .max(BigDecimal::compareTo)
                            .orElse(null);

                    List<BigDecimal> ctcs = cOffers.stream()
                            .map(PlacementOffer::getTotalCtcLpa)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    BigDecimal cAvg = ctcs.isEmpty() ? null : ctcs.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(ctcs.size()), 2, RoundingMode.HALF_UP);

                    return PlacementAnalyticsDto.TopRecruiterAnalyticsDto.builder()
                            .companyName(company.getCompanyName())
                            .industry(company.getIndustry())
                            .offersCount(offCount)
                            .acceptedCount(accCount)
                            .highestCtc(cHighest)
                            .averageCtc(cAvg)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getOffersCount(), a.getOffersCount()))
                .limit(10)
                .collect(Collectors.toList());

        // Monthly timeline
        Map<String, List<PlacementOffer>> byMonth = validIssuedOffers.stream()
                .filter(o -> o.getIssuanceDate() != null)
                .collect(Collectors.groupingBy(o -> MONTH_FORMATTER.format(o.getIssuanceDate())));

        List<PlacementAnalyticsDto.MonthlyPlacementStatDto> monthlyTimeline = byMonth.entrySet().stream()
                .map(entry -> PlacementAnalyticsDto.MonthlyPlacementStatDto.builder()
                        .monthLabel(entry.getKey())
                        .offersIssued((long) entry.getValue().size())
                        .offersAccepted(entry.getValue().stream().filter(o -> "ACCEPTED".equalsIgnoreCase(o.getOfferStatus())).count())
                        .build())
                .sorted(Comparator.comparing(PlacementAnalyticsDto.MonthlyPlacementStatDto::getMonthLabel))
                .collect(Collectors.toList());

        String scopeDesc = departmentId != null
                ? "Department-Specific Analytics [Dept ID: " + departmentId + "]"
                : "Institution-Wide Placement Analytics (All Departments)";

        return PlacementAnalyticsDto.builder()
                .scopeDescription(scopeDesc)
                .departmentId(departmentId)
                .departmentCode(departmentId != null ? departmentRepository.findById(departmentId).map(Department::getDepartmentCode).orElse(null) : null)
                .academicYear(academicYear != null ? academicYear : "2025-2026")
                .totalStudents(totalStudents)
                .eligibleStudents(eligibleStudents)
                .participatingStudents(participatingStudents)
                .selectedCandidates(selectedCandidates)
                .totalOffersIssued(totalOffersIssued)
                .uniqueStudentsPlaced(uniqueStudentsPlaced)
                .placementRateOnEligible(placementRateOnEligible)
                .placementRateOnTotal(placementRateOnTotal)
                .compensation(PlacementAnalyticsDto.CompensationMetricsDto.builder()
                        .highestCtc(highestCtc)
                        .averageCtc(avgCtc)
                        .medianCtc(medianCtc)
                        .lowestCtc(lowestCtc)
                        .build())
                .tierDistribution(PlacementAnalyticsDto.TierDistributionDto.builder()
                        .regularCount(regularCount)
                        .dreamCount(dreamCount)
                        .superDreamCount(superDreamCount)
                        .build())
                .departmentBreakdown(deptStats)
                .topRecruiters(topRecruiters)
                .monthlyTimeline(monthlyTimeline)
                .policyNotes("Internal placement intelligence: Metrics distinguish selected candidates (round outcomes) from formal offers issued. Unique placed students counts each student exactly once based on their accepted offer, preventing multiple-offer double-counting. Note: Tier classifications (Regular, Dream, Super Dream) and compensation cutoffs are determined by the active institutional policy configuration and require formal university confirmation prior to production publication.")
                .build();
    }
}
