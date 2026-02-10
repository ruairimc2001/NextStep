package com.example.NextSteps.service;

import com.example.NextSteps.dto.ProfileResponse;
import com.example.NextSteps.entities.Profile;
import com.example.NextSteps.entities.User;
import com.example.NextSteps.repository.ProfileRepository;
import com.example.NextSteps.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTests {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileService profileService;

    private User testUser;
    private Profile testProfile;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Setup test user
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("john.doe@example.com");

        // Setup test profile
        testProfile = new Profile();
        testProfile.setUserId(userId);
        testProfile.setUser(testUser);
        testProfile.setFirstName("John");
        testProfile.setSurname("Doe");
        testProfile.setGoalTitle("Software Developer");
        testProfile.setSkills(Arrays.asList("Java", "Spring Boot", "REST APIs"));
        testProfile.setInterests(Arrays.asList("Web Development", "Machine Learning"));
        testProfile.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void getProfileByUserId_WithExistingUserAndProfile_ReturnsCompleteProfileResponse() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // When
        Optional<ProfileResponse> result = profileService.getProfileByUserId(userId);

        // Then
        assertTrue(result.isPresent());
        ProfileResponse profileResponse = result.get();

        assertEquals(userId, profileResponse.getUserId());
        assertEquals("john.doe@example.com", profileResponse.getEmail());
        assertEquals("John", profileResponse.getFirstName());
        assertEquals("Doe", profileResponse.getSurname());
        assertEquals("Software Developer", profileResponse.getGoalTitle());
        assertEquals(Arrays.asList("Java", "Spring Boot", "REST APIs"), profileResponse.getSkills());
        assertEquals(Arrays.asList("Web Development", "Machine Learning"), profileResponse.getInterests());
        assertEquals(testProfile.getUpdatedAt(), profileResponse.getUpdatedAt());

        // Verify repository interactions
        verify(userRepository).findById(userId);
        verify(profileRepository).findByUserId(userId);
    }

    @Test
    void getProfileByUserId_WithExistingUserButNoProfile_ReturnsBasicProfileResponse() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When
        Optional<ProfileResponse> result = profileService.getProfileByUserId(userId);

        // Then
        assertTrue(result.isPresent());
        ProfileResponse profileResponse = result.get();

        assertEquals(userId, profileResponse.getUserId());
        assertEquals("john.doe@example.com", profileResponse.getEmail());
        assertNull(profileResponse.getFirstName());
        assertNull(profileResponse.getSurname());
        assertNull(profileResponse.getGoalTitle());
        assertNull(profileResponse.getSkills());
        assertNull(profileResponse.getInterests());
        assertNull(profileResponse.getUpdatedAt());

        // Verify repository interactions
        verify(userRepository).findById(userId);
        verify(profileRepository).findByUserId(userId);
    }

    @Test
    void getProfileByUserId_WithNonExistentUser_ReturnsEmpty() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<ProfileResponse> result = profileService.getProfileByUserId(userId);

        // Then
        assertFalse(result.isPresent());

        // Verify repository interactions
        verify(userRepository).findById(userId);
        verify(profileRepository, never()).findByUserId(any());
    }

    @Test
    void getProfileByUserId_WithProfileHavingEmptySkillsAndInterests_ReturnsProfileWithEmptyLists() {
        // Given
        testProfile.setSkills(Collections.emptyList());
        testProfile.setInterests(Collections.emptyList());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // When
        Optional<ProfileResponse> result = profileService.getProfileByUserId(userId);

        // Then
        assertTrue(result.isPresent());
        ProfileResponse profileResponse = result.get();

        assertEquals(userId, profileResponse.getUserId());
        assertEquals("john.doe@example.com", profileResponse.getEmail());
        assertEquals("John", profileResponse.getFirstName());
        assertEquals("Doe", profileResponse.getSurname());
        assertEquals("Software Developer", profileResponse.getGoalTitle());
        assertTrue(profileResponse.getSkills().isEmpty());
        assertTrue(profileResponse.getInterests().isEmpty());
        assertEquals(testProfile.getUpdatedAt(), profileResponse.getUpdatedAt());
    }

    @Test
    void getProfileByUserId_WithProfileHavingNullSkillsAndInterests_ReturnsProfileWithNullLists() {
        // Given
        testProfile.setSkills(null);
        testProfile.setInterests(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // When
        Optional<ProfileResponse> result = profileService.getProfileByUserId(userId);

        // Then
        assertTrue(result.isPresent());
        ProfileResponse profileResponse = result.get();

        assertEquals(userId, profileResponse.getUserId());
        assertEquals("john.doe@example.com", profileResponse.getEmail());
        assertEquals("John", profileResponse.getFirstName());
        assertEquals("Doe", profileResponse.getSurname());
        assertEquals("Software Developer", profileResponse.getGoalTitle());
        assertNull(profileResponse.getSkills());
        assertNull(profileResponse.getInterests());
        assertEquals(testProfile.getUpdatedAt(), profileResponse.getUpdatedAt());
    }

    @Test
    void getProfileByUserId_WithProfileHavingNullFields_ReturnsProfileWithNullFields() {
        // Given
        testProfile.setFirstName(null);
        testProfile.setSurname(null);
        testProfile.setGoalTitle(null);
        testProfile.setUpdatedAt(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // When
        Optional<ProfileResponse> result = profileService.getProfileByUserId(userId);

        // Then
        assertTrue(result.isPresent());
        ProfileResponse profileResponse = result.get();

        assertEquals(userId, profileResponse.getUserId());
        assertEquals("john.doe@example.com", profileResponse.getEmail());
        assertNull(profileResponse.getFirstName());
        assertNull(profileResponse.getSurname());
        assertNull(profileResponse.getGoalTitle());
        assertNull(profileResponse.getUpdatedAt());
        assertEquals(Arrays.asList("Java", "Spring Boot", "REST APIs"), profileResponse.getSkills());
        assertEquals(Arrays.asList("Web Development", "Machine Learning"), profileResponse.getInterests());
    }

    @Test
    void getProfileByUserId_WithLargeSkillsAndInterestsLists_ReturnsCompleteProfile() {
        // Given
        List<String> largeSkillsList = Arrays.asList(
            "Java", "Spring Boot", "REST APIs", "Microservices", "Docker",
            "Kubernetes", "AWS", "PostgreSQL", "Redis", "RabbitMQ"
        );
        List<String> largeInterestsList = Arrays.asList(
            "Web Development", "Machine Learning", "DevOps", "Cloud Computing",
            "Data Science", "Mobile Development", "Blockchain", "AI"
        );

        testProfile.setSkills(largeSkillsList);
        testProfile.setInterests(largeInterestsList);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // When
        Optional<ProfileResponse> result = profileService.getProfileByUserId(userId);

        // Then
        assertTrue(result.isPresent());
        ProfileResponse profileResponse = result.get();

        assertEquals(largeSkillsList, profileResponse.getSkills());
        assertEquals(largeInterestsList, profileResponse.getInterests());
        assertEquals(10, profileResponse.getSkills().size());
        assertEquals(8, profileResponse.getInterests().size());
    }

    @Test
    void getProfileByUserId_WithSpecialCharactersInUserData_ReturnsCorrectProfile() {
        // Given
        testUser.setEmail("test+user@example-domain.co.uk");
        testProfile.setFirstName("José");
        testProfile.setSurname("O'Connor-Smith");
        testProfile.setGoalTitle("Full-Stack Developer & Designer");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // When
        Optional<ProfileResponse> result = profileService.getProfileByUserId(userId);

        // Then
        assertTrue(result.isPresent());
        ProfileResponse profileResponse = result.get();

        assertEquals("test+user@example-domain.co.uk", profileResponse.getEmail());
        assertEquals("José", profileResponse.getFirstName());
        assertEquals("O'Connor-Smith", profileResponse.getSurname());
        assertEquals("Full-Stack Developer & Designer", profileResponse.getGoalTitle());
    }

    @Test
    void getProfileByUserId_WithDifferentTimeZones_ReturnsCorrectUpdatedAt() {
        // Given
        OffsetDateTime specificTime = OffsetDateTime.parse("2023-10-15T14:30:00+05:30");
        testProfile.setUpdatedAt(specificTime);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // When
        Optional<ProfileResponse> result = profileService.getProfileByUserId(userId);

        // Then
        assertTrue(result.isPresent());
        ProfileResponse profileResponse = result.get();

        assertEquals(specificTime, profileResponse.getUpdatedAt());
    }

    @Test
    void getProfileByUserId_WithSingleSkillAndInterest_ReturnsCorrectLists() {
        // Given
        testProfile.setSkills(Collections.singletonList("Python"));
        testProfile.setInterests(Collections.singletonList("Data Analysis"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // When
        Optional<ProfileResponse> result = profileService.getProfileByUserId(userId);

        // Then
        assertTrue(result.isPresent());
        ProfileResponse profileResponse = result.get();

        assertEquals(1, profileResponse.getSkills().size());
        assertEquals("Python", profileResponse.getSkills().get(0));
        assertEquals(1, profileResponse.getInterests().size());
        assertEquals("Data Analysis", profileResponse.getInterests().get(0));
    }

    @Test
    void getProfileByUserId_MultipleCallsWithSameUserId_CallsRepositoryEachTime() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // When
        profileService.getProfileByUserId(userId);
        profileService.getProfileByUserId(userId);
        profileService.getProfileByUserId(userId);

        // Then
        verify(userRepository, times(3)).findById(userId);
        verify(profileRepository, times(3)).findByUserId(userId);
    }

    @Test
    void getProfileByUserId_WithDifferentUserIds_CallsRepositoryWithCorrectIds() {
        // Given
        UUID userId2 = UUID.randomUUID();
        UUID userId3 = UUID.randomUUID();

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        // When
        profileService.getProfileByUserId(userId);
        profileService.getProfileByUserId(userId2);
        profileService.getProfileByUserId(userId3);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).findById(userId2);
        verify(userRepository).findById(userId3);
    }
}
