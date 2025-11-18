package com.scenariq.model;

import java.util.List;

public record ScenicSpot(
        String id,
        String name,
        double scenicScore,
        int detourMinutes,
        double detourDistanceKm,
        int comfortScore,
        String description,
        List<String> highlights,
        List<String> views,
        String thumbnail
) {
}
