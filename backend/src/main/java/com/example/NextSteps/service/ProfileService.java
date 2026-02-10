package com.example.NextSteps.service;

import com.example.NextSteps.dto.ProfileResponse;
import com.example.NextSteps.entities.Profile;
import com.example.NextSteps.entities.User;
import com.example.NextSteps.repository.ProfileRepository;
import com.example.NextSteps.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    public Optional<ProfileResponse> getProfileByUserId(UUID userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();
        Optional<Profile> profileOptional = profileRepository.findByUserId(userId);

        if (profileOptional.isEmpty()) {
            ProfileResponse response = ProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .build();
            return Optional.of(response);
        }

        Profile profile = profileOptional.get();
        ProfileResponse response = ProfileResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .firstName(profile.getFirstName())
            .surname(profile.getSurname())
            .goalTitle(profile.getGoalTitle())
            .skills(profile.getSkills())
            .interests(profile.getInterests())
            .updatedAt(profile.getUpdatedAt())
            .build();

        return Optional.of(response);
    }
}

