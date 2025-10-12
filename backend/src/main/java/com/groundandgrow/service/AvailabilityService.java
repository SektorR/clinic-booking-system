package com.groundandgrow.service;

import com.groundandgrow.dto.AvailabilityDTO;
import com.groundandgrow.model.Availability;
import com.groundandgrow.model.GuestBooking;
import com.groundandgrow.model.TimeOff;
import com.groundandgrow.repository.AvailabilityRepository;
import com.groundandgrow.repository.GuestBookingRepository;
import com.groundandgrow.repository.TimeOffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for calculating psychologist availability
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final GuestBookingRepository guestBookingRepository;
    private final TimeOffRepository timeOffRepository;

    /**
     * Get available time slots for a psychologist on a specific date
     */
    public AvailabilityDTO getAvailableSlotsForDate(String psychologistId, LocalDate date, Integer durationMinutes) {
        log.info("Getting available slots for psychologist {} on {}", psychologistId, date);

        List<LocalDateTime> availableSlots = getAvailableSlots(psychologistId, date, durationMinutes);

        // Convert LocalDateTime slots to TimeSlot DTOs
        List<AvailabilityDTO.TimeSlot> timeSlots = availableSlots.stream()
                .map(startTime -> AvailabilityDTO.TimeSlot.builder()
                        .startTime(startTime)
                        .endTime(startTime.plusMinutes(durationMinutes))
                        .durationMinutes(durationMinutes)
                        .available(true)
                        .build())
                .collect(Collectors.toList());

        AvailabilityDTO dto = new AvailabilityDTO();
        dto.setPsychologistId(psychologistId);
        dto.setDate(date);
        dto.setDurationMinutes(durationMinutes);
        dto.setAvailableSlots(timeSlots);
        dto.setTotalSlots(timeSlots.size());

        return dto;
    }

    /**
     * Calculate available time slots
     */
    public List<LocalDateTime> getAvailableSlots(String psychologistId, LocalDate date, Integer durationMinutes) {
        List<LocalDateTime> slots = new ArrayList<>();

        // 1. Get recurring availability for the day of week
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<Availability> dayAvailability = availabilityRepository
                .findByPsychologistIdAndDayOfWeek(psychologistId, dayOfWeek);

        if (dayAvailability.isEmpty()) {
            log.info("No availability configured for psychologist {} on {}", psychologistId, dayOfWeek);
            return slots;
        }

        // 2. Check if date is in time off
        if (isDateInTimeOff(psychologistId, date)) {
            log.info("Psychologist {} has time off on {}", psychologistId, date);
            return slots;
        }

        // 3. Get existing bookings for the date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<GuestBooking> existingBookings = guestBookingRepository
                .findByPsychologistIdAndAppointmentDateTimeBetween(
                        psychologistId,
                        startOfDay,
                        endOfDay
                )
                .stream()
                .filter(booking -> "CONFIRMED".equals(booking.getBookingStatus()) ||
                                  "PENDING_PAYMENT".equals(booking.getBookingStatus()))
                .collect(Collectors.toList());

        // 4. Generate time slots from availability blocks
        for (Availability availability : dayAvailability) {
            // Check if availability is effective for this date
            if (!isAvailabilityEffective(availability, date)) {
                continue;
            }

            LocalDateTime slotStart = date.atTime(availability.getStartTime());
            LocalDateTime slotEnd = date.atTime(availability.getEndTime());

            // Generate slots within this availability block
            LocalDateTime currentSlot = slotStart;
            while (currentSlot.plusMinutes(durationMinutes).isBefore(slotEnd) ||
                   currentSlot.plusMinutes(durationMinutes).isEqual(slotEnd)) {

                // Check if slot conflicts with existing bookings
                if (!isSlotConflicting(currentSlot, durationMinutes, existingBookings)) {
                    // Only add future slots
                    if (currentSlot.isAfter(LocalDateTime.now())) {
                        slots.add(currentSlot);
                    }
                }

                currentSlot = currentSlot.plusMinutes(durationMinutes);
            }
        }

        log.info("Found {} available slots for psychologist {} on {}", slots.size(), psychologistId, date);
        return slots;
    }

    /**
     * Check if a specific slot is available
     */
    public boolean isSlotAvailable(String psychologistId, LocalDateTime startTime, Integer durationMinutes) {
        LocalDate date = startTime.toLocalDate();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // 1. Check if psychologist has availability on this day
        List<Availability> dayAvailability = availabilityRepository
                .findByPsychologistIdAndDayOfWeek(psychologistId, dayOfWeek);

        if (dayAvailability.isEmpty()) {
            return false;
        }

        // 2. Check if date is in time off
        if (isDateInTimeOff(psychologistId, date)) {
            return false;
        }

        // 3. Check if time is within any availability block
        LocalTime requestedTime = startTime.toLocalTime();
        LocalTime requestedEndTime = requestedTime.plusMinutes(durationMinutes);

        boolean withinAvailabilityBlock = false;
        for (Availability availability : dayAvailability) {
            if (!isAvailabilityEffective(availability, date)) {
                continue;
            }

            if ((requestedTime.isAfter(availability.getStartTime()) || requestedTime.equals(availability.getStartTime())) &&
                (requestedEndTime.isBefore(availability.getEndTime()) || requestedEndTime.equals(availability.getEndTime()))) {
                withinAvailabilityBlock = true;
                break;
            }
        }

        if (!withinAvailabilityBlock) {
            return false;
        }

        // 4. Check for conflicts with existing bookings
        List<GuestBooking> existingBookings = guestBookingRepository
                .findByPsychologistIdAndAppointmentDateTimeBetween(
                        psychologistId,
                        startTime.minusHours(2),
                        startTime.plusHours(2)
                )
                .stream()
                .filter(booking -> "CONFIRMED".equals(booking.getBookingStatus()) ||
                                  "PENDING_PAYMENT".equals(booking.getBookingStatus()))
                .collect(Collectors.toList());

        return !isSlotConflicting(startTime, durationMinutes, existingBookings);
    }

    /**
     * Check if a time slot conflicts with existing bookings
     */
    private boolean isSlotConflicting(LocalDateTime slotStart, Integer durationMinutes, List<GuestBooking> bookings) {
        LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);

        for (GuestBooking booking : bookings) {
            LocalDateTime bookingStart = booking.getAppointmentDateTime();
            LocalDateTime bookingEnd = bookingStart.plusMinutes(booking.getDurationMinutes());

            // Check for overlap
            if (slotStart.isBefore(bookingEnd) && slotEnd.isAfter(bookingStart)) {
                return true; // Conflict found
            }
        }

        return false; // No conflict
    }

    /**
     * Check if a date is in time off
     */
    private boolean isDateInTimeOff(String psychologistId, LocalDate date) {
        List<TimeOff> timeOffList = timeOffRepository.findByPsychologistId(psychologistId);

        for (TimeOff timeOff : timeOffList) {
            LocalDate timeOffStart = timeOff.getStartDateTime().toLocalDate();
            LocalDate timeOffEnd = timeOff.getEndDateTime().toLocalDate();

            if ((date.isAfter(timeOffStart) || date.isEqual(timeOffStart)) &&
                (date.isBefore(timeOffEnd) || date.isEqual(timeOffEnd))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if availability is effective for a given date
     */
    private boolean isAvailabilityEffective(Availability availability, LocalDate date) {
        // Check effectiveFrom
        if (availability.getEffectiveFrom() != null && date.isBefore(availability.getEffectiveFrom())) {
            return false;
        }

        // Check effectiveUntil
        if (availability.getEffectiveUntil() != null && date.isAfter(availability.getEffectiveUntil())) {
            return false;
        }

        return true;
    }

    /**
     * Get all availability for a psychologist
     */
    public List<Availability> getPsychologistAvailability(String psychologistId) {
        return availabilityRepository.findByPsychologistId(psychologistId);
    }

    /**
     * Get time off for a psychologist
     */
    public List<TimeOff> getPsychologistTimeOff(String psychologistId) {
        return timeOffRepository.findByPsychologistId(psychologistId);
    }
}
