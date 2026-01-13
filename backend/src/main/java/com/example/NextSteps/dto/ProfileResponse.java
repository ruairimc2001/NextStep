package com.example.NextSteps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String surname;
    private String goalTitle;
    private List<String> skills;
    private List<String> interests;
    private OffsetDateTime updatedAt;
}

