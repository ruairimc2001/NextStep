package com.example.NextSteps.dto.roadmap.stage;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class StageProgressTracker {

    @Min(1)
    private int totalSteps;

    @Min(1)
    private int currentStep;

    //Must be between 0 and 100
    @Min(0)
    @Max(100)
    private int percent;
}
