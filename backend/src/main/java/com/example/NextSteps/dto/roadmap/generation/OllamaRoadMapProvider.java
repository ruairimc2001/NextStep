package com.example.NextSteps.dto.roadmap.generation;

import com.example.NextSteps.dto.roadmap.RoadMapDto;
import com.example.NextSteps.entities.Profile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Data
@RequiredArgsConstructor
public class OllamaRoadMapProvider implements AiRoadMapProvider {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    private final String aiModel;
    private final int timeOut;

    @Override
    public RoadMapDto generateRoadmap(Profile profile) {
        String prompt = buildUserPrompt(profile);
        OllamaRequest request = new OllamaRequest(aiModel, prompt, false);

        OllamaResponse response;
        try {
            response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OllamaResponse.class)
                    .block(Duration.ofSeconds(timeOut));
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to Ollama: " + e.getMessage(), e);
        }

        if ((response == null) || response.response() == null) {
            throw new RuntimeException("Ollama Response body was empty");
        }

        RoadMapDto roadmap = parseRoadmapFromResponse(response.response());

        roadmap.setCreatedAt(OffsetDateTime.now());

        if (roadmap.getGenerationDetails() == null) {
            roadmap.setGenerationDetails(new GenerationDetails());
        }
        roadmap.getGenerationDetails().setProvider(Provider.OLLAMA.name());
        roadmap.getGenerationDetails().setModel(aiModel);
        roadmap.getGenerationDetails().setGeneratedAt(OffsetDateTime.now());

        validateOrThrow(roadmap);
        return roadmap;
    }
    private RoadMapDto parseRoadmapFromResponse(String responseBody) {
        String json = extractFirstJsonObject(responseBody);

        try {
            return objectMapper.readValue(json, RoadMapDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse roadmap from Ollama response", e);
        }
    }

    private void validateOrThrow(RoadMapDto roadMap) {
        Set<ConstraintViolation<RoadMapDto>> violations = validator.validate(roadMap);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("Roadmap violation:" + errorMessage);
        }
    }

    private String buildUserPrompt(Profile profile) {
        String profileJson = json(profile);

        return """
            Based on this user profile: %s

            Generate a learning roadmap as valid JSON with this exact structure (no markdown, no commentary, just JSON):
            {
                "roadmapId": "unique-id-here",
                "userId": "user-id-here",
                "targetRole": "the career goal from the profile",
                "summary": "a brief summary of the roadmap",
                "stages": [
                    {
                        "stageId": "stage-1",
                        "order": 1,
                        "title": "Stage Title",
                        "description": "What this stage covers",
                        "progress": {
                            "totalSteps": 3,
                            "currentStep": 1,
                            "percent": 0
                        },
                        "items": [
                            {
                                "itemId": "course-1",
                                "order": 1,
                                "title": "Course Title",
                                "description": "Course description",
                                "details": "Additional details",
                                "url": "https://example.com",
                                "estimatedHours": 10.0,
                                "status": "NOT_STARTED"
                            }
                        ]
                    }
                ]
            }

            Create 3-5 stages with 2-3 courses each based on the user's goal and current skills.
            """.formatted(profileJson);
    }


    private String json(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    private String extractFirstJsonObject(String rawJson) {
        int start = rawJson.indexOf('{');
        if (start < 0) {
            return rawJson.trim();
        }

        int depth = 0;
        for (int i = start; i < rawJson.length(); i++) {
            char c = rawJson.charAt(i);
            if (c == '{') depth++;
            if (c == '}') depth--;
            if (depth == 0) {
                return rawJson.substring(start, i + 1).trim();
            }
        }
        return rawJson.substring(start).trim();
    }
}
