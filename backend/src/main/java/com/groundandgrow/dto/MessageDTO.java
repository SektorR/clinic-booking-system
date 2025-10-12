package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for message response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    private String id;
    private String senderId;
    private String receiverId;
    private String senderType;
    private String receiverType;
    private String subject;
    private String content;
    private String appointmentId;
    private String threadId;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    // Additional fields for display
    private String senderName;
    private String receiverName;
}
