package edu.jecrc.portal.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.jecrc.portal.modules.auth.dto.LoginRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Valid credentials should return access token and set HttpOnly refresh cookie")
    void testSuccessfulLogin() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("admin@jecrcuniversity.edu.in")
                .password("Admin@JECRC2026!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("admin@jecrcuniversity.edu.in"))
                .andExpect(cookie().exists("jecrc_refresh_token"))
                .andExpect(cookie().httpOnly("jecrc_refresh_token", true));
    }

    @Test
    @DisplayName("Invalid credentials must return 401 Unauthorized")
    void testInvalidCredentials() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("admin@jecrcuniversity.edu.in")
                .password("WrongPassword123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Refresh Token Rotation (RTR) and Replay Breach Detection")
    void testRefreshTokenRotationAndReplayBreach() throws Exception {
        // 1. Log in to get initial refresh token
        LoginRequest loginRequest = LoginRequest.builder()
                .email("admin@jecrcuniversity.edu.in")
                .password("Admin@JECRC2026!")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie initialCookie = loginResult.getResponse().getCookie("jecrc_refresh_token");
        assertThat(initialCookie).isNotNull();

        // 2. Perform legitimate refresh with T1 -> should get new T2 and new access token
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(initialCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        Cookie rotatedCookie = refreshResult.getResponse().getCookie("jecrc_refresh_token");
        assertThat(rotatedCookie).isNotNull();
        assertThat(rotatedCookie.getValue()).isNotEqualTo(initialCookie.getValue());

        // 3. REPLAY ATTACK: Attacker replays consumed token T1 -> MUST detect breach & revoke session family!
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(initialCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("TOKEN_REPLAY_BREACH_DETECTED"));

        // 4. Verifying family revocation: Even the newly rotated token T2 must now be rejected because family was revoked!
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(rotatedCookie))
                .andExpect(status().isUnauthorized());
    }
}
