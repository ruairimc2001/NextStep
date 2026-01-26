package com.example.NextSteps.dto.roadmap.generation;

public record OllamaRequest(
        String model,
        String prompt,
        boolean stream
) {}
