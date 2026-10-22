package edu.jecrc.portal.modules.dashboard.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.dashboard.dto.*;
import edu.jecrc.portal.modules.dashboard.service.DashboardService;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboards")
@RequiredArgsConstructor
@Tag(name = "Dashboards", description = "Role-specific aggregated dashboards for Students, TPO, Faculty, HODs, and Directors")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    private User getCallerUser(Authentication auth) {
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT') or hasAuthority('DRIVE_APPLY')")
    @Operation(summary = "Get student personalized dashboard with placement status, drives, offers and attendance")
    public ResponseEntity<ApiResponse<StudentDashboardDto>> getStudentDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getStudentDashboard(getCallerUser(auth))));
    }

    @GetMapping("/tpo")
    @PreAuthorize("hasAnyRole('TPO_ADMIN', 'TPO_COORDINATOR', 'SUPER_ADMIN') or hasAuthority('DRIVE_MANAGE')")
    @Operation(summary = "Get TPO administrator dashboard with live drives, offers, recruiters and pipeline metrics")
    public ResponseEntity<ApiResponse<TpoDashboardDto>> getTpoDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getTpoDashboard()));
    }

    @GetMapping("/faculty")
    @PreAuthorize("hasAnyRole('FACULTY', 'SUPER_ADMIN') or hasAuthority('ATTENDANCE_MARK')")
    @Operation(summary = "Get faculty dashboard with timetables, conducted sessions, and assigned interns")
    public ResponseEntity<ApiResponse<FacultyDashboardDto>> getFacultyDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getFacultyDashboard(getCallerUser(auth))));
    }

    @GetMapping("/hod")
    @PreAuthorize("hasAnyRole('HOD', 'SUPER_ADMIN') or hasAuthority('STUDENT_RECORD_VERIFY')")
    @Operation(summary = "Get Head of Department dashboard scoped to department students, attendance shortage, and placements")
    public ResponseEntity<ApiResponse<HodDashboardDto>> getHodDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getHodDashboard(getCallerUser(auth))));
    }

    @GetMapping("/director")
    @PreAuthorize("hasAnyRole('DIRECTOR', 'SUPER_ADMIN') or hasAuthority('REPORT_INSTITUTION')")
    @Operation(summary = "Get Director institutional executive dashboard with cross-department metrics and audits")
    public ResponseEntity<ApiResponse<DirectorDashboardDto>> getDirectorDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDirectorDashboard()));
    }
}
