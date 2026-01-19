package com.example.NextSteps.repository;

import com.example.NextSteps.entities.Profile;
import com.example.NextSteps.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

//Using JpaRepository in order to skip boilerplate SQL code
@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByUser(User user);

    Optional<Profile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
