package com.groundandgrow.controller;

import com.groundandgrow.dto.AvailabilityRequest;
import com.groundandgrow.dto.RecurringAvailabilityDTO;
import com.groundandgrow.dto.TimeOffDTO;
import com.groundandgrow.dto.TimeOffRequest;
import com.groundandgrow.service.AvailabilityManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for psychologist availability and schedule management
 * All endpoints require JWT authentication with PSYCHOLOGIST role
 */
@Slf4j
@RestController
@RequestMapping("/psychologist/availability")
@PreAuthorize("hasRole('PSYCHOLOGIST') or hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Availability Management", description = "Endpoints for managing psychologist availability and time off")
public class AvailabilityController {

    private final AvailabilityManagementService availabilityManagementService;

    /**
     * Get all recurring availability for the authenticated psychologist
     */
    @GetMapping
    @Operation(summary = "Get availability", description = "Get all recurring availability slots")
    public ResponseEntity<List<RecurringAvailabilityDTO>> getMyAvailability(Authentication authentication) {
        String psychologistId = authentication.getName();

        log.info("GET /api/psychologist/availability - Psychologist: {}", psychologistId);
        List<RecurringAvailabilityDTO> availability = availabilityManagementService.getAvailability(psychologistId);
        return ResponseEntity.ok(availability);
    }

    /**
     * Add new recurring availability
     */
    @PostMapping
    @Operation(summary = "Add availability", description = "Add a new recurring availability slot")
    public ResponseEntity<RecurringAvailabilityDTO> addAvailability(
        @Valid @RequestBody AvailabilityRequest request,
        Authentication authentication
    ) {
        String psychologistId = authentication.getName();

        log.info("POST /api/psychologist/availability - Psychologist: {}, Day: {}, Time: {}-{}",
            psychologistId, request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

        RecurringAvailabilityDTO created = availabilityManagementService.addAvailability(psychologistId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update existing availability
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update availability", description = "Update an existing recurring availability slot")
    public ResponseEntity<RecurringAvailabilityDTO> updateAvailability(
        @PathVariable String id,
        @Valid @RequestBody AvailabilityRequest request,
        Authentication authentication
    ) {
        String psychologistId = authentication.getName();

        log.info("PUT /api/psychologist/availability/{} - Psychologist: {}", id, psychologistId);

        RecurringAvailabilityDTO updated = availabilityManagementService.updateAvailability(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete availability
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete availability", description = "Delete a recurring availability slot")
    public ResponseEntity<Void> deleteAvailability(
        @PathVariable String id,
        Authentication authentication
    ) {
        String psychologistId = authentication.getName();

        log.info("DELETE /api/psychologist/availability/{} - Psychologist: {}", id, psychologistId);

        availabilityManagementService.deleteAvailability(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all time off for the authenticated psychologist
     */
    @GetMapping("/time-off")
    @Operation(summary = "Get time off", description = "Get all scheduled time off periods")
    public ResponseEntity<List<TimeOffDTO>> getTimeOff(Authentication authentication) {
        String psychologistId = authentication.getName();

        log.info("GET /api/psychologist/availability/time-off - Psychologist: {}", psychologistId);

        List<TimeOffDTO> timeOff = availabilityManagementService.getTimeOff(psychologistId);
        return ResponseEntity.ok(timeOff);
    }

    /**
     * Add time off
     */
    @PostMapping("/time-off")
    @Operation(summary = "Add time off", description = "Schedule a new time off period")
    public ResponseEntity<TimeOffDTO> addTimeOff(
        @Valid @RequestBody TimeOffRequest request,
        Authentication authentication
    ) {
        String psychologistId = authentication.getName();

        log.info("POST /api/psychologist/availability/time-off - Psychologist: {}, Period: {} to {}",
            psychologistId, request.getStartDateTime(), request.getEndDateTime());

        TimeOffDTO created = availabilityManagementService.addTimeOff(psychologistId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Delete time off
     */
    @DeleteMapping("/time-off/{id}")
    @Operation(summary = "Delete time off", description = "Delete a scheduled time off period")
    public ResponseEntity<Void> deleteTimeOff(
        @PathVariable String id,
        Authentication authentication
    ) {
        String psychologistId = authentication.getName();

        log.info("DELETE /api/psychologist/availability/time-off/{} - Psychologist: {}", id, psychologistId);

        availabilityManagementService.deleteTimeOff(id);
        return ResponseEntity.noContent().build();
    }
}
