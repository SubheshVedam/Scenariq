package com.scenariq.model;

import java.util.List;

public record RouteOption(
        String id,
        String name,
        int totalMinutes,
        double distanceKm,
        double scenicScore,
        int trafficImpactMinutes,
        double fuelImpactPercent,
        int comfortScore,
        String description,
        List<String> highlights,
        List<String> views,
        String thumbnail,
        String googleMapsUrl
) {
}
