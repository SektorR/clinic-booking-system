package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for psychologist availability information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDTO {

    private String psychologistId;
    private LocalDate date;
    private Integer durationMinutes;
    private List<TimeSlot> availableSlots;
    private Integer totalSlots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer durationMinutes;
        private Boolean available;
    }
}
