package com.groundandgrow.service;

import com.groundandgrow.model.Message;
import com.groundandgrow.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing secure messaging between psychologists and clients
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final NotificationSchedulerService notificationSchedulerService;

    /**
     * Send a message
     */
    public Message sendMessage(
            String senderId,
            String receiverId,
            String senderType,
            String receiverType,
            String subject,
            String content,
            String appointmentId) {

        // Create thread ID if this is a new conversation
        String threadId = createThreadId(senderId, receiverId, appointmentId);

        Message message = Message.builder()
            .senderId(senderId)
            .receiverId(receiverId)
            .senderType(senderType)
            .receiverType(receiverType)
            .subject(subject)
            .content(content)
            .appointmentId(appointmentId)
            .threadId(threadId)
            .isRead(false)
            .deleted(false)
            .createdAt(LocalDateTime.now())
            .build();

        Message savedMessage = messageRepository.save(message);
        log.info("Message sent from {} to {}", senderId, receiverId);

        // Notify recipient via email
        notifyRecipient(savedMessage);

        return savedMessage;
    }

    /**
     * Get all messages in a thread
     */
    public List<Message> getThreadMessages(String threadId) {
        return messageRepository.findByThreadIdOrderByCreatedAtAsc(threadId);
    }

    /**
     * Mark a message as read
     */
    public Message markAsRead(String messageId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());

        log.info("Message {} marked as read", messageId);
        return messageRepository.save(message);
    }

    /**
     * Get unread messages for a user
     */
    public List<Message> getUnreadMessages(String userId) {
        return messageRepository.findByReceiverIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }

    /**
     * Get all messages for a user (sent or received)
     */
    public List<Message> getUserMessages(String userId) {
        return messageRepository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId);
    }

    /**
     * Get messages for a specific appointment
     */
    public List<Message> getAppointmentMessages(String appointmentId) {
        return messageRepository.findByAppointmentIdOrderByCreatedAtAsc(appointmentId);
    }

    /**
     * Soft delete a message
     */
    public void deleteMessage(String messageId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setDeleted(true);
        message.setDeletedAt(LocalDateTime.now());

        messageRepository.save(message);
        log.info("Message {} soft deleted", messageId);
    }

    /**
     * Create a thread ID for a conversation
     * Format: "{appointmentId}_{userId1}_{userId2}" (sorted)
     */
    public String createThreadId(String senderId, String receiverId, String appointmentId) {
        if (appointmentId != null && !appointmentId.isEmpty()) {
            // Use appointment-based thread ID
            return appointmentId + "_" + sortIds(senderId, receiverId);
        } else {
            // Use user-based thread ID
            return "thread_" + sortIds(senderId, receiverId);
        }
    }

    /**
     * Sort two IDs to ensure consistent thread ID generation
     */
    private String sortIds(String id1, String id2) {
        if (id1.compareTo(id2) < 0) {
            return id1 + "_" + id2;
        } else {
            return id2 + "_" + id1;
        }
    }

    /**
     * Notify recipient of new message via email
     */
    private void notifyRecipient(Message message) {
        try {
            // TODO: Get recipient email and name from database
            String recipientEmail = "recipient@example.com"; // Placeholder
            String recipientName = "Recipient"; // Placeholder
            String senderName = "Sender"; // Placeholder

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("recipientName", recipientName);
            templateData.put("senderName", senderName);
            templateData.put("subject", message.getSubject());
            templateData.put("messageContent", message.getContent());
            templateData.put("messageLink", "http://localhost:5173/messages/" + message.getThreadId());

            notificationSchedulerService.scheduleNotification(
                message.getReceiverId(),
                message.getReceiverType(),
                recipientEmail,
                null, // No SMS for messages
                "MESSAGE_RECEIVED",
                "EMAIL",
                "New Message - Ground & Grow Psychology",
                "You have received a new message",
                LocalDateTime.now(), // Send immediately
                "email/message-notification",
                templateData
            );

            log.info("Message notification scheduled for recipient {}", message.getReceiverId());
        } catch (Exception e) {
            log.error("Failed to send message notification", e);
            // Don't throw exception - message was still sent successfully
        }
    }

    /**
     * Count unread messages for a user
     */
    public long countUnreadMessages(String userId) {
        return messageRepository.findByReceiverIdAndIsReadOrderByCreatedAtDesc(userId, false).size();
    }
}
