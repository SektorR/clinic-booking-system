package com.groundandgrow.service;

import com.groundandgrow.model.Availability;
import com.groundandgrow.model.TimeOff;
import com.groundandgrow.repository.AvailabilityRepository;
import com.groundandgrow.repository.TimeOffRepository;
import com.groundandgrow.dto.AvailabilityRequest;
import com.groundandgrow.dto.RecurringAvailabilityDTO;
import com.groundandgrow.dto.TimeOffDTO;
import com.groundandgrow.dto.TimeOffRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing psychologist availability and time off
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityManagementService {

    private final AvailabilityRepository availabilityRepository;
    private final TimeOffRepository timeOffRepository;

    /**
     * Get all recurring availability for a psychologist
     */
    public List<RecurringAvailabilityDTO> getAvailability(String psychologistId) {
        log.info("Fetching availability for psychologist: {}", psychologistId);

        List<Availability> availabilities = availabilityRepository.findByPsychologistId(psychologistId);
        return availabilities.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Add new recurring availability
     */
    public RecurringAvailabilityDTO addAvailability(String psychologistId, AvailabilityRequest request) {
        log.info("Adding availability for psychologist {}: {} {} - {}",
            psychologistId, request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

        // Parse the day of week string to DayOfWeek enum
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());

        Availability availability = Availability.builder()
            .psychologistId(psychologistId)
            .dayOfWeek(dayOfWeek)
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : true)
            .effectiveFrom(request.getEffectiveFrom())
            .effectiveUntil(request.getEffectiveUntil())
            .build();

        Availability saved = availabilityRepository.save(availability);
        log.info("Availability added successfully: {}", saved.getId());
        return mapToDTO(saved);
    }

    /**
     * Update existing availability
     */
    public RecurringAvailabilityDTO updateAvailability(String availabilityId, AvailabilityRequest request) {
        log.info("Updating availability: {}", availabilityId);

        Availability availability = availabilityRepository.findById(availabilityId)
            .orElseThrow(() -> new RuntimeException("Availability not found"));

        // Parse the day of week string to DayOfWeek enum
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());

        availability.setDayOfWeek(dayOfWeek);
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setIsRecurring(request.getIsRecurring());
        availability.setEffectiveFrom(request.getEffectiveFrom());
        availability.setEffectiveUntil(request.getEffectiveUntil());

        Availability updated = availabilityRepository.save(availability);
        log.info("Availability updated successfully: {}", availabilityId);
        return mapToDTO(updated);
    }

    /**
     * Delete availability
     */
    public void deleteAvailability(String availabilityId) {
        log.info("Deleting availability: {}", availabilityId);

        if (!availabilityRepository.existsById(availabilityId)) {
            throw new RuntimeException("Availability not found");
        }

        availabilityRepository.deleteById(availabilityId);
        log.info("Availability deleted successfully: {}", availabilityId);
    }

    /**
     * Get all time off for a psychologist
     */
    public List<TimeOffDTO> getTimeOff(String psychologistId) {
        log.info("Fetching time off for psychologist: {}", psychologistId);

        List<TimeOff> timeOffs = timeOffRepository.findByPsychologistId(psychologistId);
        return timeOffs.stream()
            .map(this::mapTimeOffToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Add time off
     */
    public TimeOffDTO addTimeOff(String psychologistId, TimeOffRequest request) {
        log.info("Adding time off for psychologist {}: {} to {}",
            psychologistId, request.getStartDateTime(), request.getEndDateTime());

        TimeOff timeOff = TimeOff.builder()
            .psychologistId(psychologistId)
            .startDateTime(request.getStartDateTime())
            .endDateTime(request.getEndDateTime())
            .reason(request.getReason())
            .createdAt(LocalDateTime.now())
            .build();

        TimeOff saved = timeOffRepository.save(timeOff);
        log.info("Time off added successfully: {}", saved.getId());
        return mapTimeOffToDTO(saved);
    }

    /**
     * Delete time off
     */
    public void deleteTimeOff(String timeOffId) {
        log.info("Deleting time off: {}", timeOffId);

        if (!timeOffRepository.existsById(timeOffId)) {
            throw new RuntimeException("Time off not found");
        }

        timeOffRepository.deleteById(timeOffId);
        log.info("Time off deleted successfully: {}", timeOffId);
    }

    // Helper methods

    private RecurringAvailabilityDTO mapToDTO(Availability availability) {
        return RecurringAvailabilityDTO.builder()
            .id(availability.getId())
            .psychologistId(availability.getPsychologistId())
            .dayOfWeek(availability.getDayOfWeek().toString())
            .startTime(availability.getStartTime())
            .endTime(availability.getEndTime())
            .isRecurring(availability.getIsRecurring())
            .effectiveFrom(availability.getEffectiveFrom())
            .effectiveUntil(availability.getEffectiveUntil())
            .build();
    }

    private TimeOffDTO mapTimeOffToDTO(TimeOff timeOff) {
        return TimeOffDTO.builder()
            .id(timeOff.getId())
            .psychologistId(timeOff.getPsychologistId())
            .startDateTime(timeOff.getStartDateTime())
            .endDateTime(timeOff.getEndDateTime())
            .reason(timeOff.getReason())
            .createdAt(timeOff.getCreatedAt())
            .build();
    }
}
