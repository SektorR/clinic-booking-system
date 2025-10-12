package com.groundandgrow.controller;

import com.groundandgrow.dto.AppointmentDTO;
import com.groundandgrow.dto.ClientSummaryDTO;
import com.groundandgrow.dto.MessageDTO;
import com.groundandgrow.service.ClientManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for psychologists to manage their clients
 * All endpoints require JWT authentication with PSYCHOLOGIST role
 */
@Slf4j
@RestController
@RequestMapping("/psychologist/clients")
@PreAuthorize("hasRole('PSYCHOLOGIST') or hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Client Management", description = "Endpoints for psychologists to view and manage client information")
public class ClientController {

    private final ClientManagementService clientManagementService;

    /**
     * Get all clients who have had appointments with this psychologist
     * Returns a summary of each client including appointment counts and history
     */
    @GetMapping
    @Operation(summary = "Get all clients", description = "Get all clients who have had appointments with the psychologist")
    public ResponseEntity<List<ClientSummaryDTO>> getMyClients(Authentication authentication) {
        String psychologistId = authentication.getName();

        log.info("GET /api/psychologist/clients - Psychologist: {}", psychologistId);

        List<ClientSummaryDTO> clients = clientManagementService.getClients(psychologistId);
        return ResponseEntity.ok(clients);
    }

    /**
     * Get all appointments for a specific client
     * Client is identified by email address for guest bookings
     */
    @GetMapping("/{clientId}/appointments")
    @Operation(summary = "Get client appointments", description = "Get all appointments for a specific client")
    public ResponseEntity<List<AppointmentDTO>> getClientAppointments(
        @PathVariable String clientId,
        Authentication authentication
    ) {
        String psychologistId = authentication.getName();

        log.info("GET /api/psychologist/clients/{}/appointments - Psychologist: {}", clientId, psychologistId);

        List<AppointmentDTO> appointments = clientManagementService.getClientAppointments(clientId, psychologistId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get message history with a specific client
     */
    @GetMapping("/{clientId}/messages")
    @Operation(summary = "Get client messages", description = "Get message history with a specific client")
    public ResponseEntity<List<MessageDTO>> getClientMessages(
        @PathVariable String clientId,
        Authentication authentication
    ) {
        String psychologistId = authentication.getName();

        log.info("GET /api/psychologist/clients/{}/messages - Psychologist: {}", clientId, psychologistId);

        List<MessageDTO> messages = clientManagementService.getClientMessages(clientId, psychologistId);
        return ResponseEntity.ok(messages);
    }
}
