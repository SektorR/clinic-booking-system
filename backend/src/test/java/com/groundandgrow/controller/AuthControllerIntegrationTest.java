package com.groundandgrow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groundandgrow.dto.LoginRequest;
import com.groundandgrow.dto.RegisterRequest;
import com.groundandgrow.model.Psychologist;
import com.groundandgrow.repository.PsychologistRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PsychologistRepository psychologistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Psychologist testPsychologist;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // Use embedded MongoDB for testing
        registry.add("spring.data.mongodb.database", () -> "groundandgrow-test");
    }

    @BeforeEach
    void setUp() {
        // Clean database
        psychologistRepository.deleteAll();

        // Create test psychologist
        testPsychologist = Psychologist.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password(passwordEncoder.encode("password123"))
                .phone("+61412345678")
                .specialization("Clinical Psychology")
                .registrationNumber("PSY12345")
                .bio("Experienced psychologist")
                .role(Psychologist.Role.PSYCHOLOGIST)
                .isActive(true)
                .build();

        testPsychologist = psychologistRepository.save(testPsychologist);
    }

    @AfterEach
    void tearDown() {
        psychologistRepository.deleteAll();
    }

    @Test
    void testLogin_Success() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("john.doe@example.com", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("PSYCHOLOGIST"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("john.doe@example.com", "wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_UserNotFound() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_InvalidEmail() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("invalid-email", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_EmptyPassword() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("john.doe@example.com", "");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testValidateToken_ValidToken() throws Exception {
        // Given - First login to get a valid token
        LoginRequest loginRequest = new LoginRequest("john.doe@example.com", "password123");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Token is valid"));
    }

    @Test
    void testValidateToken_InvalidToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }
}
