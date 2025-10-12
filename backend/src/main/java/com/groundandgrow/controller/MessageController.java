package com.groundandgrow.controller;

import com.groundandgrow.dto.MessageDTO;
import com.groundandgrow.dto.MessageRequest;
import com.groundandgrow.model.Message;
import com.groundandgrow.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for secure messaging between psychologists and clients
 */
@Slf4j
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Secure messaging endpoints")
public class MessageController {

    private final MessageService messageService;

    /**
     * Send a new message
     */
    @PostMapping
    @Operation(summary = "Send a message", description = "Send a message to another user")
    public ResponseEntity<MessageDTO> sendMessage(@Valid @RequestBody MessageRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String senderId = authentication.getName(); // Get psychologist ID from JWT

        // Determine sender type based on authentication
        String senderType = "PSYCHOLOGIST"; // Default for authenticated users

        Message message = messageService.sendMessage(
            senderId,
            request.getReceiverId(),
            senderType,
            request.getReceiverType(),
            request.getSubject(),
            request.getContent(),
            request.getAppointmentId()
        );

        MessageDTO dto = convertToDTO(message);
        log.info("Message sent from {} to {}", senderId, request.getReceiverId());

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Get all messages in a thread
     */
    @GetMapping("/thread/{threadId}")
    @Operation(summary = "Get thread messages", description = "Retrieve all messages in a conversation thread")
    public ResponseEntity<List<MessageDTO>> getThreadMessages(@PathVariable String threadId) {
        List<Message> messages = messageService.getThreadMessages(threadId);
        List<MessageDTO> dtos = messages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get unread messages for the authenticated user
     */
    @GetMapping("/unread")
    @Operation(summary = "Get unread messages", description = "Retrieve all unread messages for the authenticated user")
    public ResponseEntity<List<MessageDTO>> getUnreadMessages() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        List<Message> messages = messageService.getUnreadMessages(userId);
        List<MessageDTO> dtos = messages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all messages for the authenticated user
     */
    @GetMapping
    @Operation(summary = "Get user messages", description = "Retrieve all messages for the authenticated user")
    public ResponseEntity<List<MessageDTO>> getUserMessages() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        List<Message> messages = messageService.getUserMessages(userId);
        List<MessageDTO> dtos = messages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get messages for a specific appointment
     */
    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Get appointment messages", description = "Retrieve all messages related to a specific appointment")
    public ResponseEntity<List<MessageDTO>> getAppointmentMessages(@PathVariable String appointmentId) {
        List<Message> messages = messageService.getAppointmentMessages(appointmentId);
        List<MessageDTO> dtos = messages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Mark a message as read
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "Mark message as read", description = "Mark a specific message as read")
    public ResponseEntity<MessageDTO> markAsRead(@PathVariable String id) {
        Message message = messageService.markAsRead(id);
        MessageDTO dto = convertToDTO(message);

        return ResponseEntity.ok(dto);
    }

    /**
     * Get unread message count
     */
    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count", description = "Get the count of unread messages for the authenticated user")
    public ResponseEntity<Long> getUnreadCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        long count = messageService.countUnreadMessages(userId);

        return ResponseEntity.ok(count);
    }

    /**
     * Delete a message (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete message", description = "Soft delete a message")
    public ResponseEntity<Void> deleteMessage(@PathVariable String id) {
        messageService.deleteMessage(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Convert Message entity to MessageDTO
     */
    private MessageDTO convertToDTO(Message message) {
        return MessageDTO.builder()
            .id(message.getId())
            .senderId(message.getSenderId())
            .receiverId(message.getReceiverId())
            .senderType(message.getSenderType())
            .receiverType(message.getReceiverType())
            .subject(message.getSubject())
            .content(message.getContent())
            .appointmentId(message.getAppointmentId())
            .threadId(message.getThreadId())
            .isRead(message.getIsRead())
            .readAt(message.getReadAt())
            .createdAt(message.getCreatedAt())
            .build();
    }
}
