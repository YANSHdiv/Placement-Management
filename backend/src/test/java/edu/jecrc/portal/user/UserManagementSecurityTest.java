package edu.jecrc.portal.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.jecrc.portal.modules.auth.dto.LoginRequest;
import edu.jecrc.portal.modules.user.dto.UserCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class UserManagementSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getAccessToken(String email, String password) throws Exception {
        LoginRequest req = LoginRequest.builder().email(email).password(password).build();
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        String json = res.getResponse().getContentAsString();
        return objectMapper.readTree(json).path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("Unauthenticated request to admin API must return 401 Unauthorized")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Student role accessing admin API must return 403 Forbidden")
    void testStudentForbiddenFromAdminApi() throws Exception {
        String studentToken = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("Admin role can create user and inspect append-only audit trail")
    void testAdminCreateUserAndAuditLog() throws Exception {
        String adminToken = getAccessToken("admin@jecrcuniversity.edu.in", "Admin@JECRC2026!");

        String uniqueFacultyEmail = "faculty." + System.currentTimeMillis() + "@jecrcuniversity.edu.in";
        UserCreateRequest createReq = UserCreateRequest.builder()
                .email(uniqueFacultyEmail)
                .password("FacultyPass@123")
                .firstName("Rohan")
                .lastName("Verma")
                .phoneNumber("+91-9988776655")
                .roles(Set.of("FACULTY"))
                .build();

        // 1. Create User
        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value(uniqueFacultyEmail));

        // 2. Query Audit Log for USER_CREATED event
        mockMvc.perform(get("/api/v1/audit-logs")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("action", "USER_CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Instantaneous revocation test: Suspending account must immediately deny subsequent requests")
    void testSuspensionInstantaneousRevocation() throws Exception {
        String adminToken = getAccessToken("admin@jecrcuniversity.edu.in", "Admin@JECRC2026!");

        // Create isolated test user for revocation test
        String uniqueEmail = "user." + System.currentTimeMillis() + "@jecrcuniversity.edu.in";
        UserCreateRequest createReq = UserCreateRequest.builder()
                .email(uniqueEmail)
                .password("TempPass@123")
                .firstName("Instant")
                .lastName("Revoke")
                .phoneNumber("+91-1122334455")
                .roles(Set.of("STUDENT"))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String targetUserId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // 1. Obtain active token for target user
        String targetUserToken = getAccessToken(uniqueEmail, "TempPass@123");

        // Verify token works
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + targetUserToken))
                .andExpect(status().isOk());

        // 2. Admin suspends the user
        mockMvc.perform(patch("/api/v1/admin/users/" + targetUserId + "/suspension")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("suspended", true))))
                .andExpect(status().isOk());

        // 3. INSTANTANEOUS CHECK: The user token must immediately fail with 401 without any cache delay
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + targetUserToken))
                .andExpect(status().isUnauthorized());
    }
}
