package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for guest booking response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestBookingDTO {

    private String id;
    private String firstName;
    private String lastName;
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
    private String confirmationToken;
    private Boolean reminderSent;
    private String meetingLink;
    private String roomNumber;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
