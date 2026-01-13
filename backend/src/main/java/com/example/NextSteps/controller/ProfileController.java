package com.example.NextSteps.controller;

import com.example.NextSteps.dto.ProfileResponse;
import com.example.NextSteps.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable UUID userId) {
        Optional<ProfileResponse> profileResponse = profileService.getProfileByUserId(userId);

        if (profileResponse.isPresent()) {
            return ResponseEntity.ok(profileResponse.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

