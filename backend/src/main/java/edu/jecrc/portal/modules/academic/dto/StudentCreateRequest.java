package edu.jecrc.portal.modules.academic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCreateRequest {

    @NotBlank(message = "University Student ID is required")
    private String universityStudentId;

    @NotBlank(message = "Roll Number is required")
    private String rollNumber;

    private String registrationNumber;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "University email is required")
    @Email(message = "Must be a valid email format")
    private String universityEmail;

    @Email(message = "Must be a valid personal email format")
    private String personalEmail;

    private String phoneNumber;

    private LocalDate dateOfBirth;

    private String gender;

    @NotNull(message = "Program ID is required")
    private Integer programId;

    @NotNull(message = "Batch ID is required")
    private Integer batchId;

    @NotNull(message = "Current semester is required")
    private Integer currentSemester;

    private String section;

    private LocalDate admissionDate;
}
