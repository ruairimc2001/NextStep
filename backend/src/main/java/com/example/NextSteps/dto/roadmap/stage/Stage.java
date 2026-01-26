package com.example.NextSteps.dto.roadmap.stage;

import com.example.NextSteps.dto.roadmap.course.Course;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class Stage {

    @NotBlank
    private String stageId;

    @Min(1)
    private int order;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private StageProgressTracker progress;

    @NotEmpty
    private List<Course> items;
}
