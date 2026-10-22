package edu.jecrc.portal.modules.user.service;

import com.github.benmanes.caffeine.cache.Cache;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.auth.repository.UserSessionRepository;
import edu.jecrc.portal.modules.user.dto.*;
import edu.jecrc.portal.modules.user.entity.Department;
import edu.jecrc.portal.modules.user.entity.Role;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.entity.UserDepartmentScope;
import edu.jecrc.portal.modules.user.repository.DepartmentRepository;
import edu.jecrc.portal.modules.user.repository.RoleRepository;
import edu.jecrc.portal.modules.user.repository.UserDepartmentScopeRepository;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import edu.jecrc.portal.security.CaffeineCacheConfig;
import edu.jecrc.portal.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserDepartmentScopeRepository departmentScopeRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final Cache<UUID, CaffeineCacheConfig.UserSecuritySnapshot> userSecurityCache;

    @Transactional(readOnly = true)
    public UserProfileResponse getMe(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> ApiException.notFound("User profile not found"));

        List<UserDepartmentScope> scopes = departmentScopeRepository.findByUserId(user.getId());
        List<UserProfileResponse.DepartmentScopeDto> scopeDtos = scopes.stream()
                .map(s -> UserProfileResponse.DepartmentScopeDto.builder()
                        .departmentId(s.getDepartment().getId())
                        .departmentCode(s.getDepartment().getDepartmentCode())
                        .departmentName(s.getDepartment().getDepartmentName())
                        .isPrimary(Boolean.TRUE.equals(s.getIsPrimary()))
                        .build())
                .collect(Collectors.toList());

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .active(Boolean.TRUE.equals(user.getIsActive()))
                .suspended(Boolean.TRUE.equals(user.getIsSuspended()))
                .roles(principal.getRoles())
                .permissions(principal.getPermissions())
                .departmentScopes(scopeDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchUsers(String search, String roleName, Pageable pageable) {
        Page<User> page = userRepository.searchUsers(search, roleName, pageable);
        return PageResponse.from(page.map(UserResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request, UUID actorId, String actorEmail, HttpServletRequest httpRequest) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw ApiException.conflict("A user with this university email already exists: " + request.getEmail());
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByRoleName(roleName.toUpperCase())
                    .orElseThrow(() -> ApiException.badRequest("Unknown role: " + roleName));
            roles.add(role);
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .isSuspended(false)
                .securityStamp(UUID.randomUUID())
                .roles(roles)
                .build();

        user = userRepository.save(user);

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> ApiException.badRequest("Invalid department ID: " + request.getDepartmentId()));

            UserDepartmentScope scope = UserDepartmentScope.builder()
                    .user(user)
                    .department(dept)
                    .isPrimary(true)
                    .build();
            departmentScopeRepository.save(scope);
        }

        Map<String, Object> details = Map.of(
                "createdUserId", user.getId().toString(),
                "createdEmail", user.getEmail(),
                "assignedRoles", request.getRoles()
        );
        auditService.logSync(actorId, actorEmail, "USER_CREATED", "User", user.getId().toString(), details, httpRequest);

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request, UUID actorId, String actorEmail, HttpServletRequest httpRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User not found: " + id));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);

        Map<String, Object> details = Map.of(
                "updatedFields", Map.of("firstName", request.getFirstName(), "lastName", request.getLastName())
        );
        auditService.logSync(actorId, actorEmail, "USER_UPDATED", "User", user.getId().toString(), details, httpRequest);

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse toggleSuspension(UUID id, boolean suspend, UUID actorId, String actorEmail, HttpServletRequest httpRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User not found: " + id));

        user.setIsSuspended(suspend);
        user.setSecurityStamp(UUID.randomUUID()); // Stamp invalidates all active access tokens
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);

        if (suspend) {
            // Revoke all existing sessions in DB
            userSessionRepository.revokeAllUserSessions(user.getId());
        }

        // Synchronous cache eviction for instantaneous zero-delay revocation across all request threads
        userSecurityCache.invalidate(user.getId());

        String action = suspend ? "USER_SUSPENDED" : "USER_ACTIVATED";
        Map<String, Object> details = Map.of("targetUserEmail", user.getEmail(), "suspended", suspend);
        auditService.logSync(actorId, actorEmail, action, "User", user.getId().toString(), details, httpRequest);

        log.info("User {} suspension status toggled to {} by {}", user.getEmail(), suspend, actorEmail);
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse assignRoles(UUID id, Set<String> roleNames, UUID actorId, String actorEmail, HttpServletRequest httpRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User not found: " + id));

        Set<Role> roles = new HashSet<>();
        for (String r : roleNames) {
            Role role = roleRepository.findByRoleName(r.toUpperCase())
                    .orElseThrow(() -> ApiException.badRequest("Invalid role name: " + r));
            roles.add(role);
        }

        user.setRoles(roles);
        user.setSecurityStamp(UUID.randomUUID()); // Invalidate existing access tokens to apply new authorities
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);

        userSecurityCache.invalidate(user.getId());

        Map<String, Object> details = Map.of("assignedRoles", roleNames);
        auditService.logSync(actorId, actorEmail, "ROLES_ASSIGNED", "User", user.getId().toString(), details, httpRequest);

        return UserResponse.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
