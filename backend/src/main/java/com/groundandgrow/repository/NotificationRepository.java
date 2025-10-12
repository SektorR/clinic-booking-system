package com.groundandgrow.repository;

import com.groundandgrow.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Notification entity
 */
@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    /**
     * Find notifications that are pending and scheduled before a given time
     */
    List<Notification> findByStatusAndScheduledForBefore(String status, LocalDateTime time);

    /**
     * Find all notifications for a specific guest booking
     */
    List<Notification> findByGuestBookingId(String guestBookingId);

    /**
     * Find notifications by recipient ID and status
     */
    List<Notification> findByRecipientIdAndStatus(String recipientId, String status);

    /**
     * Find all notifications for a recipient
     */
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);

    /**
     * Find all notifications by status
     */
    List<Notification> findByStatus(String status);

    /**
     * Find all notifications by recipient ID
     */
    List<Notification> findByRecipientId(String recipientId);
}
