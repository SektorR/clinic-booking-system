package com.groundandgrow.controller;

import com.groundandgrow.dto.NotificationDTO;
import com.groundandgrow.dto.NotificationRequest;
import com.groundandgrow.model.Notification;
import com.groundandgrow.repository.NotificationRepository;
import com.groundandgrow.service.NotificationSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Internal controller for notification management
 * Protected - requires authentication
 */
@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Internal notification management endpoints")
public class NotificationController {

    private final NotificationSchedulerService notificationSchedulerService;
    private final NotificationRepository notificationRepository;

    /**
     * Schedule a notification to be sent
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('PSYCHOLOGIST') or hasRole('ADMIN')")
    @Operation(summary = "Send notification", description = "Schedule a notification for delivery")
    public ResponseEntity<NotificationDTO> sendNotification(@Valid @RequestBody NotificationRequest request) {
        Notification notification = notificationSchedulerService.scheduleNotification(
            request.getRecipientId(),
            request.getRecipientType(),
            request.getRecipientEmail(),
            request.getRecipientPhone(),
            request.getNotificationType(),
            request.getDeliveryMethod(),
            request.getSubject(),
            request.getMessage(),
            request.getScheduledFor(),
            request.getTemplateId(),
            request.getTemplateData()
        );

        if (request.getGuestBookingId() != null) {
            notification.setGuestBookingId(request.getGuestBookingId());
            notificationRepository.save(notification);
        }

        if (request.getAppointmentId() != null) {
            notification.setAppointmentId(request.getAppointmentId());
            notificationRepository.save(notification);
        }

        NotificationDTO dto = convertToDTO(notification);
        log.info("Notification scheduled: {} for {}", notification.getId(), request.getRecipientEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Get notification by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PSYCHOLOGIST') or hasRole('ADMIN')")
    @Operation(summary = "Get notification", description = "Retrieve notification status by ID")
    public ResponseEntity<NotificationDTO> getNotificationStatus(@PathVariable String id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        NotificationDTO dto = convertToDTO(notification);

        return ResponseEntity.ok(dto);
    }

    /**
     * Get all notifications for a guest booking
     */
    @GetMapping("/guest-booking/{bookingId}")
    @PreAuthorize("hasRole('PSYCHOLOGIST') or hasRole('ADMIN')")
    @Operation(summary = "Get booking notifications", description = "Retrieve all notifications for a specific guest booking")
    public ResponseEntity<List<NotificationDTO>> getBookingNotifications(@PathVariable String bookingId) {
        List<Notification> notifications = notificationRepository.findByGuestBookingId(bookingId);
        List<NotificationDTO> dtos = notifications.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all pending notifications
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending notifications", description = "Retrieve all pending notifications (admin only)")
    public ResponseEntity<List<NotificationDTO>> getPendingNotifications() {
        List<Notification> notifications = notificationRepository.findByStatus("PENDING");
        List<NotificationDTO> dtos = notifications.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all failed notifications
     */
    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get failed notifications", description = "Retrieve all failed notifications (admin only)")
    public ResponseEntity<List<NotificationDTO>> getFailedNotifications() {
        List<Notification> notifications = notificationRepository.findByStatus("FAILED");
        List<NotificationDTO> dtos = notifications.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Cancel a pending notification
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PSYCHOLOGIST') or hasRole('ADMIN')")
    @Operation(summary = "Cancel notification", description = "Cancel a pending notification")
    public ResponseEntity<Void> cancelNotification(@PathVariable String id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        if ("PENDING".equals(notification.getStatus())) {
            notification.setStatus("CANCELLED");
            notificationRepository.save(notification);
            log.info("Notification {} cancelled", id);
        } else {
            throw new RuntimeException("Only pending notifications can be cancelled");
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Retry a failed notification
     */
    @PostMapping("/{id}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retry notification", description = "Retry a failed notification (admin only)")
    public ResponseEntity<NotificationDTO> retryNotification(@PathVariable String id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        if ("FAILED".equals(notification.getStatus())) {
            notification.setStatus("PENDING");
            notification.setRetryCount(notification.getRetryCount() != null ? notification.getRetryCount() + 1 : 1);
            notification.setErrorMessage(null);
            notificationRepository.save(notification);
            log.info("Notification {} marked for retry", id);
        } else {
            throw new RuntimeException("Only failed notifications can be retried");
        }

        NotificationDTO dto = convertToDTO(notification);

        return ResponseEntity.ok(dto);
    }

    /**
     * Get all notifications for a specific recipient
     */
    @GetMapping("/recipient/{recipientId}")
    @PreAuthorize("hasRole('PSYCHOLOGIST') or hasRole('ADMIN')")
    @Operation(summary = "Get recipient notifications", description = "Retrieve all notifications for a specific recipient")
    public ResponseEntity<List<NotificationDTO>> getRecipientNotifications(@PathVariable String recipientId) {
        List<Notification> notifications = notificationRepository.findByRecipientId(recipientId);
        List<NotificationDTO> dtos = notifications.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Convert Notification entity to NotificationDTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
            .id(notification.getId())
            .recipientId(notification.getRecipientId())
            .recipientType(notification.getRecipientType())
            .recipientEmail(notification.getRecipientEmail())
            .recipientPhone(notification.getRecipientPhone())
            .guestBookingId(notification.getGuestBookingId())
            .notificationType(notification.getNotificationType())
            .deliveryMethod(notification.getDeliveryMethod())
            .subject(notification.getSubject())
            .message(notification.getMessage())
            .templateId(notification.getTemplateId())
            .scheduledFor(notification.getScheduledFor())
            .sentAt(notification.getSentAt())
            .status(notification.getStatus())
            .errorMessage(notification.getErrorMessage())
            .retryCount(notification.getRetryCount())
            .externalId(notification.getExternalId())
            .externalProvider(notification.getExternalProvider())
            .appointmentId(notification.getAppointmentId())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}
