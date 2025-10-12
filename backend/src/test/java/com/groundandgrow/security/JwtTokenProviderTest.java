package com.groundandgrow.security;

import com.groundandgrow.model.Psychologist;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String jwtSecret = "test-secret-key-for-jwt-token-must-be-at-least-256-bits-long-for-security";
    private final long jwtExpiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", jwtExpiration);
    }

    @Test
    void testGenerateToken() {
        // Given
        Psychologist psychologist = Psychologist.builder()
                .id("test-id")
                .email("test@example.com")
                .password("password")
                .role(Psychologist.Role.PSYCHOLOGIST)
                .build();

        UserPrincipal userPrincipal = UserPrincipal.create(psychologist);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // When
        String token = tokenProvider.generateToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void testGenerateTokenFromEmail() {
        // Given
        String email = "test@example.com";

        // When
        String token = tokenProvider.generateTokenFromEmail(email);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void testGetEmailFromToken() {
        // Given
        String email = "test@example.com";
        String token = tokenProvider.generateTokenFromEmail(email);

        // When
        String extractedEmail = tokenProvider.getEmailFromToken(token);

        // Then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void testValidateToken_ValidToken() {
        // Given
        String email = "test@example.com";
        String token = tokenProvider.generateTokenFromEmail(email);

        // When
        boolean isValid = tokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = tokenProvider.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void testValidateToken_ExpiredToken() {
        // Given - Create an expired token
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("test@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .expiration(new Date(System.currentTimeMillis() - 3600000)) // Expired 1 hour ago
                .signWith(key)
                .compact();

        // When
        boolean isValid = tokenProvider.validateToken(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void testValidateToken_MalformedToken() {
        // Given
        String malformedToken = "not-a-valid-jwt";

        // When
        boolean isValid = tokenProvider.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }
}
