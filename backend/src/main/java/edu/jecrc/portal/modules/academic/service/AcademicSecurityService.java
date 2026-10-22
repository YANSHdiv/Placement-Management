package edu.jecrc.portal.modules.academic.service;

import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.user.entity.Role;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.entity.UserDepartmentScope;
import edu.jecrc.portal.modules.user.repository.UserDepartmentScopeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicSecurityService {

    private final UserDepartmentScopeRepository departmentScopeRepository;

    public void validateStudentAccess(User caller, Student student) {
        if (caller == null) {
            throw new AccessDeniedException("Unauthenticated request");
        }

        Set<String> roleNames = caller.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        // 1. Super Admin, TPO Admin, Director have global institutional access
        if (roleNames.contains("SUPER_ADMIN") || roleNames.contains("TPO_ADMIN") || roleNames.contains("DIRECTOR")) {
            return;
        }

        // 2. Student can ONLY access their own records
        if (roleNames.contains("STUDENT")) {
            boolean isOwn = (student.getUser() != null && student.getUser().getId().equals(caller.getId()))
                    || student.getUniversityEmail().equalsIgnoreCase(caller.getEmail());

            if (!isOwn) {
                log.warn("SECURITY ALERT: Student {} attempted unauthorized access to student profile {}",
                        caller.getEmail(), student.getId());
                throw new AccessDeniedException("Access Denied: You may only view or manage your own academic profile.");
            }
            return;
        }

        // 3. HOD or Faculty must be scoped to the student's department
        if (roleNames.contains("HOD") || roleNames.contains("FACULTY")) {
            List<Integer> scopedDeptIds = getScopedDepartmentIds(caller.getId());
            if (student.getDepartment() == null || !scopedDeptIds.contains(student.getDepartment().getId())) {
                log.warn("SECURITY ALERT: User {} with department scope attempted access to student {} outside assigned department",
                        caller.getEmail(), student.getId());
                throw new AccessDeniedException("Access Denied: Student belongs to a department outside your assigned departmental scope.");
            }
            return;
        }

        // Any other role without explicit permission
        throw new AccessDeniedException("Access Denied: Insufficient academic permissions.");
    }

    public void validateVerificationPrivilege(User caller, Student student) {
        if (caller == null) {
            throw new AccessDeniedException("Unauthenticated request");
        }

        Set<String> roleNames = caller.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        if (roleNames.contains("SUPER_ADMIN") || roleNames.contains("TPO_ADMIN")) {
            return;
        }

        if (roleNames.contains("HOD")) {
            List<Integer> scopedDeptIds = getScopedDepartmentIds(caller.getId());
            if (student.getDepartment() == null || !scopedDeptIds.contains(student.getDepartment().getId())) {
                throw new AccessDeniedException("Access Denied: You can only verify records for students in your assigned department.");
            }
            return;
        }

        throw new AccessDeniedException("Access Denied: Only authorized administrators and HODs may verify academic records.");
    }

    public List<Integer> getScopedDepartmentIds(UUID userId) {
        return departmentScopeRepository.findByUserId(userId).stream()
                .map(s -> s.getDepartment().getId())
                .collect(Collectors.toList());
    }

    public boolean hasGlobalAcademicAccess(User caller) {
        if (caller == null) return false;
        Set<String> roles = caller.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet());
        return roles.contains("SUPER_ADMIN") || roles.contains("TPO_ADMIN") || roles.contains("DIRECTOR");
    }
}
