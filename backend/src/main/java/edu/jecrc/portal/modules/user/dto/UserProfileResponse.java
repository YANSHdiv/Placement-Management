package edu.jecrc.portal.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private boolean active;
    private boolean suspended;
    private Set<String> roles;
    private Set<String> permissions;
    private List<DepartmentScopeDto> departmentScopes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentScopeDto {
        private Integer departmentId;
        private String departmentCode;
        private String departmentName;
        private boolean isPrimary;
    }
}
