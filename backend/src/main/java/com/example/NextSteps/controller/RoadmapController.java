package com.example.NextSteps.controller;

import com.example.NextSteps.dto.roadmap.RoadMapDto;
import com.example.NextSteps.entities.Profile;
import com.example.NextSteps.entities.Roadmap;
import com.example.NextSteps.service.RoadmapService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roadmaps")
@CrossOrigin(origins = "http://localhost:3000")
@AllArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    @PostMapping("/generate")
    public RoadMapDto generate(@Valid @RequestBody Profile profile) {
        return roadmapService.generateAndSaveRoadmap(profile);
    }

    @GetMapping("/user/{userId}")
    public List<Roadmap> getUserRoadmaps(@PathVariable UUID userId) {
        return roadmapService.getRoadmapsByUserId(userId);
    }

    @GetMapping("/{roadmapId}")
    public ResponseEntity<RoadMapDto> getRoadmap(@PathVariable UUID roadmapId) {
        return roadmapService.getRoadmapById(roadmapId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
