package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for scheduling a notification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "Recipient ID is required")
    private String recipientId;

    @NotBlank(message = "Recipient type is required")
    private String recipientType; // CLIENT, PSYCHOLOGIST, GUEST

    private String recipientEmail;
    private String recipientPhone;

    @NotBlank(message = "Notification type is required")
    private String notificationType; // BOOKING_CONFIRMATION, REMINDER, etc.

    @NotBlank(message = "Delivery method is required")
    private String deliveryMethod; // EMAIL, SMS, BOTH

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledFor;

    private String templateId;
    private Map<String, Object> templateData;

    private String guestBookingId;
    private String appointmentId;
}
