package edu.jecrc.portal.modules.academic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentUpdateRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Must be a valid personal email format")
    private String personalEmail;

    private String phoneNumber;

    private LocalDate dateOfBirth;

    private String gender;

    private Integer currentSemester;

    private String section;

    private String academicStatus; // ENROLLED, SUSPENDED, ALUMNI, DROPOUT
}
