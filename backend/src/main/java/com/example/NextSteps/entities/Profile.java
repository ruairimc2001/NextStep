package com.example.NextSteps.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "surname", length = 100)
    private String surname;

    @Column(name = "goal_title", length = 255)
    private String goalTitle;

    @Column(name = "skills", columnDefinition = "text[]")
    private List<String> skills;

    @Column(name = "interests", columnDefinition = "text[]")
    private List<String> interests;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

}
