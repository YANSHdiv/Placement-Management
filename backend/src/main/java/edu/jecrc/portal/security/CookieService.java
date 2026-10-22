package edu.jecrc.portal.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CookieService {

    @Value("${app.cookie.name:jecrc_refresh_token}")
    private String cookieName;

    @Value("${app.cookie.domain:}")
    private String cookieDomain;

    @Value("${app.cookie.same-site:Lax}")
    private String sameSite;

    @Value("${app.cookie.secure:false}")
    private boolean secure;

    public ResponseCookie createRefreshTokenCookie(String token, long maxAgeMillis) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(secure)
                .path("/api/v1/auth")
                .maxAge(Duration.ofMillis(maxAgeMillis))
                .sameSite(sameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    public ResponseCookie createCleanRefreshTokenCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite(sameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    public String getCookieName() {
        return cookieName;
    }
}
