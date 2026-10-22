package edu.jecrc.portal.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.academic.repository.StudentRepository;
import edu.jecrc.portal.modules.auth.dto.LoginRequest;
import edu.jecrc.portal.modules.offer.entity.PlacementOffer;
import edu.jecrc.portal.modules.offer.repository.PlacementOfferRepository;
import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.entity.JobApplication;
import edu.jecrc.portal.modules.placement.entity.JobProfile;
import edu.jecrc.portal.modules.placement.entity.PlacementDrive;
import edu.jecrc.portal.modules.placement.repository.CompanyRepository;
import edu.jecrc.portal.modules.placement.repository.JobApplicationRepository;
import edu.jecrc.portal.modules.placement.repository.JobProfileRepository;
import edu.jecrc.portal.modules.placement.repository.PlacementDriveRepository;
import edu.jecrc.portal.modules.user.entity.Department;
import edu.jecrc.portal.modules.user.repository.DepartmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class Milestone7UnifiedDashboardAndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PlacementDriveRepository driveRepository;

    @Autowired
    private JobProfileRepository jobProfileRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private PlacementOfferRepository offerRepository;

    private String getAccessToken(String email, String password) throws Exception {
        LoginRequest req = LoginRequest.builder().email(email).password(password).build();
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("accessToken").asText();
    }

    // 1. Scenario 1: Public Portal Overview API
    @Test
    @DisplayName("Scenario 1: Public portal overview must be publicly accessible without authentication")
    void testPublicOverviewAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/public/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.institutionName").value("JECRC University, Jaipur"))
                .andExpect(jsonPath("$.data.campusAddress").isNotEmpty())
                .andExpect(jsonPath("$.data.academicSchools").isArray())
                .andExpect(jsonPath("$.data.placementMilestones").isArray());
    }

    // 2. Scenario 2: Student Dashboard API
    @Test
    @DisplayName("Scenario 2: Student dashboard returns personalized statistics, attendance and active status")
    void testStudentDashboard() throws Exception {
        String token = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        mockMvc.perform(get("/api/v1/dashboards/student")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rollNumber").value("22BCSE101"))
                .andExpect(jsonPath("$.data.departmentCode").value("CSE"))
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.attendanceSummary").exists())
                .andExpect(jsonPath("$.data.appliedDrivesCount").isNumber());
    }

    // 3. Scenario 3: Unauthenticated Access to Student Dashboard
    @Test
    @DisplayName("Scenario 3: Unauthenticated access to student dashboard must be rejected with 401")
    void testStudentDashboardUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/student"))
                .andExpect(status().isUnauthorized());
    }

    // 4. Scenario 4: TPO Administrator Dashboard
    @Test
    @DisplayName("Scenario 4: TPO Administrator dashboard returns live recruiters, placement rate and tier metrics")
    void testTpoAdminDashboard() throws Exception {
        String token = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");

        mockMvc.perform(get("/api/v1/dashboards/tpo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRecruiters").isNumber())
                .andExpect(jsonPath("$.data.tierDistribution").exists())
                .andExpect(jsonPath("$.data.placementPercentage").isNumber());
    }

    // 5. Scenario 5: TPO Coordinator Access to TPO Dashboard
    @Test
    @DisplayName("Scenario 5: TPO Coordinator can access TPO dashboard under corrected V10 role permissions")
    void testTpoCoordinatorDashboardAccess() throws Exception {
        String token = getAccessToken("tpo.coord@jecrcuniversity.edu.in", "Password@123");

        mockMvc.perform(get("/api/v1/dashboards/tpo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // 6. Scenario 6: Student Forbidden from TPO Dashboard
    @Test
    @DisplayName("Scenario 6: Student accessing TPO dashboard must be rejected with 403 Forbidden")
    void testStudentForbiddenFromTpoDashboard() throws Exception {
        String token = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        mockMvc.perform(get("/api/v1/dashboards/tpo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    // 7. Scenario 7: Faculty Dashboard
    @Test
    @DisplayName("Scenario 7: Faculty dashboard returns assigned teaching subjects and conducted sessions")
    void testFacultyDashboard() throws Exception {
        String token = getAccessToken("faculty.cse@jecrcuniversity.edu.in", "Password@123");

        mockMvc.perform(get("/api/v1/dashboards/faculty")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").isNotEmpty())
                .andExpect(jsonPath("$.data.assignedClassesCount").isNumber());
    }

    // 8. Scenario 8: Student Forbidden from Faculty Dashboard
    @Test
    @DisplayName("Scenario 8: Student accessing Faculty dashboard must be rejected with 403 Forbidden")
    void testStudentForbiddenFromFacultyDashboard() throws Exception {
        String token = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        mockMvc.perform(get("/api/v1/dashboards/faculty")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // 9. Scenario 9: HOD Dashboard (Department Scoped)
    @Test
    @DisplayName("Scenario 9: HOD dashboard returns department-scoped student verification, placement and shortage roster")
    void testHodDashboardDepartmentScoped() throws Exception {
        String token = getAccessToken("hod.cse@jecrcuniversity.edu.in", "Password@123");

        mockMvc.perform(get("/api/v1/dashboards/hod")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.departmentCode").value("CSE"))
                .andExpect(jsonPath("$.data.totalStudents").isNumber())
                .andExpect(jsonPath("$.data.verifiedStudents").isNumber())
                .andExpect(jsonPath("$.data.studentsInShortage").isArray());
    }

    // 10. Scenario 10: Faculty Forbidden from HOD Dashboard
    @Test
    @DisplayName("Scenario 10: Faculty accessing HOD dashboard must be rejected with 403 Forbidden")
    void testFacultyForbiddenFromHodDashboard() throws Exception {
        String token = getAccessToken("faculty.cse@jecrcuniversity.edu.in", "Password@123");

        mockMvc.perform(get("/api/v1/dashboards/hod")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // 11. Scenario 11: Director Dashboard
    @Test
    @DisplayName("Scenario 11: Director dashboard returns cross-department institutional analytics and audit feed")
    void testDirectorDashboard() throws Exception {
        String token = getAccessToken("director@jecrcuniversity.edu.in", "Password@123");

        mockMvc.perform(get("/api/v1/dashboards/director")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalEnrolledStudents").isNumber())
                .andExpect(jsonPath("$.data.departmentWiseStats").isArray())
                .andExpect(jsonPath("$.data.ctcTierDistribution").exists())
                .andExpect(jsonPath("$.data.recentAuditLogs").isArray());
    }

    // 12. Scenario 12: Least Privilege Enforcement for Director Role
    @Test
    @DisplayName("Scenario 12: Director role cannot perform student verification mutations (least privilege)")
    void testDirectorLeastPrivilegeVerificationDenied() throws Exception {
        String token = getAccessToken("director@jecrcuniversity.edu.in", "Password@123");
        Student student = studentRepository.findByRollNumber("22BCSE101").orElseThrow();

        mockMvc.perform(post("/api/v1/students/" + student.getId() + "/verify")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verificationStatus\":\"VERIFIED\",\"remarks\":\"Director verification test\"}"))
                .andExpect(status().isForbidden());
    }

    // 13. Scenario 13: Placement Analytics Global Metrics
    @Test
    @DisplayName("Scenario 13: Placement Analytics API returns complete funnel, percentiles, tier breakdown and policy notes")
    void testPlacementAnalyticsGlobal() throws Exception {
        String token = getAccessToken("director@jecrcuniversity.edu.in", "Password@123");

        mockMvc.perform(get("/api/v1/analytics/placements")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalStudents").isNumber())
                .andExpect(jsonPath("$.data.eligibleStudents").isNumber())
                .andExpect(jsonPath("$.data.uniqueStudentsPlaced").isNumber())
                .andExpect(jsonPath("$.data.tierDistribution").exists())
                .andExpect(jsonPath("$.data.departmentBreakdown").isArray())
                .andExpect(jsonPath("$.data.policyNotes").isNotEmpty());
    }

    // 14. Scenario 14: Placement Analytics Department Filtered
    @Test
    @DisplayName("Scenario 14: Placement Analytics API filters accurately by departmentId")
    void testPlacementAnalyticsDepartmentFiltered() throws Exception {
        String token = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");
        Department cseDept = departmentRepository.findByDepartmentCode("CSE").orElseThrow();

        mockMvc.perform(get("/api/v1/analytics/placements")
                        .header("Authorization", "Bearer " + token)
                        .param("departmentId", cseDept.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.departmentId").value(cseDept.getId()))
                .andExpect(jsonPath("$.data.departmentCode").value("CSE"))
                .andExpect(jsonPath("$.data.scopeDescription").value("Department-Specific Analytics [Dept ID: " + cseDept.getId() + "]"));
    }

    // 15. Scenario 15: Double-Counting Prevention in Placement Metrics
    @Test
    @DisplayName("Scenario 15: Placement analytics calculates unique placed students without double counting multi-offer recipients")
    void testPlacementAnalyticsNoDoubleCounting() throws Exception {
        String token = getAccessToken("tpo.admin@jecrcuniversity.edu.in", "Password@123");

        // Verify that uniqueStudentsPlaced <= totalOffersIssued
        MvcResult res = mockMvc.perform(get("/api/v1/analytics/placements")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        long totalOffers = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("totalOffersIssued").asLong();
        long uniquePlaced = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("uniqueStudentsPlaced").asLong();

        assertThat(uniquePlaced).isLessThanOrEqualTo(totalOffers);
    }

    // 16. Scenario 16: Student Forbidden from Placement Analytics API
    @Test
    @DisplayName("Scenario 16: Student role cannot query executive placement analytics API")
    void testStudentForbiddenFromPlacementAnalytics() throws Exception {
        String token = getAccessToken("student.cse@jecrcuniversity.edu.in", "Password@123");

        mockMvc.perform(get("/api/v1/analytics/placements")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
