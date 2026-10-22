package edu.jecrc.portal.modules.academic.ops.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.ops.dto.*;
import edu.jecrc.portal.modules.academic.ops.service.FacultyService;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Faculty & Curriculum", description = "Faculty profiles, course teaching allocations, subjects, and classrooms")
public class FacultyController {

    private final FacultyService facultyService;
    private final UserRepository userRepository;

    private User getCallerUser(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @PostMapping("/faculty")
    @PreAuthorize("hasAuthority('FACULTY_MANAGE')")
    @Operation(summary = "Create a faculty master profile")
    public ResponseEntity<ApiResponse<FacultyProfileDto>> createProfile(
            @Valid @RequestBody FacultyProfileCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Faculty profile created successfully",
                facultyService.createFacultyProfile(req, getCallerUser(auth))));
    }

    @GetMapping("/faculty")
    @Operation(summary = "List faculty profiles")
    public ResponseEntity<ApiResponse<List<FacultyProfileDto>>> listFaculty(
            @RequestParam(required = false) Integer departmentId
    ) {
        return ResponseEntity.ok(ApiResponse.success("Faculty profiles retrieved",
                facultyService.listFacultyProfiles(departmentId)));
    }

    @GetMapping("/faculty/me")
    @Operation(summary = "Get current logged-in faculty profile")
    public ResponseEntity<ApiResponse<FacultyProfileDto>> getMyProfile(Authentication auth) {
        User user = getCallerUser(auth);
        return ResponseEntity.ok(ApiResponse.success("Faculty profile retrieved",
                facultyService.getFacultyProfileByUserId(user.getId())));
    }

    @GetMapping("/faculty/{facultyId}")
    @Operation(summary = "Get faculty profile details")
    public ResponseEntity<ApiResponse<FacultyProfileDto>> getProfile(@PathVariable UUID facultyId) {
        return ResponseEntity.ok(ApiResponse.success("Faculty profile retrieved",
                facultyService.getFacultyProfile(facultyId)));
    }

    @PostMapping("/faculty/assignments")
    @PreAuthorize("hasAuthority('FACULTY_ASSIGNMENT_MANAGE')")
    @Operation(summary = "Allocate teaching subject and cohort assignment to faculty")
    public ResponseEntity<ApiResponse<FacultyTeachingAssignmentDto>> createAssignment(
            @Valid @RequestBody FacultyTeachingAssignmentCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Teaching assignment created successfully",
                facultyService.createTeachingAssignment(req, getCallerUser(auth))));
    }

    @GetMapping("/faculty/assignments")
    @Operation(summary = "List teaching assignments")
    public ResponseEntity<ApiResponse<List<FacultyTeachingAssignmentDto>>> listAssignments(
            @RequestParam(required = false) UUID facultyId,
            @RequestParam(required = false) Integer batchId,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String section
    ) {
        return ResponseEntity.ok(ApiResponse.success("Teaching assignments retrieved",
                facultyService.listTeachingAssignments(facultyId, batchId, semester, section)));
    }

    @GetMapping("/academic-subjects")
    @Operation(summary = "List academic subjects / curriculum")
    public ResponseEntity<ApiResponse<List<AcademicSubjectDto>>> listSubjects(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer semester
    ) {
        return ResponseEntity.ok(ApiResponse.success("Subjects retrieved",
                facultyService.listAcademicSubjects(departmentId, semester)));
    }

    @PostMapping("/academic-subjects")
    @PreAuthorize("hasAuthority('FACULTY_MANAGE')")
    @Operation(summary = "Add a new academic curriculum subject")
    public ResponseEntity<ApiResponse<AcademicSubjectDto>> createSubject(
            @Valid @RequestBody AcademicSubjectCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Academic subject created successfully",
                facultyService.createAcademicSubject(req, getCallerUser(auth))));
    }

    @GetMapping("/classrooms")
    @Operation(summary = "List active classrooms and lecture halls")
    public ResponseEntity<ApiResponse<List<ClassroomDto>>> listClassrooms() {
        return ResponseEntity.ok(ApiResponse.success("Classrooms retrieved",
                facultyService.listClassrooms()));
    }

    @PostMapping("/classrooms")
    @PreAuthorize("hasAuthority('TIMETABLE_MANAGE')")
    @Operation(summary = "Create a new classroom or lecture venue")
    public ResponseEntity<ApiResponse<ClassroomDto>> createClassroom(
            @Valid @RequestBody ClassroomCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Classroom created successfully",
                facultyService.createClassroom(req, getCallerUser(auth))));
    }
}
