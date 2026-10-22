package edu.jecrc.portal.modules.user.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.modules.user.dto.UserProfileResponse;
import edu.jecrc.portal.modules.user.service.UserService;
import edu.jecrc.portal.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Current authenticated user profile and scopes")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns profile details, active roles, granular named permissions, and department scopes")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse profile = userService.getMe(principal);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
