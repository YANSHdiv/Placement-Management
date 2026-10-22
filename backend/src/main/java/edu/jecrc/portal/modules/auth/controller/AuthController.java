package edu.jecrc.portal.modules.auth.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.modules.auth.dto.*;
import edu.jecrc.portal.modules.auth.service.AuthService;
import edu.jecrc.portal.security.CookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, Refresh Token Rotation, Session Revocation and Password Recovery")
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Returns short-lived in-memory access token and sets HttpOnly refresh cookie")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletRequest httpRequest,
                                                           HttpServletResponse httpResponse) {
        AuthResponse response = authService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token", description = "Exchanges unconsumed refresh cookie for new access token and rotated refresh cookie. Detects replay attacks.")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(HttpServletRequest httpRequest,
                                                                     HttpServletResponse httpResponse) {
        String rawRefreshToken = extractCookie(httpRequest, cookieService.getCookieName());
        RefreshTokenResponse response = authService.refreshToken(rawRefreshToken, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revokes refresh token and active session in database and clears HttpOnly cookie")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest,
                                                    HttpServletResponse httpResponse) {
        String rawRefreshToken = extractCookie(httpRequest, cookieService.getCookieName());
        authService.logout(rawRefreshToken, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Generates a 15-minute cryptographically secure reset token")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                            HttpServletRequest httpRequest) {
        authService.forgotPassword(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("If the email is registered, password reset instructions have been issued", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Verifies reset token, updates password, revokes all sessions, and evicts security cache")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
                                                           HttpServletRequest httpRequest) {
        authService.resetPassword(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. You may now log in with your new password.", null));
    }

    private String extractCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
