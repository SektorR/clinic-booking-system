package com.groundandgrow.repository;

import com.groundandgrow.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Message entity
 */
@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    /**
     * Find all messages in a thread
     */
    List<Message> findByThreadIdOrderByCreatedAtAsc(String threadId);

    /**
     * Find unread messages for a receiver
     */
    List<Message> findByReceiverIdAndIsReadOrderByCreatedAtDesc(String receiverId, Boolean isRead);

    /**
     * Find all messages for a specific appointment
     */
    List<Message> findByAppointmentIdOrderByCreatedAtAsc(String appointmentId);

    /**
     * Find all messages where user is sender or receiver
     */
    List<Message> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(String senderId, String receiverId);
}
