package com.example.NextSteps.dto.roadmap.generation;

import com.example.NextSteps.dto.roadmap.RoadMapDto;
import com.example.NextSteps.entities.Profile;
import com.example.NextSteps.entities.Roadmap;

public interface AiRoadMapProvider {
    RoadMapDto generateRoadmap(Profile profile);
}
