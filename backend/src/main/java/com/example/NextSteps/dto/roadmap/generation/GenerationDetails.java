package com.example.NextSteps.dto.roadmap.generation;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class GenerationDetails {

    private String model;

    private String provider;

    private OffsetDateTime generatedAt;

}
