package com.groundandgrow.service;

import com.groundandgrow.model.GuestBooking;
import com.groundandgrow.model.Message;
import com.groundandgrow.repository.GuestBookingRepository;
import com.groundandgrow.repository.MessageRepository;
import com.groundandgrow.dto.AppointmentDTO;
import com.groundandgrow.dto.ClientSummaryDTO;
import com.groundandgrow.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for psychologists to view and manage client information
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientManagementService {

    private final GuestBookingRepository guestBookingRepository;
    private final MessageRepository messageRepository;

    /**
     * Get all clients who have had appointments with this psychologist
     * Groups guest bookings by email to create client summaries
     */
    public List<ClientSummaryDTO> getClients(String psychologistId) {
        log.info("Fetching clients for psychologist: {}", psychologistId);

        List<GuestBooking> allBookings = guestBookingRepository.findByPsychologistId(psychologistId);

        // Group bookings by email (since guests don't have accounts, we use email as identifier)
        Map<String, List<GuestBooking>> clientBookings = allBookings.stream()
            .collect(Collectors.groupingBy(GuestBooking::getEmail));

        return clientBookings.entrySet().stream()
            .map(entry -> createClientSummary(entry.getKey(), entry.getValue(), psychologistId))
            .filter(Objects::nonNull)
            .sorted((a, b) -> {
                if (b.getLastAppointmentDate() != null && a.getLastAppointmentDate() != null) {
                    return b.getLastAppointmentDate().compareTo(a.getLastAppointmentDate());
                }
                return 0;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get all appointments for a specific client (identified by email for guest bookings)
     */
    public List<AppointmentDTO> getClientAppointments(String clientEmail, String psychologistId) {
        log.info("Fetching appointments for client {} and psychologist {}", clientEmail, psychologistId);

        // For guest bookings, clientId is the email address
        List<GuestBooking> bookings = guestBookingRepository.findByEmail(clientEmail).stream()
            .filter(b -> psychologistId.equals(b.getPsychologistId()))
            .collect(Collectors.toList());

        return bookings.stream()
            .map(this::mapToAppointmentDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get message history with a specific client
     */
    public List<MessageDTO> getClientMessages(String clientEmail, String psychologistId) {
        log.info("Fetching messages between psychologist {} and client {}", psychologistId, clientEmail);

        // Find all appointments between this psychologist and client
        List<GuestBooking> bookings = guestBookingRepository.findByEmail(clientEmail).stream()
            .filter(b -> psychologistId.equals(b.getPsychologistId()))
            .collect(Collectors.toList());

        // Get all message threads for these appointments
        List<Message> messages = new ArrayList<>();
        for (GuestBooking booking : bookings) {
            if (booking.getId() != null) {
                List<Message> appointmentMessages = messageRepository.findByAppointmentIdOrderByCreatedAtAsc(booking.getId());
                messages.addAll(appointmentMessages);
            }
        }

        return messages.stream()
            .map(this::mapToMessageDTO)
            .sorted(Comparator.comparing(MessageDTO::getCreatedAt))
            .collect(Collectors.toList());
    }

    // Helper methods

    private ClientSummaryDTO createClientSummary(String email, List<GuestBooking> bookings, String psychologistId) {
        GuestBooking mostRecent = bookings.stream()
            .max(Comparator.comparing(booking ->
                booking.getCreatedAt() != null ? booking.getCreatedAt() : LocalDateTime.MIN))
            .orElse(null);

        if (mostRecent == null) {
            return null;
        }

        long completed = bookings.stream()
            .filter(b -> "completed".equalsIgnoreCase(b.getBookingStatus()))
            .count();

        long cancelled = bookings.stream()
            .filter(b -> "cancelled".equalsIgnoreCase(b.getBookingStatus()))
            .count();

        LocalDateTime lastAppt = bookings.stream()
            .filter(b -> "completed".equalsIgnoreCase(b.getBookingStatus()))
            .map(GuestBooking::getAppointmentDateTime)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        LocalDateTime nextAppt = bookings.stream()
            .filter(b -> "confirmed".equalsIgnoreCase(b.getBookingStatus()))
            .map(GuestBooking::getAppointmentDateTime)
            .filter(dt -> dt != null && dt.isAfter(LocalDateTime.now()))
            .min(LocalDateTime::compareTo)
            .orElse(null);

        // Count unread messages for this client
        int unreadMessages = (int) bookings.stream()
            .flatMap(booking -> messageRepository.findByAppointmentIdOrderByCreatedAtAsc(booking.getId()).stream())
            .filter(msg -> psychologistId.equals(msg.getReceiverId()) && Boolean.FALSE.equals(msg.getIsRead()))
            .count();

        return ClientSummaryDTO.builder()
            .id(email) // Using email as ID for guest clients
            .firstName(mostRecent.getFirstName())
            .lastName(mostRecent.getLastName())
            .email(email)
            .phone(mostRecent.getPhone())
            .totalAppointments(bookings.size())
            .completedAppointments((int) completed)
            .cancelledAppointments((int) cancelled)
            .lastAppointmentDate(lastAppt)
            .nextAppointmentDate(nextAppt)
            .unreadMessages(unreadMessages)
            .build();
    }

    private AppointmentDTO mapToAppointmentDTO(GuestBooking booking) {
        return AppointmentDTO.builder()
            .id(booking.getId())
            .firstName(booking.getFirstName())
            .lastName(booking.getLastName())
            .email(booking.getEmail())
            .phone(booking.getPhone())
            .psychologistId(booking.getPsychologistId())
            .sessionTypeId(booking.getSessionTypeId())
            .appointmentDateTime(booking.getAppointmentDateTime())
            .durationMinutes(booking.getDurationMinutes())
            .modality(booking.getModality())
            .amount(booking.getAmount())
            .paymentStatus(booking.getPaymentStatus())
            .bookingStatus(booking.getBookingStatus())
            .notes(booking.getNotes())
            .psychologistNotes(booking.getPsychologistNotes())
            .cancellationReason(booking.getCancellationReason())
            .meetingLink(booking.getMeetingLink())
            .roomNumber(booking.getRoomNumber())
            .reminderSent(booking.getReminderSent())
            .createdAt(booking.getCreatedAt())
            .updatedAt(booking.getUpdatedAt())
            .build();
    }

    private MessageDTO mapToMessageDTO(Message message) {
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
