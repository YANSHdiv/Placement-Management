package edu.jecrc.portal.modules.auth.service;

import com.github.benmanes.caffeine.cache.Cache;
import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.common.exception.TokenReplayBreachException;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.auth.dto.*;
import edu.jecrc.portal.modules.auth.entity.PasswordResetToken;
import edu.jecrc.portal.modules.auth.entity.RefreshToken;
import edu.jecrc.portal.modules.auth.entity.UserSession;
import edu.jecrc.portal.modules.auth.repository.PasswordResetTokenRepository;
import edu.jecrc.portal.modules.auth.repository.RefreshTokenRepository;
import edu.jecrc.portal.modules.auth.repository.UserSessionRepository;
import edu.jecrc.portal.modules.user.dto.UserProfileResponse;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import edu.jecrc.portal.modules.user.service.UserService;
import edu.jecrc.portal.security.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final UserService userService;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final Cache<UUID, CaffeineCacheConfig.UserSecuritySnapshot> userSecurityCache;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String email = request.getEmail().toLowerCase().trim();
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (Exception ex) {
            auditService.logSync(null, email, "LOGIN_FAILED", "User", null,
                    Map.of("reason", ex.getMessage()), httpRequest);
            throw ex;
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> ApiException.unauthorized("User record not found"));

        if (Boolean.TRUE.equals(user.getIsSuspended())) {
            throw ApiException.forbidden("Your account has been suspended. Please contact the administrator.");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw ApiException.forbidden("Your account is inactive.");
        }

        UUID familyId = UUID.randomUUID();
        UserSession session = UserSession.builder()
                .user(user)
                .familyId(familyId)
                .ipAddress(AuditService.getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .isRevoked(false)
                .build();
        session = userSessionRepository.save(session);

        String rawRefreshToken = jwtService.generateOpaqueToken();
        String tokenHash = jwtService.hashToken(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .session(session)
                .familyId(familyId)
                .tokenHash(tokenHash)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        String accessToken = jwtService.generateAccessToken(principal);

        ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(rawRefreshToken, refreshTokenExpirationMs);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        UserProfileResponse profile = userService.getMe(principal);

        auditService.logSync(user.getId(), user.getEmail(), "LOGIN_SUCCESS", "User", user.getId().toString(),
                Map.of("sessionId", session.getId().toString()), httpRequest);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresInMs(accessTokenExpirationMs)
                .user(profile)
                .build();
    }

    @Transactional(noRollbackFor = TokenReplayBreachException.class)
    public RefreshTokenResponse refreshToken(String rawRefreshToken, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw ApiException.unauthorized("Refresh token is missing");
        }

        String tokenHash = jwtService.hashToken(rawRefreshToken);
        RefreshToken currentToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> ApiException.unauthorized("Invalid refresh token"));

        UUID familyId = currentToken.getFamilyId();
        UserSession session = currentToken.getSession();

        // 1. REPLAY DETECTION: Token already consumed!
        if (currentToken.isConsumed()) {
            log.error("SECURITY ALERT: Refresh token replay attack detected on family {} for user {}",
                    familyId, session.getUser().getEmail());

            // Immediate penalty: Revoke entire token family and invalidate session
            refreshTokenRepository.revokeAllInFamily(familyId);
            session.setIsRevoked(true);
            userSessionRepository.save(session);

            ResponseCookie cleanCookie = cookieService.createCleanRefreshTokenCookie();
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, cleanCookie.toString());

            auditService.logSync(session.getUser().getId(), session.getUser().getEmail(),
                    "TOKEN_REPLAY_BREACH", "RefreshToken", currentToken.getId().toString(),
                    Map.of("familyId", familyId.toString()), httpRequest);

            throw new TokenReplayBreachException("Refresh token reuse detected. Your session has been revoked for security.");
        }

        // 2. Revoked or Expired check
        if (Boolean.TRUE.equals(currentToken.getIsRevoked()) || Boolean.TRUE.equals(session.getIsRevoked()) || currentToken.isExpired()) {
            ResponseCookie cleanCookie = cookieService.createCleanRefreshTokenCookie();
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, cleanCookie.toString());
            throw ApiException.unauthorized("Refresh token expired or revoked");
        }

        User user = session.getUser();
        if (Boolean.TRUE.equals(user.getIsSuspended()) || !Boolean.TRUE.equals(user.getIsActive())) {
            ResponseCookie cleanCookie = cookieService.createCleanRefreshTokenCookie();
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, cleanCookie.toString());
            throw ApiException.forbidden("Account is suspended or inactive");
        }

        // 3. Legitimate Rotation
        String newRawRefreshToken = jwtService.generateOpaqueToken();
        String newTokenHash = jwtService.hashToken(newRawRefreshToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .session(session)
                .familyId(familyId)
                .tokenHash(newTokenHash)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .isRevoked(false)
                .build();
        newRefreshToken = refreshTokenRepository.save(newRefreshToken);

        currentToken.setConsumedAt(Instant.now());
        currentToken.setReplacedByTokenId(newRefreshToken.getId());
        refreshTokenRepository.save(currentToken);

        session.setLastActiveAt(Instant.now());
        userSessionRepository.save(session);

        UserPrincipal principal = UserPrincipal.create(user);
        String newAccessToken = jwtService.generateAccessToken(principal);

        ResponseCookie newCookie = cookieService.createRefreshTokenCookie(newRawRefreshToken, refreshTokenExpirationMs);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, newCookie.toString());

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresInMs(accessTokenExpirationMs)
                .build();
    }

    @Transactional
    public void logout(String rawRefreshToken, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String tokenHash = jwtService.hashToken(rawRefreshToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
                rt.setIsRevoked(true);
                refreshTokenRepository.save(rt);

                UserSession session = rt.getSession();
                session.setIsRevoked(true);
                userSessionRepository.save(session);

                auditService.logSync(session.getUser().getId(), session.getUser().getEmail(),
                        "LOGOUT", "UserSession", session.getId().toString(), Map.of(), httpRequest);
            });
        }

        ResponseCookie cleanCookie = cookieService.createCleanRefreshTokenCookie();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cleanCookie.toString());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().toLowerCase().trim();
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            String rawToken = jwtService.generateOpaqueToken();
            String tokenHash = jwtService.hashToken(rawToken);

            PasswordResetToken prt = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(tokenHash)
                    .expiresAt(Instant.now().plusSeconds(900)) // 15 minutes
                    .isUsed(false)
                    .build();
            passwordResetTokenRepository.save(prt);

            auditService.logSync(user.getId(), user.getEmail(), "PASSWORD_RESET_REQUESTED", "User", user.getId().toString(), Map.of(), httpRequest);
            log.info("Password reset initiated for user: {}. Token: {}", user.getEmail(), rawToken);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request, HttpServletRequest httpRequest) {
        String tokenHash = jwtService.hashToken(request.getToken());
        PasswordResetToken prt = passwordResetTokenRepository.findByTokenHashAndIsUsedFalse(tokenHash)
                .orElseThrow(() -> ApiException.badRequest("Invalid or expired password reset token"));

        if (prt.isExpired()) {
            throw ApiException.badRequest("Password reset token has expired");
        }

        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setSecurityStamp(UUID.randomUUID()); // Stamp change invalidates all existing JWTs
        user.setPasswordChangedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        prt.setIsUsed(true);
        passwordResetTokenRepository.save(prt);

        // Revoke all existing sessions
        userSessionRepository.revokeAllUserSessions(user.getId());

        // Evict security cache
        userSecurityCache.invalidate(user.getId());

        auditService.logSync(user.getId(), user.getEmail(), "PASSWORD_RESET_COMPLETED", "User", user.getId().toString(), Map.of(), httpRequest);
        log.info("Password reset successfully completed for user {}", user.getEmail());
    }
}
