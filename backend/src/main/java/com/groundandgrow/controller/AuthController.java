package com.groundandgrow.controller;

import com.groundandgrow.dto.LoginRequest;
import com.groundandgrow.dto.LoginResponse;
import com.groundandgrow.dto.PsychologistDTO;
import com.groundandgrow.dto.RegisterRequest;
import com.groundandgrow.model.Psychologist;
import com.groundandgrow.security.JwtTokenProvider;
import com.groundandgrow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints for psychologists")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate psychologist and get JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register psychologist", description = "Register a new psychologist (admin only)")
    public ResponseEntity<PsychologistDTO> register(@Valid @RequestBody RegisterRequest request) {
        Psychologist psychologist = authService.register(request);

        PsychologistDTO dto = PsychologistDTO.builder()
                .id(psychologist.getId())
                .firstName(psychologist.getFirstName())
                .lastName(psychologist.getLastName())
                .email(psychologist.getEmail())
                .phone(psychologist.getPhone())
                .specialization(psychologist.getSpecialization())
                .registrationNumber(psychologist.getRegistrationNumber())
                .bio(psychologist.getBio())
                .role(psychologist.getRole().name())
                .isActive(psychologist.getIsActive())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh JWT token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String email = tokenProvider.getEmailFromToken(token);

        LoginResponse response = authService.refreshToken(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate JWT token")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        boolean isValid = tokenProvider.validateToken(token);

        if (isValid) {
            return ResponseEntity.ok("Token is valid");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid");
        }
    }
}
