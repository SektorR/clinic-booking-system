package com.groundandgrow.service;

import com.groundandgrow.dto.*;
import com.groundandgrow.model.*;
import com.groundandgrow.repository.*;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing guest bookings
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GuestBookingService {

    private final GuestBookingRepository guestBookingRepository;
    private final PsychologistRepository psychologistRepository;
    private final SessionTypeRepository sessionTypeRepository;
    private final StripeService stripeService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final AvailabilityService availabilityService;
    private final NotificationSchedulerService notificationSchedulerService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.cancellation.hours-notice:24}")
    private int cancellationHoursNotice;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    /**
     * Create a new guest booking and initiate Stripe checkout
     */
    @Transactional
    public CheckoutSessionResponse createBooking(GuestBookingRequest request) {
        log.info("Creating guest booking for {} {}", request.getFirstName(), request.getLastName());

        // 1. Validate psychologist exists
        Psychologist psychologist = psychologistRepository.findById(request.getPsychologistId())
                .orElseThrow(() -> new RuntimeException("Psychologist not found"));

        // 2. Validate session type exists
        SessionType sessionType = sessionTypeRepository.findById(request.getSessionTypeId())
                .orElseThrow(() -> new RuntimeException("Session type not found"));

        // 3. Validate availability
        LocalDateTime appointmentDateTime = request.getAppointmentDateTime();
        boolean isAvailable = availabilityService.isSlotAvailable(
                request.getPsychologistId(),
                appointmentDateTime,
                sessionType.getDurationMinutes()
        );

        if (!isAvailable) {
            throw new RuntimeException("Selected time slot is not available");
        }

        // 4. Create guest booking record
        GuestBooking booking = new GuestBooking();
        booking.setFirstName(request.getFirstName());
        booking.setLastName(request.getLastName());
        booking.setEmail(request.getEmail());
        booking.setPhone(request.getPhone());
        booking.setPsychologistId(request.getPsychologistId());
        booking.setSessionTypeId(request.getSessionTypeId());
        booking.setAppointmentDateTime(appointmentDateTime);
        booking.setDurationMinutes(sessionType.getDurationMinutes());
        booking.setModality(request.getModality());
        booking.setAmount(sessionType.getPrice().doubleValue());
        booking.setPaymentStatus("PENDING");
        booking.setBookingStatus("PENDING_PAYMENT");
        booking.setConfirmationToken(UUID.randomUUID().toString());
        booking.setEmailConfirmed(false);
        booking.setReminderSent(false);
        booking.setNotes(request.getNotes());

        GuestBooking savedBooking = guestBookingRepository.save(booking);
        log.info("Guest booking created with ID: {}", savedBooking.getId());

        // 5. Create Stripe checkout session
        try {
            Long amountInCents = stripeService.convertToCents(sessionType.getPrice().doubleValue());
            Session stripeSession = stripeService.createCheckoutSession(
                    amountInCents,
                    savedBooking.getId(),
                    request.getEmail()
            );

            // 6. Update booking with Stripe session ID
            savedBooking.setStripeCheckoutSessionId(stripeSession.getId());
            guestBookingRepository.save(savedBooking);

            // 7. Return checkout URL
            CheckoutSessionResponse response = new CheckoutSessionResponse();
            response.setSessionId(stripeSession.getId());
            response.setCheckoutSessionId(stripeSession.getId());
            response.setCheckoutUrl(stripeSession.getUrl());
            response.setBookingId(savedBooking.getId());
            response.setConfirmationToken(savedBooking.getConfirmationToken());

            log.info("Stripe checkout session created: {}", stripeSession.getId());
            return response;

        } catch (StripeException e) {
            log.error("Failed to create Stripe checkout session", e);
            // Clean up the booking
            guestBookingRepository.delete(savedBooking);
            throw new RuntimeException("Failed to initiate payment: " + e.getMessage());
        }
    }

    /**
     * Handle successful payment (called by Stripe webhook)
     */
    @Transactional
    public void handlePaymentSuccess(String checkoutSessionId) {
        log.info("Processing payment success for session: {}", checkoutSessionId);

        GuestBooking booking = guestBookingRepository.findByStripeCheckoutSessionId(checkoutSessionId)
                .orElseThrow(() -> new RuntimeException("Booking not found for session: " + checkoutSessionId));

        // Update booking status
        booking.setPaymentStatus("COMPLETED");
        booking.setBookingStatus("CONFIRMED");
        booking.setEmailConfirmed(true);
        guestBookingRepository.save(booking);

        // Get psychologist details
        Psychologist psychologist = psychologistRepository.findById(booking.getPsychologistId())
                .orElseThrow(() -> new RuntimeException("Psychologist not found"));

        String psychologistName = psychologist.getFirstName() + " " + psychologist.getLastName();
        String patientName = booking.getFirstName() + " " + booking.getLastName();
        String appointmentDate = booking.getAppointmentDateTime().format(DATE_FORMATTER);
        String appointmentTime = booking.getAppointmentDateTime().format(TIME_FORMATTER);

        // Send confirmation email
        try {
            emailService.sendBookingConfirmation(
                    booking.getEmail(),
                    patientName,
                    psychologistName,
                    appointmentDate,
                    appointmentTime,
                    booking.getModality()
            );
            log.info("Booking confirmation email sent to: {}", booking.getEmail());
        } catch (Exception e) {
            log.error("Failed to send confirmation email", e);
        }

        // Send confirmation SMS
        try {
            smsService.sendBookingConfirmationSms(
                    booking.getPhone(),
                    patientName,
                    psychologistName,
                    appointmentDate,
                    appointmentTime
            );
            log.info("Booking confirmation SMS sent to: {}", booking.getPhone());
        } catch (Exception e) {
            log.error("Failed to send confirmation SMS", e);
        }

        // Schedule reminder notification (24 hours before)
        scheduleAppointmentReminder(booking, psychologistName, patientName, appointmentDate, appointmentTime);
    }

    /**
     * Handle failed payment (called by Stripe webhook)
     */
    @Transactional
    public void handlePaymentFailure(String checkoutSessionId) {
        log.info("Processing payment failure for session: {}", checkoutSessionId);

        GuestBooking booking = guestBookingRepository.findByStripeCheckoutSessionId(checkoutSessionId)
                .orElseThrow(() -> new RuntimeException("Booking not found for session: " + checkoutSessionId));

        booking.setPaymentStatus("FAILED");
        booking.setBookingStatus("CANCELLED");
        guestBookingRepository.save(booking);

        log.info("Booking {} marked as failed", booking.getId());
    }

    /**
     * Get booking by confirmation token
     */
    public GuestBookingDTO getBookingByToken(String token) {
        GuestBooking booking = guestBookingRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        return convertToDTO(booking);
    }

    /**
     * Cancel booking with refund
     */
    @Transactional
    public CancellationResponse cancelBooking(String token) {
        GuestBooking booking = guestBookingRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!"CONFIRMED".equals(booking.getBookingStatus())) {
            throw new RuntimeException("Only confirmed bookings can be cancelled");
        }

        // Check cancellation policy (24 hours notice)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime appointmentTime = booking.getAppointmentDateTime();
        long hoursUntilAppointment = java.time.Duration.between(now, appointmentTime).toHours();

        boolean refundEligible = hoursUntilAppointment >= cancellationHoursNotice;

        CancellationResponse response = CancellationResponse.builder()
                .bookingId(booking.getId())
                .cancelled(true)
                .refundEligible(refundEligible)
                .build();

        // Process refund if eligible
        if (refundEligible && booking.getStripePaymentIntentId() != null) {
            try {
                Long amountInCents = stripeService.convertToCents(booking.getAmount());
                stripeService.createRefund(booking.getStripePaymentIntentId(), amountInCents);
                booking.setPaymentStatus("REFUNDED");
                response.setRefundAmount(booking.getAmount());
                response.setRefundProcessed(true);
                log.info("Refund processed for booking: {}", booking.getId());
            } catch (StripeException e) {
                log.error("Failed to process refund", e);
                response.setRefundProcessed(false);
                response.setRefundError("Failed to process refund: " + e.getMessage());
            }
        } else {
            response.setRefundAmount(0.0);
            response.setRefundProcessed(false);
            response.setRefundError(refundEligible ? "No payment to refund" :
                "Cancellation must be made at least " + cancellationHoursNotice + " hours in advance");
        }

        // Update booking status
        booking.setBookingStatus("CANCELLED");
        guestBookingRepository.save(booking);

        // Send cancellation confirmation
        try {
            String patientName = booking.getFirstName() + " " + booking.getLastName();
            String appointmentDate = booking.getAppointmentDateTime().format(DATE_FORMATTER);

            emailService.sendCancellationConfirmation(booking.getEmail(), patientName, appointmentDate);
            smsService.sendCancellationSms(booking.getPhone(), patientName, appointmentDate);
            log.info("Cancellation notifications sent for booking: {}", booking.getId());
        } catch (Exception e) {
            log.error("Failed to send cancellation notifications", e);
        }

        return response;
    }

    /**
     * Reschedule booking
     */
    @Transactional
    public GuestBookingDTO rescheduleBooking(String token, RescheduleRequest request) {
        GuestBooking booking = guestBookingRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!"CONFIRMED".equals(booking.getBookingStatus())) {
            throw new RuntimeException("Only confirmed bookings can be rescheduled");
        }

        LocalDateTime newDateTime = request.getNewAppointmentDateTime();

        // Validate new slot is available
        boolean isAvailable = availabilityService.isSlotAvailable(
                booking.getPsychologistId(),
                newDateTime,
                booking.getDurationMinutes()
        );

        if (!isAvailable) {
            throw new RuntimeException("Selected time slot is not available");
        }

        // Update booking
        booking.setAppointmentDateTime(newDateTime);
        GuestBooking updatedBooking = guestBookingRepository.save(booking);

        // Send rescheduling confirmation
        try {
            Psychologist psychologist = psychologistRepository.findById(booking.getPsychologistId())
                    .orElseThrow(() -> new RuntimeException("Psychologist not found"));

            String psychologistName = psychologist.getFirstName() + " " + psychologist.getLastName();
            String patientName = booking.getFirstName() + " " + booking.getLastName();
            String appointmentDate = booking.getAppointmentDateTime().format(DATE_FORMATTER);
            String appointmentTime = booking.getAppointmentDateTime().format(TIME_FORMATTER);

            emailService.sendBookingConfirmation(
                    booking.getEmail(),
                    patientName,
                    psychologistName,
                    appointmentDate,
                    appointmentTime,
                    booking.getModality()
            );

            log.info("Rescheduling confirmation sent for booking: {}", booking.getId());
        } catch (Exception e) {
            log.error("Failed to send rescheduling confirmation", e);
        }

        return convertToDTO(updatedBooking);
    }

    /**
     * Get bookings by email
     */
    public List<GuestBookingDTO> getBookingsByEmail(String email) {
        List<GuestBooking> bookings = guestBookingRepository.findByEmail(email);
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Schedule appointment reminder
     */
    private void scheduleAppointmentReminder(GuestBooking booking, String psychologistName,
                                            String patientName, String appointmentDate, String appointmentTime) {
        try {
            LocalDateTime reminderTime = booking.getAppointmentDateTime().minusHours(24);

            // Only schedule if appointment is more than 24 hours away
            if (reminderTime.isAfter(LocalDateTime.now())) {
                String subject = "Appointment Reminder - Ground & Grow Psychology";
                String message = String.format(
                    "Reminder: %s, your appointment with %s is tomorrow at %s",
                    patientName, psychologistName, appointmentTime
                );

                notificationSchedulerService.scheduleNotification(
                    booking.getId(),           // recipientId
                    "GUEST",                   // recipientType
                    booking.getEmail(),        // recipientEmail
                    booking.getPhone(),        // recipientPhone
                    "REMINDER",                // notificationType
                    "BOTH",                    // deliveryMethod
                    subject,                   // subject
                    message,                   // message
                    reminderTime,              // scheduledFor
                    null,                      // templateId
                    null                       // templateData
                );
                log.info("Reminder scheduled for booking: {} at {}", booking.getId(), reminderTime);
            }
        } catch (Exception e) {
            log.error("Failed to schedule reminder", e);
        }
    }

    /**
     * Convert GuestBooking to DTO
     */
    private GuestBookingDTO convertToDTO(GuestBooking booking) {
        Psychologist psychologist = psychologistRepository.findById(booking.getPsychologistId())
                .orElse(null);

        SessionType sessionType = sessionTypeRepository.findById(booking.getSessionTypeId())
                .orElse(null);

        GuestBookingDTO dto = new GuestBookingDTO();
        dto.setId(booking.getId());
        dto.setFirstName(booking.getFirstName());
        dto.setLastName(booking.getLastName());
        dto.setEmail(booking.getEmail());
        dto.setPhone(booking.getPhone());
        dto.setPsychologistId(booking.getPsychologistId());
        dto.setPsychologistName(psychologist != null ?
                psychologist.getFirstName() + " " + psychologist.getLastName() : "Unknown");
        dto.setSessionTypeId(booking.getSessionTypeId());
        dto.setSessionTypeName(sessionType != null ? sessionType.getName() : "Unknown");
        dto.setAppointmentDateTime(booking.getAppointmentDateTime());
        dto.setDurationMinutes(booking.getDurationMinutes());
        dto.setModality(booking.getModality());
        dto.setAmount(booking.getAmount());
        dto.setPaymentStatus(booking.getPaymentStatus());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setConfirmationToken(booking.getConfirmationToken());
        dto.setReminderSent(booking.getReminderSent());
        dto.setMeetingLink(booking.getMeetingLink());
        dto.setRoomNumber(booking.getRoomNumber());
        dto.setNotes(booking.getNotes());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());

        return dto;
    }
}
