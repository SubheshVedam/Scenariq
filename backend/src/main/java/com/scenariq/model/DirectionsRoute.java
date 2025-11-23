package com.scenariq.model;

import java.util.List;

public record DirectionsRoute(
        String summary,
        double distanceKm,
        int durationMinutes,
        String startAddress,
        String endAddress,
        List<String> warnings,
        List<LatLng> anchorPoints
) {
}
