package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for session type information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionTypeDTO {

    private String id;
    private String name;
    private String description;
    private Integer durationMinutes;
    private Double price;
    private String modality; // online, in_person, phone
    private Boolean isActive;
}
