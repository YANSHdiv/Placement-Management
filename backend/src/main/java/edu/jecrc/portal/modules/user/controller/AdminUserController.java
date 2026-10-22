package edu.jecrc.portal.modules.user.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.modules.user.dto.*;
import edu.jecrc.portal.modules.user.service.UserService;
import edu.jecrc.portal.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "User provisioning, activation, suspension, and role/permission assignment")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Search user accounts", description = "Paginated directory with search across name, email, and role filters")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<UserResponse> users = userService.searchUsers(search, role, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Create new user account", description = "Provisions a new user with assigned roles and optional department scope")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal UserPrincipal actor,
            HttpServletRequest httpRequest) {

        UserResponse user = userService.createUser(request, actor.getId(), actor.getEmail(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User created successfully", user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Update user details")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal actor,
            HttpServletRequest httpRequest) {

        UserResponse user = userService.updateUser(id, request, actor.getId(), actor.getEmail(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @PatchMapping("/{id}/suspension")
    @PreAuthorize("hasAuthority('USER_SUSPEND')")
    @Operation(summary = "Suspend or activate user account", description = "Immediately invalidates all existing sessions, revokes access tokens via security stamp, and evicts security cache")
    public ResponseEntity<ApiResponse<UserResponse>> toggleSuspension(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> payload,
            @AuthenticationPrincipal UserPrincipal actor,
            HttpServletRequest httpRequest) {

        boolean suspend = payload.getOrDefault("suspended", true);
        UserResponse user = userService.toggleSuspension(id, suspend, actor.getId(), actor.getEmail(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Account status updated successfully", user));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    @Operation(summary = "Assign roles to user", description = "Updates role assignments and invalidates current access tokens to enforce new permission matrix")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRolesRequest request,
            @AuthenticationPrincipal UserPrincipal actor,
            HttpServletRequest httpRequest) {

        UserResponse user = userService.assignRoles(id, request.getRoles(), actor.getId(), actor.getEmail(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Roles updated successfully", user));
    }

    @GetMapping("/roles/catalog")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN') or hasAuthority('USER_MANAGE')")
    @Operation(summary = "List all available roles and permissions catalog")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = userService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }
}
