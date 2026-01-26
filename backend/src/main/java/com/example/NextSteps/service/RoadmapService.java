package com.example.NextSteps.service;

import com.example.NextSteps.dto.roadmap.RoadMapDto;
import com.example.NextSteps.dto.roadmap.generation.AiRoadMapProvider;
import com.example.NextSteps.entities.Profile;
import com.example.NextSteps.entities.Roadmap;
import com.example.NextSteps.entities.User;
import com.example.NextSteps.repository.RoadmapRepository;
import com.example.NextSteps.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RoadmapService {

    private final AiRoadMapProvider roadMapProvider;
    private final RoadmapRepository roadmapRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public RoadMapDto generateAndSaveRoadmap(Profile profile) {
        // Generate roadmap from AI
        RoadMapDto roadmapDto = roadMapProvider.generateRoadmap(profile);

        // Save to database
        saveRoadmap(profile.getUserId(), roadmapDto);

        return roadmapDto;
    }

    private void saveRoadmap(UUID userId, RoadMapDto roadmapDto) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found: " + userId);
        }

        Roadmap roadmap = new Roadmap();
        roadmap.setId(UUID.randomUUID());
        roadmap.setUser(userOptional.get());
        roadmap.setTitle(roadmapDto.getTargetRole());
        roadmap.setRawAiOutput(toJson(roadmapDto));
        roadmap.setCreatedAt(OffsetDateTime.now());

        roadmapRepository.save(roadmap);
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize roadmap to JSON", e);
        }
    }

    public List<Roadmap> getRoadmapsByUserId(UUID userId) {
        return roadmapRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<RoadMapDto> getRoadmapById(UUID roadmapId) {
        Optional<Roadmap> roadmapOptional = roadmapRepository.findById(roadmapId);

        if (roadmapOptional.isEmpty()) {
            return Optional.empty();
        }

        try {
            RoadMapDto dto = objectMapper.readValue(
                roadmapOptional.get().getRawAiOutput(),
                RoadMapDto.class
            );
            return Optional.of(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse roadmap from database", e);
        }
    }
}

