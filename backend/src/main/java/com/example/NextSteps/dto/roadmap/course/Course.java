package com.example.NextSteps.dto.roadmap.course;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class Course {

    @NotBlank
    private String itemId;

    @Min(1)
    private int order;

    @NotBlank
    private String title;

    private String description;

    private String details;

    @NotBlank
    //TODO: Add URL validation
    private String url;

    @DecimalMin("0.0")
    private Double estimatedHours;

    @NotEmpty
    private CourseStatus status;

}

