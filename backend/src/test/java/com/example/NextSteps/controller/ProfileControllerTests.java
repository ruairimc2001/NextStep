package com.example.NextSteps.controller;

import com.example.NextSteps.dto.ProfileResponse;
import com.example.NextSteps.service.ProfileService;
import com.example.NextSteps.security.JwtUtil;
import com.example.NextSteps.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void getProfile_WhenProfileExists_ReturnsProfileResponse() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        List<String> skills = Arrays.asList("Java", "Spring Boot", "REST APIs");
        List<String> interests = Arrays.asList("Web Development", "Machine Learning");
        OffsetDateTime updatedAt = OffsetDateTime.now();

        ProfileResponse profileResponse = ProfileResponse.builder()
            .userId(userId)
            .email("john.doe@example.com")
            .firstName("John")
            .surname("Doe")
            .goalTitle("Software Developer")
            .skills(skills)
            .interests(interests)
            .updatedAt(updatedAt)
            .build();

        when(profileService.getProfileByUserId(userId)).thenReturn(Optional.of(profileResponse));

        // When & Then
        mockMvc.perform(get("/api/profile/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.goalTitle").value("Software Developer"))
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.skills[0]").value("Java"))
                .andExpect(jsonPath("$.skills[1]").value("Spring Boot"))
                .andExpect(jsonPath("$.skills[2]").value("REST APIs"))
                .andExpect(jsonPath("$.interests").isArray())
                .andExpect(jsonPath("$.interests[0]").value("Web Development"))
                .andExpect(jsonPath("$.interests[1]").value("Machine Learning"))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    public void getProfile_WhenProfileNotFound_ReturnsNotFound() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        when(profileService.getProfileByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/profile/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    @Test
    public void getProfile_WithInvalidUUID_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/profile/{userId}", "invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getProfile_WithEmptySkillsAndInterestsLists_ReturnsProfileResponse() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        OffsetDateTime updatedAt = OffsetDateTime.now();

        ProfileResponse profileResponse = ProfileResponse.builder()
            .userId(userId)
            .email("empty.lists@example.com")
            .firstName("Empty")
            .surname("Lists")
            .goalTitle("Test Role")
            .skills(Collections.emptyList()) // empty skills list
            .interests(Collections.emptyList()) // empty interests list
            .updatedAt(updatedAt)
            .build();

        when(profileService.getProfileByUserId(userId)).thenReturn(Optional.of(profileResponse));

        // When & Then
        mockMvc.perform(get("/api/profile/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("empty.lists@example.com"))
                .andExpect(jsonPath("$.firstName").value("Empty"))
                .andExpect(jsonPath("$.surname").value("Lists"))
                .andExpect(jsonPath("$.goalTitle").value("Test Role"))
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.skills").isEmpty())
                .andExpect(jsonPath("$.interests").isArray())
                .andExpect(jsonPath("$.interests").isEmpty())
                .andExpect(jsonPath("$.updatedAt").exists());
    }
}
