package edu.jecrc.portal.security;

import edu.jecrc.portal.modules.user.repository.UserDepartmentScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("scopeEvaluator")
@RequiredArgsConstructor
public class SecurityEvaluator {

    private final UserDepartmentScopeRepository departmentScopeRepository;

    public boolean isSelf(Authentication authentication, UUID targetUserId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        return principal.getId().equals(targetUserId);
    }

    public boolean hasDepartmentScope(Authentication authentication, Integer targetDepartmentId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        if (principal.getRoles().contains("SUPER_ADMIN") || principal.getRoles().contains("DIRECTOR")) {
            return true;
        }
        return departmentScopeRepository.findByUserId(principal.getId()).stream()
                .anyMatch(scope -> scope.getDepartment().getId().equals(targetDepartmentId));
    }
}
