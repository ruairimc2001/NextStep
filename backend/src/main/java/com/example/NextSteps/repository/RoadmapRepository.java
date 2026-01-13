package com.example.NextSteps.repository;

import com.example.NextSteps.entities.Roadmap;
import com.example.NextSteps.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {

    List<Roadmap> findByUser(User user);

    List<Roadmap> findByUserId(UUID userId);

    List<Roadmap> findByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserId(UUID userId);
}
