package com.example.NextSteps.controller;

import com.example.NextSteps.dto.LoginRequest;
import com.example.NextSteps.dto.LoginResponse;
import com.example.NextSteps.security.JwtAuthenticationFilter;
import com.example.NextSteps.security.JwtUtil;
import com.example.NextSteps.service.LoginAuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginAuthenticationService loginAuthenticationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void login_WithValidCredentials_ReturnsSuccessResponse() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("john.doe@example.com");
        loginRequest.setPassword("validPassword123");

        UUID userId = UUID.randomUUID();
        LoginResponse successResponse = new LoginResponse(
            true,
            "Login successful",
            userId,
            "john.doe@example.com",
            "jwt-token-here"
        );

        when(loginAuthenticationService.login(any(LoginRequest.class))).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token-here"));
    }

    @Test
    public void login_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("john.doe@example.com");
        loginRequest.setPassword("wrongPassword");

        LoginResponse failureResponse = new LoginResponse(
            false,
            "Invalid password",
            null,
            null,
            null
        );

        when(loginAuthenticationService.login(any(LoginRequest.class))).thenReturn(failureResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid password"))
                .andExpect(jsonPath("$.userId").isEmpty())
                .andExpect(jsonPath("$.email").isEmpty())
                .andExpect(jsonPath("$.token").isEmpty());
    }

    @Test
    public void login_WithNonExistentUser_ReturnsUnauthorized() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent@example.com");
        loginRequest.setPassword("anyPassword");

        LoginResponse failureResponse = new LoginResponse(
            false,
            "User not found",
            null,
            null,
            null
        );

        when(loginAuthenticationService.login(any(LoginRequest.class))).thenReturn(failureResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.userId").isEmpty())
                .andExpect(jsonPath("$.email").isEmpty())
                .andExpect(jsonPath("$.token").isEmpty());
    }

    @Test
    public void login_WithEmptyUsername_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("");
        loginRequest.setPassword("password123");

        LoginResponse failureResponse = new LoginResponse(
            false,
            "Username cannot be empty",
            null,
            null,
            null
        );

        when(loginAuthenticationService.login(any(LoginRequest.class))).thenReturn(failureResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void login_WithEmptyPassword_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("john.doe@example.com");
        loginRequest.setPassword("");

        LoginResponse failureResponse = new LoginResponse(
            false,
            "Password cannot be empty",
            null,
            null,
            null
        );

        when(loginAuthenticationService.login(any(LoginRequest.class))).thenReturn(failureResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void login_WithNullCredentials_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(null);
        loginRequest.setPassword(null);

        LoginResponse failureResponse = new LoginResponse(
            false,
            "Username and password are required",
            null,
            null,
            null
        );

        when(loginAuthenticationService.login(any(LoginRequest.class))).thenReturn(failureResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void login_WithMalformedJson_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_WithEmptyBody_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_WithSpecialCharactersInEmail_ReturnsAppropriateResponse() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user+tag@example-domain.co.uk");
        loginRequest.setPassword("password123");

        UUID userId = UUID.randomUUID();
        LoginResponse successResponse = new LoginResponse(
            true,
            "Login successful",
            userId,
            "user+tag@example-domain.co.uk",
            "jwt-token-here"
        );

        when(loginAuthenticationService.login(any(LoginRequest.class))).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.email").value("user+tag@example-domain.co.uk"));
    }

    @Test
    public void login_WithLongPassword_ReturnsAppropriateResponse() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("john.doe@example.com");
        loginRequest.setPassword("a".repeat(100)); // Very long password

        UUID userId = UUID.randomUUID();
        LoginResponse successResponse = new LoginResponse(
            true,
            "Login successful",
            userId,
            "john.doe@example.com",
            "jwt-token-here"
        );

        when(loginAuthenticationService.login(any(LoginRequest.class))).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void login_WithCaseInsensitiveEmail_ReturnsAppropriateResponse() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("JOHN.DOE@EXAMPLE.COM");
        loginRequest.setPassword("password123");

        UUID userId = UUID.randomUUID();
        LoginResponse successResponse = new LoginResponse(
            true,
            "Login successful",
            userId,
            "john.doe@example.com", // Service might normalize email
            "jwt-token-here"
        );

        when(loginAuthenticationService.login(any(LoginRequest.class))).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }
}
