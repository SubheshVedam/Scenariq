package com.scenariq.service;

import com.scenariq.model.DirectionsRoute;
import com.scenariq.model.LatLng;
import com.scenariq.model.RouteOption;
import com.scenariq.service.directions.DirectionsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ScenicRouteService {

    private static final List<String> THUMBNAIL_POOL = List.of(
            "https://images.unsplash.com/photo-1505739775417-85f7f80593dd",
            "https://images.unsplash.com/photo-1500534623283-312aade485b7",
            "https://images.unsplash.com/photo-1477587458883-47145ed94245",
            "https://images.unsplash.com/photo-1433086966358-54859d0ed716"
    );

    private static final Logger log = LoggerFactory.getLogger(ScenicRouteService.class);

    private final DirectionsClient directionsClient;

    public ScenicRouteService(DirectionsClient directionsClient) {
        this.directionsClient = directionsClient;
    }

    public List<RouteOption> findRoutes(String start, String end) {
        String safeStart = start == null || start.isBlank() ? "Point A" : start.trim();
        String safeEnd = end == null || end.isBlank() ? "Point B" : end.trim();

        List<DirectionsRoute> googleRoutes = fetchRoutesWithFallback(safeStart, safeEnd);

        if (googleRoutes.isEmpty()) {
            throw new IllegalStateException("No routes returned for the requested origin/destination.");
        }

        int bestDuration = googleRoutes.stream().mapToInt(DirectionsRoute::durationMinutes).min().orElse(0);
        double bestDistance = googleRoutes.stream().mapToDouble(DirectionsRoute::distanceKm).min().orElse(0);

        AtomicInteger position = new AtomicInteger();
        return googleRoutes.stream()
                .map(route -> buildOption(route, safeStart, safeEnd, bestDuration, bestDistance, position.getAndIncrement()))
                .collect(Collectors.toList());
    }

    private RouteOption buildOption(DirectionsRoute route,
                                    String start,
                                    String end,
                                    int fastestMinutes,
                                    double shortestDistance,
                                    int index) {
        int minutesImpact = Math.max(0, route.durationMinutes() - fastestMinutes);
        double distanceImpact = route.distanceKm() - shortestDistance;
        double fuelImpactPercent = shortestDistance > 0 ? Math.max(0, distanceImpact / shortestDistance * 100d) : 0;

        double scenicScore = clamp(9.2 - (index * 0.7) - (minutesImpact * 0.02), 7.0, 9.5);
        int comfortScore = Math.max(6, 9 - route.warnings().size());

        List<String> highlights = buildHighlights(route, minutesImpact);
        List<String> badges = badgesFromSummary(route.summary());

        String headline = String.format(Locale.US, "%s → %s (%s)", start, end, route.summary());
        String description = String.format(Locale.US, "%s · %d min · %.1f km", route.summary(), route.durationMinutes(), route.distanceKm());

        return new RouteOption(
                slugify(route.summary(), index),
                headline,
                route.durationMinutes(),
                route.distanceKm(),
                roundToOneDecimal(scenicScore),
                minutesImpact,
                roundToOneDecimal(fuelImpactPercent),
                comfortScore,
                description,
                highlights,
                badges.isEmpty() ? List.of("Google recommended") : badges,
                thumbnailFor(index),
                googleMapsUrl(start, end, route.anchorPoints())
        );
    }

    private List<DirectionsRoute> fetchRoutesWithFallback(String start, String end) {
        List<DirectionsRoute> googleRoutes = List.of();
        try {
            googleRoutes = directionsClient.fetchRoutes(start, end)
                    .stream()
                    .limit(3)
                    .collect(Collectors.toList());
        } catch (RuntimeException ex) {
            log.warn("Google Directions lookup failed for {} -> {}. Falling back to catalog. Cause: {}", start, end, ex.getMessage());
            log.debug("Stack trace for Google Directions failure", ex);
        }

        if (!googleRoutes.isEmpty()) {
            return googleRoutes;
        }

        log.info("Serving curated scenic catalog for {} -> {}", start, end);
        return curatedFallbackRoutes(start, end);
    }

    private List<String> buildHighlights(DirectionsRoute route, int minutesImpact) {
        List<String> highlights = new ArrayList<>();
        highlights.add(String.format(Locale.US, "Estimated time %d min", route.durationMinutes()));
        highlights.add(String.format(Locale.US, "Distance %.1f km", route.distanceKm()));
        if (minutesImpact > 0) {
            highlights.add("Adds " + minutesImpact + " min vs fastest");
        } else {
            highlights.add("Fastest option right now");
        }
        if (!route.warnings().isEmpty()) {
            highlights.add(route.warnings().get(0));
        }
        return highlights;
    }

    private List<String> badgesFromSummary(String summary) {
        if (summary == null || summary.isBlank()) {
            return List.of();
        }
        return Arrays.stream(summary.split("/"))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .limit(3)
                .collect(Collectors.toList());
    }

    private String thumbnailFor(int index) {
        return THUMBNAIL_POOL.get(index % THUMBNAIL_POOL.size());
    }

    private String googleMapsUrl(String start, String end, List<LatLng> anchors) {
        String baseUrl = "https://www.google.com/maps/dir/?api=1&travelmode=driving";
        StringBuilder builder = new StringBuilder(baseUrl)
                .append("&origin=").append(encode(start))
                .append("&destination=").append(encode(end));

        if (anchors != null && !anchors.isEmpty()) {
            String waypoints = anchors.stream()
                    .map(point -> String.format(Locale.US, "via:%f,%f", point.lat(), point.lng()))
                    .collect(Collectors.joining("|"));
            builder.append("&waypoints=").append(encode(waypoints));
        }

        return builder.toString();
    }

    private String slugify(String value, int indexFallback) {
        if (value == null || value.isBlank()) {
            return "route-" + indexFallback;
        }
        String slug = value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("(^-|-$)", "");
        return slug.isEmpty() ? "route-" + indexFallback : slug;
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10d) / 10d;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private List<DirectionsRoute> curatedFallbackRoutes(String start, String end) {
        return List.of(
                new DirectionsRoute(
                        "Konkan Coastal via NH 66",
                        585.0,
                        610,
                        start,
                        end,
                        List.of("Toll road near Panvel"),
                        List.of(
                                new LatLng(18.7563, 73.1926),
                                new LatLng(17.2473, 73.7125),
                                new LatLng(16.2045, 73.7454)
                        )
                ),
                new DirectionsRoute(
                        "Chorla Ghat Spice Trail",
                        610.0,
                        640,
                        start,
                        end,
                        List.of("Fog prone at Chorla"),
                        List.of(
                                new LatLng(15.6248, 74.0438),
                                new LatLng(15.3956, 74.0409)
                        )
                ),
                new DirectionsRoute(
                        "Amboli Reserve Mist Run",
                        630.0,
                        660,
                        start,
                        end,
                        List.of("Hairpins around Amboli"),
                        List.of(
                                new LatLng(15.9622, 74.0060),
                                new LatLng(15.8625, 74.2815)
                        )
                )
        );
    }
}
