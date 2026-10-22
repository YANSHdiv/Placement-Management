package edu.jecrc.portal.modules.academic.dto;

import edu.jecrc.portal.modules.user.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {
    private Integer id;
    private String departmentCode;
    private String departmentName;
    private Boolean isActive;

    public static DepartmentDto fromEntity(Department dept) {
        if (dept == null) return null;
        return DepartmentDto.builder()
                .id(dept.getId())
                .departmentCode(dept.getDepartmentCode())
                .departmentName(dept.getDepartmentName())
                .isActive(dept.getIsActive())
                .build();
    }
}
