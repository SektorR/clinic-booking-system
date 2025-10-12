package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for recurring availability management (psychologist portal)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringAvailabilityDTO {
    private String id;
    private String psychologistId;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isRecurring;
    private LocalDate effectiveFrom;
    private LocalDate effectiveUntil;
}
