package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.FacultyProfile;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record FacultyProfileDto(
        UUID id,
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String facultyCode,
        Integer departmentId,
        String departmentName,
        String designation,
        String specialization,
        Boolean isActive,
        Instant createdAt
) {
    public static FacultyProfileDto from(FacultyProfile fp) {
        if (fp == null) return null;
        return FacultyProfileDto.builder()
                .id(fp.getId())
                .userId(fp.getUser() != null ? fp.getUser().getId() : null)
                .email(fp.getUser() != null ? fp.getUser().getEmail() : null)
                .firstName(fp.getUser() != null ? fp.getUser().getFirstName() : null)
                .lastName(fp.getUser() != null ? fp.getUser().getLastName() : null)
                .facultyCode(fp.getFacultyCode())
                .departmentId(fp.getDepartment() != null ? fp.getDepartment().getId() : null)
                .departmentName(fp.getDepartment() != null ? fp.getDepartment().getDepartmentName() : null)
                .designation(fp.getDesignation())
                .specialization(fp.getSpecialization())
                .isActive(fp.getIsActive())
                .createdAt(fp.getCreatedAt())
                .build();
    }
}
