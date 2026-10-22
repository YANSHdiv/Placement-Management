package edu.jecrc.portal.modules.academic.ops.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record StudentAttendanceSummaryDto(
        UUID studentId,
        String rollNumber,
        String studentName,
        Integer batchId,
        String batchName,
        Integer semester,
        String section,
        long totalSessions,
        long attendedSessions,
        long absentSessions,
        BigDecimal overallPercentage,
        List<SubjectAttendanceReportDto> subjectReports
) {}
