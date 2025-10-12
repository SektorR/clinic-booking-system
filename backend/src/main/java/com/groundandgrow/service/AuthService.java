package com.groundandgrow.service;

import com.groundandgrow.dto.LoginRequest;
import com.groundandgrow.dto.LoginResponse;
import com.groundandgrow.dto.RegisterRequest;
import com.groundandgrow.model.Psychologist;
import com.groundandgrow.repository.PsychologistRepository;
import com.groundandgrow.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PsychologistRepository psychologistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /**
     * Authenticate psychologist and return JWT token
     */
    public LoginResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = tokenProvider.generateToken(authentication);

        // Get psychologist details
        Psychologist psychologist = psychologistRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Psychologist not found"));

        log.info("Psychologist logged in: {}", psychologist.getEmail());

        return new LoginResponse(
                jwt,
                psychologist.getId(),
                psychologist.getEmail(),
                psychologist.getFirstName(),
                psychologist.getLastName(),
                psychologist.getRole().name()
        );
    }

    /**
     * Register new psychologist (admin only)
     */
    public Psychologist register(RegisterRequest request) {
        // Check if email already exists
        if (psychologistRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Create psychologist
        Psychologist psychologist = Psychologist.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .specialization(request.getSpecialization())
                .registrationNumber(request.getRegistrationNumber())
                .bio(request.getBio())
                .role(request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")
                        ? Psychologist.Role.ADMIN
                        : Psychologist.Role.PSYCHOLOGIST)
                .isActive(true)
                .build();

        psychologist = psychologistRepository.save(psychologist);

        log.info("New psychologist registered: {}", psychologist.getEmail());

        return psychologist;
    }

    /**
     * Refresh JWT token
     */
    public LoginResponse refreshToken(String email) {
        Psychologist psychologist = psychologistRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Psychologist not found"));

        String jwt = tokenProvider.generateTokenFromEmail(email);

        return new LoginResponse(
                jwt,
                psychologist.getId(),
                psychologist.getEmail(),
                psychologist.getFirstName(),
                psychologist.getLastName(),
                psychologist.getRole().name()
        );
    }
}
