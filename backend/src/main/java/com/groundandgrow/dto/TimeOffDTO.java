package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeOffDTO {
    private String id;
    private String psychologistId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String reason;
    private LocalDateTime createdAt;
}
