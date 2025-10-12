package com.groundandgrow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Guest booking model for patients who book without creating an account
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "guest_bookings")
public class GuestBooking {

    @Id
    private String id;

    // Guest Information (no account required)
    private String firstName;
    private String lastName;

    @Indexed
    private String email;

    private String phone;
    private String dateOfBirth;

    // Booking Details
    private String psychologistId;
    private String sessionTypeId;
    private LocalDateTime appointmentDateTime;
    private Integer durationMinutes;
    private String modality; // online, in_person, phone

    // Payment Information
    private String stripePaymentIntentId;
    private String stripeCheckoutSessionId;
    private Double amount;
    private String paymentStatus; // pending, completed, failed, refunded

    // Booking Status
    private String bookingStatus; // pending_payment, confirmed, cancelled, completed, no_show

    // Additional Information
    private String notes;
    private String cancellationReason;
    private String psychologistNotes; // Private notes added by psychologist

    // For online sessions
    private String meetingLink;

    // For in-person sessions
    private String roomNumber;

    // Confirmation and tracking
    @Indexed(unique = true)
    private String confirmationToken; // Unique token for managing booking without account

    private Boolean emailConfirmed;
    private Boolean reminderSent;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
