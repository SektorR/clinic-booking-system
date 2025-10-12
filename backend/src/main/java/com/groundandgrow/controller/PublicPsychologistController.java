package com.groundandgrow.controller;

import com.groundandgrow.dto.AvailabilityDTO;
import com.groundandgrow.dto.PsychologistDTO;
import com.groundandgrow.dto.SessionTypeDTO;
import com.groundandgrow.service.AvailabilityService;
import com.groundandgrow.service.PsychologistService;
import com.groundandgrow.service.SessionTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Public API for psychologist information (no authentication required)
 */
@RestController
@RequestMapping("/public/psychologists")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicPsychologistController {

    private final PsychologistService psychologistService;
    private final AvailabilityService availabilityService;
    private final SessionTypeService sessionTypeService;

    /**
     * Get all active psychologists
     */
    @GetMapping
    public ResponseEntity<List<PsychologistDTO>> getAllActivePsychologists() {
        List<PsychologistDTO> psychologists = psychologistService.getAllActivePsychologists();
        return ResponseEntity.ok(psychologists);
    }

    /**
     * Get psychologist by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PsychologistDTO> getPsychologistById(@PathVariable String id) {
        PsychologistDTO psychologist = psychologistService.getPsychologistById(id);
        return ResponseEntity.ok(psychologist);
    }

    /**
     * Get available time slots for a psychologist on a specific date
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityDTO> getAvailability(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "60") Integer durationMinutes) {
        AvailabilityDTO availability = availabilityService.getAvailableSlotsForDate(id, date, durationMinutes);
        return ResponseEntity.ok(availability);
    }

    /**
     * Get all active session types
     */
    @GetMapping("/session-types")
    public ResponseEntity<List<SessionTypeDTO>> getAllSessionTypes() {
        List<SessionTypeDTO> sessionTypes = sessionTypeService.getAllActiveSessionTypes();
        return ResponseEntity.ok(sessionTypes);
    }

    /**
     * Get session types by modality
     */
    @GetMapping("/session-types/modality/{modality}")
    public ResponseEntity<List<SessionTypeDTO>> getSessionTypesByModality(@PathVariable String modality) {
        List<SessionTypeDTO> sessionTypes = sessionTypeService.getSessionTypesByModality(modality);
        return ResponseEntity.ok(sessionTypes);
    }
}
