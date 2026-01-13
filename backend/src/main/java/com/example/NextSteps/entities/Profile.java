package com.example.NextSteps.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.example.NextSteps.entities.User;

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

    @Column(name = "goal_title", length = 255)
    private String goalTitle;

    @Column(name = "skills_text", columnDefinition = "text")
    private String skillsText;

    @Column(name = "interests_text", columnDefinition = "text")
    private String interestsText;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

}
