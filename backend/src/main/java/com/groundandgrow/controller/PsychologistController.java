package com.groundandgrow.controller;

import com.groundandgrow.dto.*;
import com.groundandgrow.service.PsychologistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for psychologist profile and appointment management
 * All endpoints require JWT authentication with PSYCHOLOGIST role
 */
@Slf4j
@RestController
@RequestMapping("/psychologist")
@PreAuthorize("hasRole('PSYCHOLOGIST') or hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Psychologist Portal", description = "Endpoints for psychologist profile and appointment management")
public class PsychologistController {

    private final PsychologistService psychologistService;

    /**
     * Get authenticated psychologist's profile
     */
    @GetMapping("/profile")
    @Operation(summary = "Get profile", description = "Get the authenticated psychologist's profile")
    public ResponseEntity<PsychologistDTO> getProfile(Authentication authentication) {
        String psychologistId = authentication.getName();

        log.info("GET /api/psychologist/profile - Psychologist: {}", psychologistId);
        PsychologistDTO profile = psychologistService.getProfile(psychologistId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update psychologist profile
     */
    @PutMapping("/profile")
    @Operation(summary = "Update profile", description = "Update the authenticated psychologist's profile")
    public ResponseEntity<PsychologistDTO> updateProfile(
        @Valid @RequestBody UpdateProfileRequest request,
        Authentication authentication
    ) {
        String psychologistId = authentication.getName();

        log.info("PUT /api/psychologist/profile - Psychologist: {}", psychologistId);
        PsychologistDTO updated = psychologistService.updateProfile(psychologistId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get dashboard with today's appointments, upcoming appointments, and statistics
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard", description = "Get dashboard with appointments and statistics")
    public ResponseEntity<DashboardDTO> getDashboard(Authentication authentication) {
        String psychologistId = authentication.getName();

        log.info("GET /api/psychologist/dashboard - Psychologist: {}", psychologistId);
        DashboardDTO dashboard = psychologistService.getDashboard(psychologistId);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get appointments with optional filters
     *
     * @param startDate Filter by start date (inclusive)
     * @param endDate Filter by end date (inclusive)
     * @param status Filter by booking status (confirmed, cancelled, completed, no_show)
     */
    @GetMapping("/appointments")
    @Operation(summary = "Get appointments", description = "Get all appointments with optional filters")
    public ResponseEntity<List<AppointmentDTO>> getAppointments(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) String status,
        Authentication authentication
    ) {
        String psychologistId = authentication.getName();

        log.info("GET /api/psychologist/appointments - Psychologist: {}, Filters: startDate={}, endDate={}, status={}",
            psychologistId, startDate, endDate, status);

        List<AppointmentDTO> appointments = psychologistService.getAppointments(
            psychologistId, startDate, endDate, status);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get specific appointment details
     */
    @GetMapping("/appointments/{id}")
    @Operation(summary = "Get appointment details", description = "Get details of a specific appointment")
    public ResponseEntity<AppointmentDTO> getAppointmentDetails(
        @PathVariable String id,
        Authentication authentication
    ) {
        String psychologistId = authentication.getName();

        log.info("GET /api/psychologist/appointments/{} - Psychologist: {}", id, psychologistId);
        AppointmentDTO appointment = psychologistService.getAppointmentDetails(id, psychologistId);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Update appointment status
     * Allowed statuses: COMPLETED, NO_SHOW, CANCELLED
     */
    @PutMapping("/appointments/{id}/status")
    @Operation(summary = "Update appointment status", description = "Update the status of an appointment (e.g., completed, no-show)")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
        @PathVariable String id,
        @Valid @RequestBody UpdateStatusRequest request,
        Authentication authentication
    ) {
        String psychologistId = authentication.getName();

        log.info("PUT /api/psychologist/appointments/{}/status - Psychologist: {}, New status: {}",
            id, psychologistId, request.getStatus());

        AppointmentDTO updated = psychologistService.updateAppointmentStatus(
            id, request.getStatus(), request.getReason());
        return ResponseEntity.ok(updated);
    }

    /**
     * Add or update notes for an appointment
     */
    @PostMapping("/appointments/{id}/notes")
    @Operation(summary = "Add notes", description = "Add or update psychologist notes for an appointment")
    public ResponseEntity<AppointmentDTO> addNotes(
        @PathVariable String id,
        @Valid @RequestBody NotesRequest request,
        Authentication authentication
    ) {
        String psychologistId = authentication.getName();

        log.info("POST /api/psychologist/appointments/{}/notes - Psychologist: {}", id, psychologistId);

        AppointmentDTO updated = psychologistService.addNotes(id, request.getNotes());
        return ResponseEntity.ok(updated);
    }
}
