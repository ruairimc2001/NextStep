package com.example.NextSteps.service;

import com.example.NextSteps.dto.roadmap.RoadMapDto;
import com.example.NextSteps.dto.roadmap.generation.AiRoadMapProvider;
import com.example.NextSteps.entities.Profile;
import com.example.NextSteps.entities.Roadmap;
import com.example.NextSteps.entities.User;
import com.example.NextSteps.repository.ProfileRepository;
import com.example.NextSteps.repository.RoadmapRepository;
import com.example.NextSteps.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final AiRoadMapProvider aiRoadMapProvider;
    private final ProfileRepository profileRepository;
    private final RoadmapRepository roadmapRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public RoadMapDto generateAndSaveRoadmap(Profile profile) {
        // Generate the roadmap using AI
        RoadMapDto roadmapDto = aiRoadMapProvider.generateRoadmap(profile);

        // Save the roadmap to the database
        Roadmap roadmap = new Roadmap();
        roadmap.setId(UUID.randomUUID());

        User user = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        roadmap.setUser(user);
        roadmap.setTitle(roadmapDto.getTargetRole());

        try {
            roadmap.setRawAiOutput(objectMapper.writeValueAsString(roadmapDto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize roadmap", e);
        }

        roadmap.setCreatedAt(OffsetDateTime.now());
        roadmapRepository.save(roadmap);

        return roadmapDto;
    }

    public List<Roadmap> getRoadmapsByUserId(UUID userId) {
        return roadmapRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<RoadMapDto> getRoadmapById(UUID roadmapId) {
        return roadmapRepository.findById(roadmapId)
                .map(roadmap -> {
                    try {
                        return objectMapper.readValue(roadmap.getRawAiOutput(), RoadMapDto.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to deserialize roadmap", e);
                    }
                });
    }

    public RoadMapDto generateRoadmap(UUID userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));

        return aiRoadMapProvider.generateRoadmap(profile);
    }

    public void deleteRoadmap(UUID roadmapId, UUID userId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new RuntimeException("Roadmap not found with id: " + roadmapId));

        // Verify the roadmap belongs to the authenticated user
        if (roadmap.getUser() == null || !roadmap.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this roadmap");
        }

        try {
            roadmapRepository.deleteById(roadmapId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete roadmap: " + e.getMessage(), e);
        }
    }
}
