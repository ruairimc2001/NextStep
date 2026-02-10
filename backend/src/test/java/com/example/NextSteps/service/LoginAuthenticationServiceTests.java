package com.example.NextSteps.service;

import com.example.NextSteps.dto.LoginRequest;
import com.example.NextSteps.dto.LoginResponse;
import com.example.NextSteps.entities.User;
import com.example.NextSteps.repository.UserRepository;
import com.example.NextSteps.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginAuthenticationServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginAuthenticationService loginAuthenticationService;

    private User testUser;
    private LoginRequest validLoginRequest;
    private UUID userId;
    private String email;
    private String rawPassword;
    private String hashedPassword;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "john.doe@example.com";
        rawPassword = "validPassword123";
        hashedPassword = "$2a$10$hashed.password.here";
        jwtToken = "jwt-token-here";

        // Setup test user
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail(email);
        testUser.setPasswordHash(hashedPassword);

        // Setup valid login request
        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername(email);
        validLoginRequest.setPassword(rawPassword);
    }

    @Test
    void login_WithValidCredentials_ReturnsSuccessfulResponse() {
        // Given
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(userId, email)).thenReturn(jwtToken);

        // When
        LoginResponse response = loginAuthenticationService.login(validLoginRequest);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals(userId, response.getUserId());
        assertEquals(email, response.getEmail());
        assertEquals(jwtToken, response.getToken());

        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, hashedPassword);
        verify(jwtUtil).generateToken(userId, email);
    }

    @Test
    void login_WithNonExistentUser_ReturnsFailureResponse() {
        // Given
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        LoginResponse response = loginAuthenticationService.login(validLoginRequest);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());
        assertNull(response.getUserId());
        assertNull(response.getEmail());
        assertNull(response.getToken());

        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void login_WithInvalidPassword_ReturnsFailureResponse() {
        // Given
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(false);

        // When
        LoginResponse response = loginAuthenticationService.login(validLoginRequest);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("Invalid password", response.getMessage());
        assertNull(response.getUserId());
        assertNull(response.getEmail());
        assertNull(response.getToken());

        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, hashedPassword);
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void login_WithPlainTextPassword_ReturnsSuccessfulResponse() {
        // Given - User with plain text password (legacy)
        String plainTextPassword = "plainPassword123";
        testUser.setPasswordHash(plainTextPassword);
        validLoginRequest.setPassword(plainTextPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(userId, email)).thenReturn(jwtToken);

        // When
        LoginResponse response = loginAuthenticationService.login(validLoginRequest);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals(userId, response.getUserId());
        assertEquals(email, response.getEmail());
        assertEquals(jwtToken, response.getToken());

        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(any(), any()); // Should not use encoder for plain text
        verify(jwtUtil).generateToken(userId, email);
    }

    @Test
    void login_WithPlainTextPasswordButWrongPassword_ReturnsFailureResponse() {
        // Given - User with plain text password but wrong password provided
        String plainTextPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        testUser.setPasswordHash(plainTextPassword);
        validLoginRequest.setPassword(wrongPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        LoginResponse response = loginAuthenticationService.login(validLoginRequest);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("Invalid password", response.getMessage());
        assertNull(response.getUserId());
        assertNull(response.getEmail());
        assertNull(response.getToken());

        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void login_WithBCryptV2bHashedPassword_ReturnsSuccessfulResponse() {
        // Given - User with $2b$ BCrypt hash
        String bcryptV2bHash = "$2b$10$hashed.password.here";
        testUser.setPasswordHash(bcryptV2bHash);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, bcryptV2bHash)).thenReturn(true);
        when(jwtUtil.generateToken(userId, email)).thenReturn(jwtToken);

        // When
        LoginResponse response = loginAuthenticationService.login(validLoginRequest);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals(userId, response.getUserId());
        assertEquals(email, response.getEmail());
        assertEquals(jwtToken, response.getToken());

        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, bcryptV2bHash);
        verify(jwtUtil).generateToken(userId, email);
    }

    @Test
    void login_WithEmptyUsername_ReturnsFailureResponse() {
        // Given
        validLoginRequest.setUsername("");

        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        // When
        LoginResponse response = loginAuthenticationService.login(validLoginRequest);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());

        // Verify interactions
        verify(userRepository).findByEmail("");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void login_WithNullUsername_ReturnsFailureResponse() {
        // Given
        validLoginRequest.setUsername(null);

        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When
        LoginResponse response = loginAuthenticationService.login(validLoginRequest);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());

        // Verify interactions
        verify(userRepository).findByEmail(null);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void login_WithEmptyPassword_ReturnsFailureResponse() {
        // Given
        validLoginRequest.setPassword("");
        testUser.setPasswordHash("somePassword");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        LoginResponse response = loginAuthenticationService.login(validLoginRequest);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("Invalid password", response.getMessage());

        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void login_WithNullPassword_ThrowsNullPointerException() {
        // Given
        validLoginRequest.setPassword(null);
        testUser.setPasswordHash("somePassword");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When & Then - The service currently throws NPE for null passwords
        // This is a bug in the service that should be fixed to handle null gracefully
        assertThrows(NullPointerException.class, () -> {
            loginAuthenticationService.login(validLoginRequest);
        });

        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void login_WithSpecialCharactersInEmail_HandlesCorrectly() {
        // Given
        String specialEmail = "user+tag@example-domain.co.uk";
        validLoginRequest.setUsername(specialEmail);
        testUser.setEmail(specialEmail);

        when(userRepository.findByEmail(specialEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(userId, specialEmail)).thenReturn(jwtToken);

        // When
        LoginResponse response = loginAuthenticationService.login(validLoginRequest);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(specialEmail, response.getEmail());

        // Verify interactions
        verify(userRepository).findByEmail(specialEmail);
        verify(passwordEncoder).matches(rawPassword, hashedPassword);
        verify(jwtUtil).generateToken(userId, specialEmail);
    }

    @Test
    void login_WithCaseVariationsInEmail_CallsRepositoryWithExactCase() {
        // Given
        String upperCaseEmail = "JOHN.DOE@EXAMPLE.COM";
        validLoginRequest.setUsername(upperCaseEmail);

        when(userRepository.findByEmail(upperCaseEmail)).thenReturn(Optional.empty());

        // When
        LoginResponse response = loginAuthenticationService.login(validLoginRequest);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());

        // Verify repository is called with exact case provided
        verify(userRepository).findByEmail(upperCaseEmail);
    }

    @Test
    void login_WithLongPassword_HandlesCorrectly() {
        // Given
        String longPassword = "a".repeat(1000); // Very long password
        validLoginRequest.setPassword(longPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(longPassword, hashedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(userId, email)).thenReturn(jwtToken);

        // When
        LoginResponse response = loginAuthenticationService.login(validLoginRequest);

        // Then
        assertTrue(response.isSuccess());

        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(longPassword, hashedPassword);
        verify(jwtUtil).generateToken(userId, email);
    }

    @Test
    void login_WhenJwtGenerationReturnsNull_StillReturnsSuccessWithNullToken() {
        // Given
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(userId, email)).thenReturn(null);

        // When
        LoginResponse response = loginAuthenticationService.login(validLoginRequest);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals(userId, response.getUserId());
        assertEquals(email, response.getEmail());
        assertNull(response.getToken()); // Token is null but login is still successful

        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, hashedPassword);
        verify(jwtUtil).generateToken(userId, email);
    }

    @Test
    void login_MultipleCallsWithSameCredentials_CallsRepositoryEachTime() {
        // Given
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(userId, email)).thenReturn(jwtToken);

        // When
        loginAuthenticationService.login(validLoginRequest);
        loginAuthenticationService.login(validLoginRequest);
        loginAuthenticationService.login(validLoginRequest);

        // Then - Verify no caching, each call hits the repository
        verify(userRepository, times(3)).findByEmail(email);
        verify(passwordEncoder, times(3)).matches(rawPassword, hashedPassword);
        verify(jwtUtil, times(3)).generateToken(userId, email);
    }
}
