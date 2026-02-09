package com.example.NextSteps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoadmapSummaryDto {
    private UUID id;
    private String title;
    private String summary;
    private OffsetDateTime createdAt;
    private int totalStages;
    private int completedStages;
    private List<StageSummary> stages;
    private RoadmapStats stats;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StageSummary {
        private String stageId;
        private int order;
        private String title;
        private String description;
        private int totalCourses;
        private int completedCourses;
        private double progressPercent;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoadmapStats {
        private int totalCourses;
        private int completedCourses;
        private double totalEstimatedHours;
        private double completedHours;
    }
}


