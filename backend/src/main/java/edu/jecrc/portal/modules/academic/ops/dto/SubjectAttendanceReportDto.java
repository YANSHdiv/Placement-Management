package edu.jecrc.portal.modules.academic.ops.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SubjectAttendanceReportDto(
        Integer subjectId,
        String subjectCode,
        String subjectName,
        long totalSessions,
        long attendedSessions,
        long absentSessions,
        long lateSessions,
        long excusedSessions,
        BigDecimal attendancePercentage
) {}
