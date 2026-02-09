package com.example.NextSteps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private ProfileResponse profile;
    private List<RoadmapSummaryDto> roadmaps;
    private DashboardStats stats;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DashboardStats {
        private int totalRoadmaps;
        private int totalStagesCompleted;
        private int totalStages;
    }
}
