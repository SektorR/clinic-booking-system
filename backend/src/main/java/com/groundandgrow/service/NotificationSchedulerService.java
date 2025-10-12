package com.groundandgrow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groundandgrow.model.GuestBooking;
import com.groundandgrow.model.Notification;
import com.groundandgrow.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for scheduling and processing notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSchedulerService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final ObjectMapper objectMapper;

    @Value("${app.booking.reminder-hours-before:24}")
    private int reminderHoursBefore;

    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.notification.sms.enabled:false}")
    private boolean smsEnabled;

    /**
     * Process pending notifications every minute
     */
    @Scheduled(fixedDelay = 60000) // Run every 60 seconds
    public void processPendingNotifications() {
        LocalDateTime now = LocalDateTime.now();

        List<Notification> pendingNotifications = notificationRepository
            .findByStatusAndScheduledForBefore("PENDING", now);

        if (pendingNotifications.isEmpty()) {
            return;
        }

        log.info("Processing {} pending notifications", pendingNotifications.size());

        for (Notification notification : pendingNotifications) {
            try {
                sendNotification(notification);
                notification.setStatus("SENT");
                notification.setSentAt(LocalDateTime.now());
            } catch (Exception e) {
                log.error("Failed to send notification {}: {}", notification.getId(), e.getMessage());
                notification.setStatus("FAILED");
                notification.setErrorMessage(e.getMessage());
                notification.setRetryCount(notification.getRetryCount() != null ? notification.getRetryCount() + 1 : 1);

                // Retry up to 3 times
                if (notification.getRetryCount() < 3) {
                    notification.setStatus("PENDING");
                    notification.setScheduledFor(LocalDateTime.now().plusMinutes(5)); // Retry in 5 minutes
                }
            } finally {
                notificationRepository.save(notification);
            }
        }

        log.info("Completed processing pending notifications");
    }

    /**
     * Send a notification via the appropriate delivery method
     */
    private void sendNotification(Notification notification) {
        String deliveryMethod = notification.getDeliveryMethod();

        if ("BOTH".equals(deliveryMethod)) {
            sendEmailNotification(notification);
            sendSmsNotification(notification);
        } else if ("EMAIL".equals(deliveryMethod)) {
            sendEmailNotification(notification);
        } else if ("SMS".equals(deliveryMethod)) {
            sendSmsNotification(notification);
        }
    }

    /**
     * Send email notification
     */
    private void sendEmailNotification(Notification notification) {
        if (!emailEnabled) {
            log.warn("Email notifications are disabled");
            return;
        }

        if (notification.getRecipientEmail() == null || notification.getRecipientEmail().isEmpty()) {
            throw new IllegalArgumentException("Recipient email is required for email notifications");
        }

        try {
            if (notification.getTemplateId() != null && !notification.getTemplateId().isEmpty()) {
                // Use template
                Map<String, Object> variables = parseTemplateData(notification.getTemplateData());
                emailService.sendTemplatedEmail(
                    notification.getRecipientEmail(),
                    notification.getSubject(),
                    notification.getTemplateId(),
                    variables
                );
            } else {
                // Send simple email
                emailService.sendSimpleEmail(
                    notification.getRecipientEmail(),
                    notification.getSubject(),
                    notification.getMessage()
                );
            }

            notification.setExternalProvider("JAVAMAIL");
            log.info("Email notification sent to: {}", notification.getRecipientEmail());
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send SMS notification
     */
    private void sendSmsNotification(Notification notification) {
        if (!smsEnabled) {
            log.warn("SMS notifications are disabled");
            return;
        }

        if (notification.getRecipientPhone() == null || notification.getRecipientPhone().isEmpty()) {
            throw new IllegalArgumentException("Recipient phone is required for SMS notifications");
        }

        try {
            smsService.sendSms(notification.getRecipientPhone(), notification.getMessage());
            notification.setExternalProvider("TWILIO");
            log.info("SMS notification sent to: {}", notification.getRecipientPhone());
        } catch (Exception e) {
            log.error("Failed to send SMS notification", e);
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    /**
     * Schedule a custom notification
     */
    public Notification scheduleNotification(
            String recipientId,
            String recipientType,
            String recipientEmail,
            String recipientPhone,
            String notificationType,
            String deliveryMethod,
            String subject,
            String message,
            LocalDateTime scheduledFor,
            String templateId,
            Map<String, Object> templateData) {

        Notification notification = Notification.builder()
            .recipientId(recipientId)
            .recipientType(recipientType)
            .recipientEmail(recipientEmail)
            .recipientPhone(recipientPhone)
            .notificationType(notificationType)
            .deliveryMethod(deliveryMethod)
            .subject(subject)
            .message(message)
            .scheduledFor(scheduledFor)
            .templateId(templateId)
            .templateData(templateData != null ? convertToJson(templateData) : null)
            .status("PENDING")
            .retryCount(0)
            .createdAt(LocalDateTime.now())
            .build();

        return notificationRepository.save(notification);
    }

    /**
     * Schedule appointment reminder for a booking
     */
    public void scheduleAppointmentReminder(GuestBooking booking) {
        LocalDateTime reminderTime = booking.getAppointmentDateTime().minusHours(reminderHoursBefore);

        if (reminderTime.isBefore(LocalDateTime.now())) {
            log.warn("Reminder time is in the past for booking {}", booking.getId());
            return;
        }

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("patientName", booking.getFirstName());
        templateData.put("psychologistName", "Dr. " + booking.getPsychologistId()); // TODO: Get actual psychologist name
        templateData.put("appointmentDate", booking.getAppointmentDateTime().toLocalDate().toString());
        templateData.put("appointmentTime", booking.getAppointmentDateTime().toLocalTime().toString());
        templateData.put("modality", booking.getModality());

        // Create email reminder
        Notification emailReminder = scheduleNotification(
            booking.getId(),
            "GUEST",
            booking.getEmail(),
            booking.getPhone(),
            "REMINDER",
            "BOTH",
            "Appointment Reminder - Ground & Grow Psychology",
            String.format("Reminder: Your appointment is tomorrow at %s",
                booking.getAppointmentDateTime().toLocalTime()),
            reminderTime,
            "email/appointment-reminder",
            templateData
        );

        emailReminder.setGuestBookingId(booking.getId());
        notificationRepository.save(emailReminder);

        log.info("Scheduled appointment reminder for booking {} at {}", booking.getId(), reminderTime);
    }

    /**
     * Send immediate booking confirmation
     */
    public void sendBookingConfirmation(GuestBooking booking, String psychologistName, String managementLink) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("patientName", booking.getFirstName());
        templateData.put("psychologistName", psychologistName);
        templateData.put("appointmentDate", booking.getAppointmentDateTime().toLocalDate().toString());
        templateData.put("appointmentTime", booking.getAppointmentDateTime().toLocalTime().toString());
        templateData.put("modality", booking.getModality());
        templateData.put("managementLink", managementLink);

        // Send immediately
        Notification notification = scheduleNotification(
            booking.getId(),
            "GUEST",
            booking.getEmail(),
            booking.getPhone(),
            "BOOKING_CONFIRMATION",
            "BOTH",
            "Booking Confirmation - Ground & Grow Psychology",
            String.format("Your appointment with %s is confirmed for %s at %s",
                psychologistName,
                booking.getAppointmentDateTime().toLocalDate(),
                booking.getAppointmentDateTime().toLocalTime()),
            LocalDateTime.now(),
            "email/booking-confirmation",
            templateData
        );

        notification.setGuestBookingId(booking.getId());
        notificationRepository.save(notification);

        log.info("Scheduled booking confirmation for booking {}", booking.getId());
    }

    /**
     * Send cancellation confirmation
     */
    public void sendCancellationConfirmation(GuestBooking booking) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("patientName", booking.getFirstName());
        templateData.put("appointmentDate", booking.getAppointmentDateTime().toLocalDate().toString());

        Notification notification = scheduleNotification(
            booking.getId(),
            "GUEST",
            booking.getEmail(),
            booking.getPhone(),
            "CANCELLATION",
            "BOTH",
            "Appointment Cancelled - Ground & Grow Psychology",
            String.format("Your appointment on %s has been cancelled",
                booking.getAppointmentDateTime().toLocalDate()),
            LocalDateTime.now(),
            "email/cancellation-confirmation",
            templateData
        );

        notification.setGuestBookingId(booking.getId());
        notificationRepository.save(notification);

        log.info("Scheduled cancellation confirmation for booking {}", booking.getId());
    }

    /**
     * Convert template data map to JSON string
     */
    private String convertToJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Failed to convert template data to JSON", e);
            return "{}";
        }
    }

    /**
     * Parse template data from JSON string
     */
    private Map<String, Object> parseTemplateData(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Failed to parse template data from JSON", e);
            return new HashMap<>();
        }
    }
}
