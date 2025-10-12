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
public class AppointmentDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String patientName; // Full name for convenience (firstName + lastName)
    private String email;
    private String phone;
    private String psychologistId;
    private String psychologistName;
    private String sessionTypeId;
    private String sessionTypeName;
    private LocalDateTime appointmentDateTime;
    private Integer durationMinutes;
    private String modality;
    private Double amount;
    private String paymentStatus;
    private String bookingStatus;
    private String notes;
    private String psychologistNotes;
    private String cancellationReason;
    private String meetingLink;
    private String roomNumber;
    private Boolean reminderSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
