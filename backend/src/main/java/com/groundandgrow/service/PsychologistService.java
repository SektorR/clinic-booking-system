package com.groundandgrow.service;

import com.groundandgrow.dto.*;
import com.groundandgrow.model.Psychologist;
import com.groundandgrow.model.GuestBooking;
import com.groundandgrow.repository.PsychologistRepository;
import com.groundandgrow.repository.GuestBookingRepository;
import com.groundandgrow.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for psychologist profile and dashboard management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PsychologistService {

    private final PsychologistRepository psychologistRepository;
    private final GuestBookingRepository guestBookingRepository;
    private final MessageRepository messageRepository;

    /**
     * Get all active psychologists
     */
    public List<PsychologistDTO> getAllActivePsychologists() {
        log.info("Fetching all active psychologists");
        List<Psychologist> psychologists = psychologistRepository.findByIsActive(true);
        return psychologists.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get psychologist profile by ID
     */
    public PsychologistDTO getPsychologistById(String psychologistId) {
        log.info("Fetching profile for psychologist: {}", psychologistId);
        Psychologist psychologist = psychologistRepository.findById(psychologistId)
                .orElseThrow(() -> new RuntimeException("Psychologist not found"));
        return mapToDTO(psychologist);
    }

    /**
     * Get psychologist profile by email or ID (authenticated)
     */
    public PsychologistDTO getProfile(String psychologistEmailOrId) {
        // Try to find by email first (for JWT authentication), then by ID
        Psychologist psychologist = psychologistRepository.findByEmail(psychologistEmailOrId)
            .orElseGet(() -> psychologistRepository.findById(psychologistEmailOrId)
                .orElseThrow(() -> new RuntimeException("Psychologist not found")));
        return mapToDTO(psychologist);
    }

    /**
     * Update psychologist profile
     */
    public PsychologistDTO updateProfile(String psychologistEmailOrId, UpdateProfileRequest request) {
        log.info("Updating profile for psychologist: {}", psychologistEmailOrId);

        // Try to find by email first (for JWT authentication), then by ID
        Psychologist psychologist = psychologistRepository.findByEmail(psychologistEmailOrId)
            .orElseGet(() -> psychologistRepository.findById(psychologistEmailOrId)
                .orElseThrow(() -> new RuntimeException("Psychologist not found")));

        if (request.getFirstName() != null) {
            psychologist.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            psychologist.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            psychologist.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            psychologist.setPhone(request.getPhone());
        }
        if (request.getSpecialization() != null) {
            psychologist.setSpecialization(request.getSpecialization());
        }
        if (request.getBio() != null) {
            psychologist.setBio(request.getBio());
        }

        Psychologist updated = psychologistRepository.save(psychologist);
        log.info("Profile updated successfully for psychologist: {}", updated.getId());
        return mapToDTO(updated);
    }

    /**
     * Get dashboard data including today's appointments, upcoming appointments, and stats
     * @param psychologistEmailOrId - can be email or ID
     */
    public DashboardDTO getDashboard(String psychologistEmailOrId) {
        log.info("Fetching dashboard for psychologist: {}", psychologistEmailOrId);

        // Try to find by email first (for JWT authentication), then by ID
        Psychologist psychologist = psychologistRepository.findByEmail(psychologistEmailOrId)
            .orElseGet(() -> psychologistRepository.findById(psychologistEmailOrId)
                .orElseThrow(() -> new RuntimeException("Psychologist not found")));

        String psychologistId = psychologist.getId();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        LocalDateTime weekEnd = todayStart.plusDays(7);

        List<GuestBooking> todayBookings = guestBookingRepository
            .findByPsychologistIdAndAppointmentDateTimeBetween(psychologistId, todayStart, todayEnd);

        List<GuestBooking> upcomingBookings = guestBookingRepository
            .findByPsychologistIdAndAppointmentDateTimeBetween(psychologistId, todayEnd, weekEnd);

        DashboardDTO.DashboardStats stats = calculateStats(psychologistId);

        return DashboardDTO.builder()
            .psychologist(mapToDTO(psychologist))
            .todayAppointments(todayBookings.stream()
                .map(this::mapBookingToAppointmentDTO)
                .collect(Collectors.toList()))
            .upcomingAppointments(upcomingBookings.stream()
                .map(this::mapBookingToAppointmentDTO)
                .collect(Collectors.toList()))
            .stats(stats)
            .build();
    }

    /**
     * Get appointments for a psychologist with optional filters
     */
    public List<AppointmentDTO> getAppointments(String psychologistEmailOrId, LocalDate startDate,
                                                 LocalDate endDate, String status) {
        log.info("Fetching appointments for psychologist: {} with filters - start: {}, end: {}, status: {}",
                psychologistEmailOrId, startDate, endDate, status);

        // Get the psychologist ID
        Psychologist psychologist = psychologistRepository.findByEmail(psychologistEmailOrId)
            .orElseGet(() -> psychologistRepository.findById(psychologistEmailOrId)
                .orElseThrow(() -> new RuntimeException("Psychologist not found")));
        String psychologistId = psychologist.getId();

        List<GuestBooking> bookings;

        if (startDate != null && endDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(23, 59, 59);
            bookings = guestBookingRepository
                .findByPsychologistIdAndAppointmentDateTimeBetween(psychologistId, start, end);
        } else {
            bookings = guestBookingRepository.findByPsychologistId(psychologistId);
        }

        if (status != null && !status.isEmpty()) {
            bookings = bookings.stream()
                .filter(b -> status.equalsIgnoreCase(b.getBookingStatus()))
                .collect(Collectors.toList());
        }

        return bookings.stream()
            .map(this::mapBookingToAppointmentDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get specific appointment details
     */
    public AppointmentDTO getAppointmentDetails(String appointmentId, String psychologistId) {
        log.info("Fetching appointment details: {} for psychologist: {}", appointmentId, psychologistId);

        GuestBooking booking = guestBookingRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Verify that the appointment belongs to this psychologist
        if (!booking.getPsychologistId().equals(psychologistId)) {
            throw new RuntimeException("Unauthorized: This appointment does not belong to you");
        }

        return mapBookingToAppointmentDTO(booking);
    }

    /**
     * Update appointment status (e.g., mark as completed, no-show)
     */
    public AppointmentDTO updateAppointmentStatus(String appointmentId, String status, String reason) {
        log.info("Updating appointment {} status to: {}", appointmentId, status);

        GuestBooking booking = guestBookingRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));

        booking.setBookingStatus(status.toLowerCase());
        if (reason != null && !reason.isEmpty()) {
            booking.setCancellationReason(reason);
        }

        GuestBooking updated = guestBookingRepository.save(booking);
        log.info("Appointment {} status updated to: {}", appointmentId, status);
        return mapBookingToAppointmentDTO(updated);
    }

    /**
     * Add notes to an appointment
     */
    public AppointmentDTO addNotes(String appointmentId, String notes) {
        log.info("Adding notes to appointment: {}", appointmentId);

        GuestBooking booking = guestBookingRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));

        booking.setPsychologistNotes(notes);

        GuestBooking updated = guestBookingRepository.save(booking);
        log.info("Notes added successfully to appointment: {}", appointmentId);
        return mapBookingToAppointmentDTO(updated);
    }

    // Helper methods

    private DashboardDTO.DashboardStats calculateStats(String psychologistId) {
        log.debug("Calculating statistics for psychologist: {}", psychologistId);

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();

        List<GuestBooking> allBookings = guestBookingRepository.findByPsychologistId(psychologistId);

        List<GuestBooking> thisMonth = allBookings.stream()
            .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().isAfter(monthStart))
            .collect(Collectors.toList());

        List<GuestBooking> thisWeek = allBookings.stream()
            .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().isAfter(weekStart))
            .collect(Collectors.toList());

        long unreadMessages = messageRepository.findByReceiverIdAndIsReadOrderByCreatedAtDesc(psychologistId, false).size();

        return DashboardDTO.DashboardStats.builder()
            .totalSessions(Long.valueOf(allBookings.size()))
            .pendingBookings(allBookings.stream()
                .filter(b -> "confirmed".equalsIgnoreCase(b.getBookingStatus()))
                .count())
            .completedThisWeek(thisWeek.stream()
                .filter(b -> "completed".equalsIgnoreCase(b.getBookingStatus()))
                .count())
            .completedThisMonth(thisMonth.stream()
                .filter(b -> "completed".equalsIgnoreCase(b.getBookingStatus()))
                .count())
            .cancelledThisMonth(thisMonth.stream()
                .filter(b -> "cancelled".equalsIgnoreCase(b.getBookingStatus()))
                .count())
            .noShowsThisMonth(thisMonth.stream()
                .filter(b -> "no_show".equalsIgnoreCase(b.getBookingStatus()))
                .count())
            .unreadMessages((int) unreadMessages)
            .build();
    }

    private AppointmentDTO mapBookingToAppointmentDTO(GuestBooking booking) {
        String patientName = booking.getFirstName() + " " + booking.getLastName();

        return AppointmentDTO.builder()
            .id(booking.getId())
            .firstName(booking.getFirstName())
            .lastName(booking.getLastName())
            .patientName(patientName)
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

    private PsychologistDTO mapToDTO(Psychologist psychologist) {
        PsychologistDTO dto = new PsychologistDTO();
        dto.setId(psychologist.getId());
        dto.setFirstName(psychologist.getFirstName());
        dto.setLastName(psychologist.getLastName());
        dto.setEmail(psychologist.getEmail());
        dto.setPhone(psychologist.getPhone());
        dto.setSpecialization(psychologist.getSpecialization());
        dto.setRegistrationNumber(psychologist.getRegistrationNumber());
        dto.setBio(psychologist.getBio());
        dto.setRole(psychologist.getRole().name());
        dto.setIsActive(psychologist.getIsActive());
        return dto;
    }
}
