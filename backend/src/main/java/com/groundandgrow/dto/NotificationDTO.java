package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for notification response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private String id;
    private String recipientId;
    private String recipientType;
    private String recipientEmail;
    private String recipientPhone;
    private String guestBookingId;
    private String notificationType;
    private String deliveryMethod;
    private String subject;
    private String message;
    private String templateId;
    private LocalDateTime scheduledFor;
    private LocalDateTime sentAt;
    private String status;
    private String errorMessage;
    private Integer retryCount;
    private String externalId;
    private String externalProvider;
    private String appointmentId;
    private LocalDateTime createdAt;
}
