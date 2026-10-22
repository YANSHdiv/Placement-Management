package edu.jecrc.portal.modules.academic.ops.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record AttendanceShortageReportDto(
        UUID studentId,
        String rollNumber,
        String studentName,
        Integer batchId,
        String batchName,
        Integer semester,
        String section,
        Integer subjectId,
        String subjectCode,
        String subjectName,
        long totalSessions,
        long attendedSessions,
        BigDecimal attendancePercentage,
        BigDecimal thresholdPercentage,
        long classesNeededToReachThreshold
) {}
