package com.scenariq.service.directions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.scenariq.model.DirectionsRoute;
import com.scenariq.model.LatLng;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class GoogleDirectionsClient implements DirectionsClient {

    private static final String DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public GoogleDirectionsClient(RestTemplateBuilder builder,
                                  @Value("${google.maps.api-key:}") String apiKey) {
        this.restTemplate = builder.build();
        this.apiKey = apiKey;
    }

    @Override
    public List<DirectionsRoute> fetchRoutes(String start, String end) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Google Maps API key is missing. Set GOOGLE_MAPS_API_KEY before calling the API.");
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(DIRECTIONS_URL)
                .queryParam("origin", start)
                .queryParam("destination", end)
                .queryParam("alternatives", "true")
                .queryParam("mode", "driving")
                .queryParam("key", apiKey)
                .build(true)
                .toUri();

        JsonNode response = restTemplate.getForObject(uri, JsonNode.class);
        if (response == null) {
            throw new IllegalStateException("Empty response returned from Google Directions API.");
        }

        String status = response.path("status").asText();
        if (!Objects.equals(status, "OK")) {
            String errorMessage = response.path("error_message").asText("No error details returned.");
            throw new IllegalStateException("Google Directions API error: " + status + " - " + errorMessage);
        }

        ArrayNode routesNode = (ArrayNode) response.path("routes");
        List<DirectionsRoute> routes = new ArrayList<>();

        for (JsonNode routeNode : routesNode) {
            JsonNode legNode = routeNode.path("legs").isArray() && routeNode.path("legs").size() > 0
                    ? routeNode.path("legs").get(0)
                    : null;
            if (legNode == null) {
                continue;
            }

            double distanceKm = roundToOneDecimal(legNode.path("distance").path("value").asDouble(0) / 1000d);
            int durationMinutes = (int) Math.round(legNode.path("duration").path("value").asDouble(0) / 60d);
            String summary = routeNode.path("summary").asText("Primary route");
            String startAddress = legNode.path("start_address").asText("");
            String endAddress = legNode.path("end_address").asText("");

            List<String> warnings = new ArrayList<>();
            routeNode.path("warnings").forEach(node -> warnings.add(node.asText()));

            List<LatLng> anchorPoints = extractAnchorPoints((ArrayNode) legNode.path("steps"));

            routes.add(new DirectionsRoute(
                    summary,
                    distanceKm,
                    durationMinutes,
                    startAddress,
                    endAddress,
                    warnings,
                    anchorPoints
            ));
        }

        return routes;
    }

    private List<LatLng> extractAnchorPoints(ArrayNode stepsNode) {
        if (stepsNode == null || stepsNode.isEmpty()) {
            return List.of();
        }

        int size = stepsNode.size();
        List<LatLng> anchors = new ArrayList<>();
        int[] indices = candidateIndices(size);

        for (int idx : indices) {
            JsonNode step = stepsNode.get(idx);
            if (step == null) {
                continue;
            }
            JsonNode endLocation = step.path("end_location");
            if (!endLocation.isMissingNode()) {
                double lat = endLocation.path("lat").asDouble();
                double lng = endLocation.path("lng").asDouble();
                anchors.add(new LatLng(lat, lng));
            }
        }

        return anchors.isEmpty() ? List.of() : List.copyOf(anchors);
    }

    private int[] candidateIndices(int size) {
        if (size <= 1) {
            return new int[]{0};
        }
        if (size == 2) {
            return new int[]{0, 1};
        }
        return new int[]{size / 3, (2 * size) / 3};
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10d) / 10d;
    }
}
