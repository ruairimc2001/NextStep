package com.example.NextSteps.controller;

import com.example.NextSteps.dto.roadmap.RoadMapDto;
import com.example.NextSteps.dto.roadmap.generation.GenerationDetails;
import com.example.NextSteps.dto.roadmap.stage.Stage;
import com.example.NextSteps.entities.Profile;
import com.example.NextSteps.entities.Roadmap;
import com.example.NextSteps.entities.User;
import com.example.NextSteps.security.JwtAuthenticationFilter;
import com.example.NextSteps.security.JwtUtil;
import com.example.NextSteps.service.RoadmapService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RoadmapController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RoadmapControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoadmapService roadmapService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void generateRoadmap_WithValidProfile_ReturnsRoadMapDto() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        Profile profile = createTestProfile(userId);
        RoadMapDto expectedRoadmap = createTestRoadMapDto(userId);

        when(roadmapService.generateAndSaveRoadmap(any(Profile.class))).thenReturn(expectedRoadmap);

        // When & Then
        mockMvc.perform(post("/api/roadmaps/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.roadmapId").value(expectedRoadmap.getRoadmapId()))
                .andExpect(jsonPath("$.userId").value(expectedRoadmap.getUserId()))
                .andExpect(jsonPath("$.targetRole").value(expectedRoadmap.getTargetRole()))
                .andExpect(jsonPath("$.summary").value(expectedRoadmap.getSummary()))
                .andExpect(jsonPath("$.stages").isArray())
                .andExpect(jsonPath("$.stages").isNotEmpty())
                .andExpect(jsonPath("$.stages[0].stageId").value("stage-1"))
                .andExpect(jsonPath("$.stages[0].title").value("Foundation"))
                .andExpect(jsonPath("$.stages[1].stageId").value("stage-2"))
                .andExpect(jsonPath("$.stages[1].title").value("Advanced Topics"));
    }

    @Test
    public void getUserRoadmaps_WhenUserHasRoadmaps_ReturnsRoadmapList() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        List<Roadmap> roadmaps = Arrays.asList(
                createTestRoadmap(UUID.randomUUID(), "Software Developer", userId),
                createTestRoadmap(UUID.randomUUID(), "Data Scientist", userId)
        );

        when(roadmapService.getRoadmapsByUserId(userId)).thenReturn(roadmaps);

        // When & Then
        mockMvc.perform(get("/api/roadmaps/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(roadmaps.get(0).getId().toString()))
                .andExpect(jsonPath("$[0].title").value("Software Developer"))
                .andExpect(jsonPath("$[1].id").value(roadmaps.get(1).getId().toString()))
                .andExpect(jsonPath("$[1].title").value("Data Scientist"));
    }

    @Test
    public void getUserRoadmaps_WhenUserHasNoRoadmaps_ReturnsEmptyList() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        when(roadmapService.getRoadmapsByUserId(userId)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/roadmaps/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void getUserRoadmaps_WithInvalidUUID_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/roadmaps/user/{userId}", "invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getRoadmap_WhenRoadmapExists_ReturnsRoadMapDto() throws Exception {
        // Given
        UUID roadmapId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RoadMapDto roadmapDto = createTestRoadMapDto(userId);

        when(roadmapService.getRoadmapById(roadmapId)).thenReturn(Optional.of(roadmapDto));

        // When & Then
        mockMvc.perform(get("/api/roadmaps/{roadmapId}", roadmapId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.roadmapId").value(roadmapDto.getRoadmapId()))
                .andExpect(jsonPath("$.userId").value(roadmapDto.getUserId()))
                .andExpect(jsonPath("$.targetRole").value(roadmapDto.getTargetRole()))
                .andExpect(jsonPath("$.summary").value(roadmapDto.getSummary()))
                .andExpect(jsonPath("$.stages").isArray())
                .andExpect(jsonPath("$.stages").isNotEmpty());
    }

    @Test
    public void getRoadmap_WhenRoadmapNotFound_ReturnsNotFound() throws Exception {
        // Given
        UUID roadmapId = UUID.randomUUID();
        when(roadmapService.getRoadmapById(roadmapId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/roadmaps/{roadmapId}", roadmapId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getRoadmap_WithInvalidUUID_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/roadmaps/{roadmapId}", "invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateRoadmap_WithEmptySkillsAndInterests_ReturnsRoadMapDto() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        Profile profile = createTestProfile(userId);
        profile.setSkills(Collections.emptyList());
        profile.setInterests(Collections.emptyList());

        RoadMapDto expectedRoadmap = createTestRoadMapDto(userId);

        when(roadmapService.generateAndSaveRoadmap(any(Profile.class))).thenReturn(expectedRoadmap);

        // When & Then
        mockMvc.perform(post("/api/roadmaps/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.roadmapId").exists())
                .andExpect(jsonPath("$.targetRole").value("Software Developer"))
                .andExpect(jsonPath("$.stages").isArray());
    }

    @Test
    public void generateRoadmap_WithDifferentTargetRole_ReturnsAppropriateRoadmap() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        Profile profile = createTestProfile(userId);
        profile.setGoalTitle("Data Scientist");

        RoadMapDto expectedRoadmap = createTestRoadMapDto(userId);
        expectedRoadmap.setTargetRole("Data Scientist");
        expectedRoadmap.setSummary("A comprehensive roadmap to become a Data Scientist");

        when(roadmapService.generateAndSaveRoadmap(any(Profile.class))).thenReturn(expectedRoadmap);

        // When & Then
        mockMvc.perform(post("/api/roadmaps/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.targetRole").value("Data Scientist"))
                .andExpect(jsonPath("$.summary").value("A comprehensive roadmap to become a Data Scientist"));
    }

    // Helper methods for creating test data
    private Profile createTestProfile(UUID userId) {
        Profile profile = new Profile();
        profile.setUserId(userId);

        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        profile.setUser(user);

        profile.setFirstName("John");
        profile.setSurname("Doe");
        profile.setGoalTitle("Software Developer");
        profile.setSkills(Arrays.asList("Java", "Spring Boot", "REST APIs"));
        profile.setInterests(Arrays.asList("Web Development", "Machine Learning"));
        profile.setUpdatedAt(OffsetDateTime.now());

        return profile;
    }

    private RoadMapDto createTestRoadMapDto(UUID userId) {
        RoadMapDto roadMapDto = new RoadMapDto();
        roadMapDto.setRoadmapId(UUID.randomUUID().toString());
        roadMapDto.setUserId(userId.toString());
        roadMapDto.setTargetRole("Software Developer");
        roadMapDto.setSummary("A comprehensive roadmap to become a Software Developer");
        roadMapDto.setCreatedAt(OffsetDateTime.now());
        roadMapDto.setUpdatedAt(OffsetDateTime.now());

        GenerationDetails generationDetails = new GenerationDetails();
        roadMapDto.setGenerationDetails(generationDetails);

        // Create test stages
        Stage stage1 = new Stage();
        stage1.setStageId("stage-1");
        stage1.setOrder(1);
        stage1.setTitle("Foundation");
        stage1.setDescription("Learn programming fundamentals");

        Stage stage2 = new Stage();
        stage2.setStageId("stage-2");
        stage2.setOrder(2);
        stage2.setTitle("Advanced Topics");
        stage2.setDescription("Master advanced concepts");

        roadMapDto.setStages(Arrays.asList(stage1, stage2));

        return roadMapDto;
    }

    private Roadmap createTestRoadmap(UUID roadmapId, String title, UUID userId) {
        Roadmap roadmap = new Roadmap();
        roadmap.setId(roadmapId);
        roadmap.setTitle(title);
        roadmap.setCreatedAt(OffsetDateTime.now());
        roadmap.setRawAiOutput("{\"roadmapId\":\"" + roadmapId + "\",\"targetRole\":\"" + title + "\"}");

        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        roadmap.setUser(user);

        return roadmap;
    }
}
