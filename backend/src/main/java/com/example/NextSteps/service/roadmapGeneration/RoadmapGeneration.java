package com.example.NextSteps.service.roadmapGeneration;

import com.example.NextSteps.dto.ProfileResponse;
import com.example.NextSteps.dto.roadmap.RoadMapDto;

public interface RoadmapGeneration {

    RoadMapDto generateRoadmap(ProfileResponse userId);
}
