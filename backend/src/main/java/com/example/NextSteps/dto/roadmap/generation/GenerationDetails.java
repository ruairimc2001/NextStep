package com.example.NextSteps.dto.roadmap.generation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerationDetails {
    private String provider;
    private String model;
    private String prompt;
    private OffsetDateTime generatedAt;
}

