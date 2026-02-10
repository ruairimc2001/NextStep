package com.example.NextSteps.service;

import com.example.NextSteps.dto.roadmap.RoadMapDto;
import com.example.NextSteps.dto.roadmap.generation.AiRoadMapProvider;
import com.example.NextSteps.dto.roadmap.generation.GenerationDetails;
import com.example.NextSteps.dto.roadmap.stage.Stage;
import com.example.NextSteps.entities.Profile;
import com.example.NextSteps.entities.Roadmap;
import com.example.NextSteps.entities.User;
import com.example.NextSteps.repository.ProfileRepository;
import com.example.NextSteps.repository.RoadmapRepository;
import com.example.NextSteps.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoadmapServiceTests {

    @Mock
    private AiRoadMapProvider aiRoadMapProvider;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RoadmapService roadmapService;

    private Profile testProfile;
    private User testUser;
    private RoadMapDto testRoadMapDto;
    private Roadmap testRoadmap;
    private UUID userId;
    private UUID roadmapId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roadmapId = UUID.randomUUID();

        // Setup test user
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        // Setup test profile
        testProfile = new Profile();
        testProfile.setUserId(userId);
        testProfile.setUser(testUser);
        testProfile.setFirstName("John");
        testProfile.setSurname("Doe");
        testProfile.setGoalTitle("Software Developer");
        testProfile.setSkills(Arrays.asList("Java", "Spring Boot"));
        testProfile.setInterests(Collections.singletonList("Web Development"));
        testProfile.setUpdatedAt(OffsetDateTime.now());

        // Setup test roadmap DTO
        testRoadMapDto = createTestRoadMapDto();

        // Setup test roadmap entity
        testRoadmap = createTestRoadmap();
    }

    @Test
    void generateAndSaveRoadmap_WithValidProfile_GeneratesAndSavesRoadmap() throws JsonProcessingException {
        // Given
        String serializedRoadmap = "{\"roadmapId\":\"test\"}";

        when(aiRoadMapProvider.generateRoadmap(testProfile)).thenReturn(testRoadMapDto);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(objectMapper.writeValueAsString(testRoadMapDto)).thenReturn(serializedRoadmap);
        when(roadmapRepository.save(any(Roadmap.class))).thenReturn(testRoadmap);

        // When
        RoadMapDto result = roadmapService.generateAndSaveRoadmap(testProfile);

        // Then
        assertNotNull(result);
        assertEquals(testRoadMapDto.getTargetRole(), result.getTargetRole());
        assertEquals(testRoadMapDto.getSummary(), result.getSummary());

        // Verify AI provider was called
        verify(aiRoadMapProvider).generateRoadmap(testProfile);

        // Verify user lookup
        verify(userRepository).findById(userId);

        // Verify roadmap was saved
        ArgumentCaptor<Roadmap> roadmapCaptor = ArgumentCaptor.forClass(Roadmap.class);
        verify(roadmapRepository).save(roadmapCaptor.capture());

        Roadmap savedRoadmap = roadmapCaptor.getValue();
        assertNotNull(savedRoadmap.getId());
        assertEquals(testUser, savedRoadmap.getUser());
        assertEquals(testRoadMapDto.getTargetRole(), savedRoadmap.getTitle());
        assertEquals(serializedRoadmap, savedRoadmap.getRawAiOutput());
        assertNotNull(savedRoadmap.getCreatedAt());

        // Verify serialization
        verify(objectMapper).writeValueAsString(testRoadMapDto);
    }

    @Test
    void generateAndSaveRoadmap_WhenUserNotFound_ThrowsException() {
        // Given
        when(aiRoadMapProvider.generateRoadmap(testProfile)).thenReturn(testRoadMapDto);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> roadmapService.generateAndSaveRoadmap(testProfile));

        assertEquals("User not found", exception.getMessage());
        verify(aiRoadMapProvider).generateRoadmap(testProfile);
        verify(userRepository).findById(userId);
        verify(roadmapRepository, never()).save(any());
    }

    @Test
    void generateAndSaveRoadmap_WhenSerializationFails_ThrowsException() throws JsonProcessingException {
        // Given
        when(aiRoadMapProvider.generateRoadmap(testProfile)).thenReturn(testRoadMapDto);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(objectMapper.writeValueAsString(testRoadMapDto))
            .thenThrow(new JsonProcessingException("Serialization failed") {});

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> roadmapService.generateAndSaveRoadmap(testProfile));

        assertEquals("Failed to serialize roadmap", exception.getMessage());
        verify(roadmapRepository, never()).save(any());
    }

    @Test
    void getRoadmapsByUserId_WithValidUserId_ReturnsRoadmaps() {
        // Given
        List<Roadmap> expectedRoadmaps = Arrays.asList(testRoadmap, createTestRoadmap());
        when(roadmapRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(expectedRoadmaps);

        // When
        List<Roadmap> result = roadmapService.getRoadmapsByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedRoadmaps, result);
        verify(roadmapRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void getRoadmapsByUserId_WhenNoRoadmaps_ReturnsEmptyList() {
        // Given
        when(roadmapRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Collections.emptyList());

        // When
        List<Roadmap> result = roadmapService.getRoadmapsByUserId(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roadmapRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void getRoadmapById_WithValidId_ReturnsRoadMapDto() throws JsonProcessingException {
        // Given
        String rawAiOutput = "{\"roadmapId\":\"test\"}";
        testRoadmap.setRawAiOutput(rawAiOutput);

        when(roadmapRepository.findById(roadmapId)).thenReturn(Optional.of(testRoadmap));
        when(objectMapper.readValue(rawAiOutput, RoadMapDto.class)).thenReturn(testRoadMapDto);

        // When
        Optional<RoadMapDto> result = roadmapService.getRoadmapById(roadmapId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testRoadMapDto, result.get());
        verify(roadmapRepository).findById(roadmapId);
        verify(objectMapper).readValue(rawAiOutput, RoadMapDto.class);
    }

    @Test
    void getRoadmapById_WhenRoadmapNotFound_ReturnsEmpty() throws JsonProcessingException {
        // Given
        when(roadmapRepository.findById(roadmapId)).thenReturn(Optional.empty());

        // When
        Optional<RoadMapDto> result = roadmapService.getRoadmapById(roadmapId);

        // Then
        assertFalse(result.isPresent());
        verify(roadmapRepository).findById(roadmapId);
        verify(objectMapper, never()).readValue(anyString(), eq(RoadMapDto.class));
    }

    @Test
    void getRoadmapById_WhenDeserializationFails_ThrowsException() throws JsonProcessingException {
        // Given
        String rawAiOutput = "invalid json";
        testRoadmap.setRawAiOutput(rawAiOutput);

        when(roadmapRepository.findById(roadmapId)).thenReturn(Optional.of(testRoadmap));
        when(objectMapper.readValue(rawAiOutput, RoadMapDto.class))
            .thenThrow(new JsonProcessingException("Deserialization failed") {});

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> roadmapService.getRoadmapById(roadmapId));

        assertEquals("Failed to deserialize roadmap", exception.getMessage());
        verify(roadmapRepository).findById(roadmapId);
        verify(objectMapper).readValue(rawAiOutput, RoadMapDto.class);
    }

    @Test
    void generateRoadmap_WithValidUserId_GeneratesRoadmap() {
        // Given
        when(profileRepository.findById(userId)).thenReturn(Optional.of(testProfile));
        when(aiRoadMapProvider.generateRoadmap(testProfile)).thenReturn(testRoadMapDto);

        // When
        RoadMapDto result = roadmapService.generateRoadmap(userId);

        // Then
        assertNotNull(result);
        assertEquals(testRoadMapDto, result);
        verify(profileRepository).findById(userId);
        verify(aiRoadMapProvider).generateRoadmap(testProfile);
    }

    @Test
    void generateRoadmap_WhenProfileNotFound_ThrowsException() {
        // Given
        when(profileRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> roadmapService.generateRoadmap(userId));

        assertEquals("Profile not found for userId: " + userId, exception.getMessage());
        verify(profileRepository).findById(userId);
        verify(aiRoadMapProvider, never()).generateRoadmap(any());
    }

    @Test
    void generateAndSaveRoadmap_WithProfileHavingEmptyLists_GeneratesRoadmap() throws JsonProcessingException {
        // Given
        testProfile.setSkills(Collections.emptyList());
        testProfile.setInterests(Collections.emptyList());
        String serializedRoadmap = "{\"roadmapId\":\"test\"}";

        when(aiRoadMapProvider.generateRoadmap(testProfile)).thenReturn(testRoadMapDto);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(objectMapper.writeValueAsString(testRoadMapDto)).thenReturn(serializedRoadmap);
        when(roadmapRepository.save(any(Roadmap.class))).thenReturn(testRoadmap);

        // When
        RoadMapDto result = roadmapService.generateAndSaveRoadmap(testProfile);

        // Then
        assertNotNull(result);
        verify(aiRoadMapProvider).generateRoadmap(testProfile);
        verify(roadmapRepository).save(any(Roadmap.class));
    }

    @Test
    void generateAndSaveRoadmap_WithDifferentTargetRole_SavesCorrectTitle() throws JsonProcessingException {
        // Given
        testProfile.setGoalTitle("Data Scientist");
        testRoadMapDto.setTargetRole("Data Scientist");
        String serializedRoadmap = "{\"roadmapId\":\"test\"}";

        when(aiRoadMapProvider.generateRoadmap(testProfile)).thenReturn(testRoadMapDto);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(objectMapper.writeValueAsString(testRoadMapDto)).thenReturn(serializedRoadmap);
        when(roadmapRepository.save(any(Roadmap.class))).thenReturn(testRoadmap);

        // When
        roadmapService.generateAndSaveRoadmap(testProfile);

        // Then
        ArgumentCaptor<Roadmap> roadmapCaptor = ArgumentCaptor.forClass(Roadmap.class);
        verify(roadmapRepository).save(roadmapCaptor.capture());

        Roadmap savedRoadmap = roadmapCaptor.getValue();
        assertEquals("Data Scientist", savedRoadmap.getTitle());
    }

    // Helper methods for creating test data
    private RoadMapDto createTestRoadMapDto() {
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

    private Roadmap createTestRoadmap() {
        Roadmap roadmap = new Roadmap();
        roadmap.setId(roadmapId);
        roadmap.setUser(testUser);
        roadmap.setTitle("Software Developer");
        roadmap.setRawAiOutput("{\"roadmapId\":\"test\"}");
        roadmap.setCreatedAt(OffsetDateTime.now());
        return roadmap;
    }
}
