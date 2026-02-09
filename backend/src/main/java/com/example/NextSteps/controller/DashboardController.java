package com.example.NextSteps.controller;

import com.example.NextSteps.dto.DashboardResponse;
import com.example.NextSteps.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<DashboardResponse> getDashboard(@PathVariable UUID userId) {
        DashboardResponse dashboard = dashboardService.getDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }
}
