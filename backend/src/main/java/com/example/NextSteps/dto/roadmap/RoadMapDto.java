package com.example.NextSteps.dto.roadmap;

import com.example.NextSteps.dto.roadmap.generation.GenerationDetails;
import com.example.NextSteps.dto.roadmap.stage.Stage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class RoadMapDto {

    @NotBlank
    private String roadmapId;

    @NotBlank
    private String userId;

    @NotBlank
    private String targetRole;

    @NotBlank
    private String summary;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private GenerationDetails generationDetails;

    @NotEmpty
    private List<Stage> stages;

}

