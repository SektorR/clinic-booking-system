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
 * Messaging/Communication model for secure communication between clients and psychologists
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    // Sender and Receiver
    @Indexed
    private String senderId; // Could be client or psychologist ID

    @Indexed
    private String receiverId; // Could be client or psychologist ID

    private String senderType; // CLIENT, PSYCHOLOGIST, SYSTEM
    private String receiverType; // CLIENT, PSYCHOLOGIST

    // Message Content
    private String subject;
    private String content;

    // Related Appointment (optional)
    @Indexed
    private String appointmentId;

    // Status
    private Boolean isRead;
    private LocalDateTime readAt;

    // Attachments (optional, for future)
    private String attachmentUrl;
    private String attachmentType;

    // Thread/Conversation grouping
    @Indexed
    private String threadId; // Group messages in a conversation

    @CreatedDate
    private LocalDateTime createdAt;

    // Soft delete
    private Boolean deleted;
    private LocalDateTime deletedAt;
}
