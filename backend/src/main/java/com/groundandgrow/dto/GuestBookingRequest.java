package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Request DTO for creating a guest booking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestBookingRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String dateOfBirth;

    @NotBlank(message = "Psychologist ID is required")
    private String psychologistId;

    @NotBlank(message = "Session type ID is required")
    private String sessionTypeId;

    @NotNull(message = "Appointment date and time is required")
    private LocalDateTime appointmentDateTime;

    @NotBlank(message = "Modality is required")
    private String modality; // online, in_person, phone

    private String notes;
}
