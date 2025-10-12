package com.groundandgrow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Notification model for email and SMS notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    // Recipient Information
    @Indexed
    private String recipientId; // Client or Psychologist ID

    private String recipientType; // CLIENT, PSYCHOLOGIST, GUEST

    private String recipientEmail;
    private String recipientPhone;

    // For guest bookings
    @Indexed
    private String guestBookingId;

    // Notification Details
    private String notificationType; // BOOKING_CONFIRMATION, REMINDER, CANCELLATION, RESCHEDULED, MESSAGE_RECEIVED, PAYMENT_CONFIRMATION

    private String deliveryMethod; // EMAIL, SMS, BOTH

    private String subject;
    private String message;

    // Template information
    private String templateId;
    private String templateData; // JSON string of template variables

    // Scheduling
    @Indexed
    private LocalDateTime scheduledFor;

    private LocalDateTime sentAt;

    // Status
    @Indexed
    private String status; // PENDING, SENT, FAILED, CANCELLED

    private String errorMessage;
    private Integer retryCount;

    // External IDs (from SendGrid, Twilio, etc.)
    private String externalId;
    private String externalProvider; // SENDGRID, TWILIO, JAVAMAIL

    // Related entities
    @Indexed
    private String appointmentId;

    @CreatedDate
    private LocalDateTime createdAt;
}
