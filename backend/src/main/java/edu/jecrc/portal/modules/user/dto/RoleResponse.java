package edu.jecrc.portal.modules.user.dto;

import edu.jecrc.portal.modules.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Integer id;
    private String roleName;
    private String description;
    private boolean systemRole;
    private Set<String> permissions;

    public static RoleResponse fromEntity(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .systemRole(Boolean.TRUE.equals(role.getIsSystemRole()))
                .permissions(role.getPermissions().stream().map(p -> p.getPermissionName()).collect(Collectors.toSet()))
                .build();
    }
}
