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

        // If profile doesn't exist, return basic user info
        if (profileOptional.isEmpty()) {
            ProfileResponse response = new ProfileResponse(
                user.getId(),
                user.getEmail(),
                null,
                null,
                null,
                null,
                null,
                null
            );
            return Optional.of(response);
        }

        Profile profile = profileOptional.get();
        ProfileResponse response = new ProfileResponse(
            user.getId(),
            user.getEmail(),
            profile.getFirstName(),
            profile.getSurname(),
            profile.getGoalTitle(),
            profile.getSkills(),
            profile.getInterests(),
            profile.getUpdatedAt()
        );

        return Optional.of(response);
    }
}

