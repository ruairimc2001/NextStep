package com.example.NextSteps.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "roadmaps")
public class Roadmap {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "raw_ai_output", columnDefinition = "text")
    private String rawAiOutput;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

}

