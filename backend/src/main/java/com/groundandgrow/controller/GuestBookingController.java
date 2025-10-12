package com.groundandgrow.controller;

import com.groundandgrow.dto.CancellationResponse;
import com.groundandgrow.dto.CheckoutSessionResponse;
import com.groundandgrow.dto.GuestBookingDTO;
import com.groundandgrow.dto.GuestBookingRequest;
import com.groundandgrow.dto.RescheduleRequest;
import com.groundandgrow.service.GuestBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controller for guest booking management (no authentication required)
 */
@Slf4j
@RestController
@RequestMapping("/public/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GuestBookingController {

    private final GuestBookingService guestBookingService;

    /**
     * Create a new guest booking and return Stripe checkout URL
     */
    @PostMapping
    public ResponseEntity<CheckoutSessionResponse> createBooking(@Valid @RequestBody GuestBookingRequest request) {
        log.info("Creating guest booking for: {} {}", request.getFirstName(), request.getLastName());
        CheckoutSessionResponse response = guestBookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get booking details by confirmation token
     */
    @GetMapping("/{token}")
    public ResponseEntity<GuestBookingDTO> getBookingByToken(@PathVariable String token) {
        log.info("Retrieving booking with token: {}", token);
        GuestBookingDTO booking = guestBookingService.getBookingByToken(token);
        return ResponseEntity.ok(booking);
    }

    /**
     * Cancel a booking by confirmation token
     */
    @PutMapping("/{token}/cancel")
    public ResponseEntity<CancellationResponse> cancelBooking(@PathVariable String token) {
        log.info("Cancelling booking with token: {}", token);
        CancellationResponse response = guestBookingService.cancelBooking(token);
        return ResponseEntity.ok(response);
    }

    /**
     * Reschedule a booking by confirmation token
     */
    @PutMapping("/{token}/reschedule")
    public ResponseEntity<GuestBookingDTO> rescheduleBooking(
            @PathVariable String token,
            @Valid @RequestBody RescheduleRequest request) {
        log.info("Rescheduling booking with token: {}", token);
        GuestBookingDTO booking = guestBookingService.rescheduleBooking(token, request);
        return ResponseEntity.ok(booking);
    }

    /**
     * Get all bookings by email (for guest to view their booking history)
     */
    @GetMapping("/by-email/{email}")
    public ResponseEntity<?> getBookingsByEmail(@PathVariable String email) {
        log.info("Retrieving bookings for email: {}", email);
        var bookings = guestBookingService.getBookingsByEmail(email);
        return ResponseEntity.ok(bookings);
    }
}
