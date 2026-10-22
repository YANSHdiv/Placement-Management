package edu.jecrc.portal.modules.academic.ops.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.ops.dto.TimetableSlotCreateRequest;
import edu.jecrc.portal.modules.academic.ops.dto.TimetableSlotDto;
import edu.jecrc.portal.modules.academic.ops.dto.TimetableSlotUpdateRequest;
import edu.jecrc.portal.modules.academic.ops.service.TimetableService;
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
@RequestMapping("/api/v1/timetables")
@RequiredArgsConstructor
@Tag(name = "Academic Timetables", description = "Scheduling matrix, clash-free slot booking, and cohort calendars")
public class TimetableController {

    private final TimetableService timetableService;
    private final UserRepository userRepository;

    private User getCallerUser(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @PostMapping("/slots")
    @PreAuthorize("hasAuthority('TIMETABLE_MANAGE')")
    @Operation(summary = "Schedule a timetable slot (race-free GiST exclusion protected)")
    public ResponseEntity<ApiResponse<TimetableSlotDto>> createSlot(
            @Valid @RequestBody TimetableSlotCreateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Timetable slot scheduled successfully",
                timetableService.createSlot(req, getCallerUser(auth))));
    }

    @PutMapping("/slots/{slotId}")
    @PreAuthorize("hasAuthority('TIMETABLE_MANAGE')")
    @Operation(summary = "Update an existing timetable slot")
    public ResponseEntity<ApiResponse<TimetableSlotDto>> updateSlot(
            @PathVariable UUID slotId,
            @RequestBody TimetableSlotUpdateRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Timetable slot updated successfully",
                timetableService.updateSlot(slotId, req, getCallerUser(auth))));
    }

    @DeleteMapping("/slots/{slotId}")
    @PreAuthorize("hasAuthority('TIMETABLE_MANAGE')")
    @Operation(summary = "Cancel / deactivate a timetable slot")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(
            @PathVariable UUID slotId,
            Authentication auth
    ) {
        timetableService.deleteSlot(slotId, getCallerUser(auth));
        return ResponseEntity.ok(ApiResponse.success("Timetable slot cancelled", null));
    }

    @GetMapping("/cohort")
    @PreAuthorize("hasAuthority('TIMETABLE_VIEW')")
    @Operation(summary = "Get timetable for a student cohort (batch, semester, section)")
    public ResponseEntity<ApiResponse<List<TimetableSlotDto>>> getCohortTimetable(
            @RequestParam Integer batchId,
            @RequestParam Integer semester,
            @RequestParam String section,
            @RequestParam(required = false) String academicYear
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cohort timetable retrieved",
                timetableService.listCohortSlots(batchId, semester, section, academicYear)));
    }

    @GetMapping("/faculty/{facultyId}")
    @PreAuthorize("hasAuthority('TIMETABLE_VIEW')")
    @Operation(summary = "Get timetable for a faculty member")
    public ResponseEntity<ApiResponse<List<TimetableSlotDto>>> getFacultyTimetable(
            @PathVariable UUID facultyId,
            @RequestParam(required = false) String academicYear
    ) {
        return ResponseEntity.ok(ApiResponse.success("Faculty timetable retrieved",
                timetableService.listFacultySlots(facultyId, academicYear)));
    }

    @GetMapping("/classroom/{classroomId}")
    @PreAuthorize("hasAuthority('TIMETABLE_VIEW')")
    @Operation(summary = "Get timetable occupancy for a classroom venue")
    public ResponseEntity<ApiResponse<List<TimetableSlotDto>>> getClassroomTimetable(
            @PathVariable Integer classroomId,
            @RequestParam(required = false) String academicYear
    ) {
        return ResponseEntity.ok(ApiResponse.success("Classroom timetable retrieved",
                timetableService.listClassroomSlots(classroomId, academicYear)));
    }
}
