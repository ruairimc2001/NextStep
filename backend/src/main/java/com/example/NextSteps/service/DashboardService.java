package com.example.NextSteps.service;

import com.example.NextSteps.dto.DashboardResponse;
import com.example.NextSteps.dto.ProfileResponse;
import com.example.NextSteps.dto.RoadmapSummaryDto;
import com.example.NextSteps.dto.roadmap.RoadMapDto;
import com.example.NextSteps.entities.Roadmap;
import com.example.NextSteps.repository.RoadmapRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProfileService profileService;
    private final RoadmapRepository roadmapRepository;
    private final ObjectMapper objectMapper;

    public DashboardResponse getDashboard(UUID userId) {
        // Get profile
        ProfileResponse profile = profileService.getProfileByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        // Get roadmaps
        List<Roadmap> roadmaps = roadmapRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // Convert to summaries
        List<RoadmapSummaryDto> roadmapSummaries = roadmaps.stream()
                .map(this::convertToSummary)
                .collect(Collectors.toList());

        // Calculate stats
        int totalStages = roadmapSummaries.stream()
                .mapToInt(RoadmapSummaryDto::getTotalStages)
                .sum();
        int completedStages = roadmapSummaries.stream()
                .mapToInt(RoadmapSummaryDto::getCompletedStages)
                .sum();

        DashboardResponse.DashboardStats stats = new DashboardResponse.DashboardStats(
                roadmaps.size(),
                completedStages,
                totalStages
        );

        return new DashboardResponse(profile, roadmapSummaries, stats);
    }

    private RoadmapSummaryDto convertToSummary(Roadmap roadmap) {
        RoadmapSummaryDto summary = new RoadmapSummaryDto();
        summary.setId(roadmap.getId());
        summary.setTitle(roadmap.getTitle());
        summary.setCreatedAt(roadmap.getCreatedAt());

        // Parse the raw JSON to extract detailed information
        try {
            RoadMapDto roadMapDto = objectMapper.readValue(roadmap.getRawAiOutput(), RoadMapDto.class);
            summary.setSummary(roadMapDto.getSummary());
            summary.setTotalStages(roadMapDto.getStages() != null ? roadMapDto.getStages().size() : 0);

            // Build stage summaries
            List<RoadmapSummaryDto.StageSummary> stageSummaries = roadMapDto.getStages().stream()
                    .map(stage -> {
                        RoadmapSummaryDto.StageSummary stageSummary = new RoadmapSummaryDto.StageSummary();
                        stageSummary.setStageId(stage.getStageId());
                        stageSummary.setOrder(stage.getOrder());
                        stageSummary.setTitle(stage.getTitle());
                        stageSummary.setDescription(stage.getDescription());

                        int totalCourses = stage.getItems() != null ? stage.getItems().size() : 0;
                        int completedCourses = stage.getItems() != null ?
                            (int) stage.getItems().stream()
                                .filter(item -> "COMPLETED".equals(item.getStatus().toString()))
                                .count() : 0;

                        stageSummary.setTotalCourses(totalCourses);
                        stageSummary.setCompletedCourses(completedCourses);
                        stageSummary.setProgressPercent(totalCourses > 0 ? (completedCourses * 100.0) / totalCourses : 0);

                        return stageSummary;
                    })
                    .collect(Collectors.toList());

            summary.setStages(stageSummaries);

            // Calculate roadmap-level stats
            int totalCourses = roadMapDto.getStages().stream()
                    .mapToInt(stage -> stage.getItems() != null ? stage.getItems().size() : 0)
                    .sum();
            int completedCourses = roadMapDto.getStages().stream()
                    .flatMap(stage -> stage.getItems() != null ? stage.getItems().stream() : java.util.stream.Stream.empty())
                    .filter(item -> "COMPLETED".equals(item.getStatus().toString()))
                    .collect(Collectors.toList())
                    .size();
            double totalEstimatedHours = roadMapDto.getStages().stream()
                    .flatMap(stage -> stage.getItems() != null ? stage.getItems().stream() : java.util.stream.Stream.empty())
                    .mapToDouble(item -> item.getEstimatedHours() != null ? item.getEstimatedHours() : 0.0)
                    .sum();
            double completedHours = roadMapDto.getStages().stream()
                    .flatMap(stage -> stage.getItems() != null ? stage.getItems().stream() : java.util.stream.Stream.empty())
                    .filter(item -> "COMPLETED".equals(item.getStatus().toString()))
                    .mapToDouble(item -> item.getEstimatedHours() != null ? item.getEstimatedHours() : 0.0)
                    .sum();

            RoadmapSummaryDto.RoadmapStats roadmapStats = new RoadmapSummaryDto.RoadmapStats(
                    totalCourses,
                    completedCourses,
                    totalEstimatedHours,
                    completedHours
            );
            summary.setStats(roadmapStats);

            // Calculate completed stages
            int completedStages = (int) stageSummaries.stream()
                    .filter(s -> s.getProgressPercent() == 100)
                    .count();
            summary.setCompletedStages(completedStages);

        } catch (Exception e) {
            summary.setSummary("No summary available");
            summary.setTotalStages(0);
            summary.setCompletedStages(0);
        }

        return summary;
    }
}
