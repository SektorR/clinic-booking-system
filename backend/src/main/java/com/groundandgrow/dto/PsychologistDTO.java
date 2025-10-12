package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for public psychologist information (no sensitive data)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PsychologistDTO {

    private String id;
    private String firstName;
    private String lastName;
    private String specialization;
    private String registrationNumber;
    private String bio;
    private String phone;
    private String email;
    private String role;
    private Boolean isActive;
}
