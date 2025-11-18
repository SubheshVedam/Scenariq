package com.scenariq.service;

import com.scenariq.model.RouteOption;
import com.scenariq.model.ScenicSpot;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ScenicRouteService {

    private static final List<ScenicSpot> SCENIC_SPOTS = List.of(
            new ScenicSpot(
                    "konkan-coast",
                    "Konkan Coastal Cruise",
                    9.4,
                    32,
                    41.0,
                    8,
                    "Palm-fringed beaches, red soil cliffs and seafood shacks between Mumbai and Goa.",
                    List.of("Fishing jetty at Harnai", "Alphonso mango stalls", "Sunset at Ganpatipule"),
                    List.of("Sea", "Cliffs", "Temples"),
                    "https://images.unsplash.com/photo-1433086966358-54859d0ed716"
            ),
            new ScenicSpot(
                    "western-ghats",
                    "Western Ghats Mist Trail",
                    8.7,
                    24,
                    29.5,
                    9,
                    "Rolling Ghats, cloud forests and monsoon waterfalls near Lonavala and Mahabaleshwar.",
                    List.of("Tiger Point fog deck", "Vada pav pit stop", "Devil's Kitchen viewpoint"),
                    List.of("Ghats", "Waterfalls", "Forest"),
                    "https://images.unsplash.com/photo-1505739775417-85f7f80593dd"
            ),
            new ScenicSpot(
                    "himalayan-serpentine",
                    "Himalayan Serpentine",
                    9.2,
                    38,
                    52.8,
                    7,
                    "High-altitude switchbacks with glimpses of snow peaks and prayer flag-lined villages.",
                    List.of("Tea stop at Kurseong", "Tiger Hill sunrise", "Prayer wheels at Ghoom"),
                    List.of("Mountains", "Tea estates", "Villages"),
                    "https://images.unsplash.com/photo-1500534623283-312aade485b7"
            ),
            new ScenicSpot(
                    "desert-dune",
                    "Thar Desert Heritage Loop",
                    7.6,
                    20,
                    33.3,
                    6,
                    "Camel caravans, dunes and sandstone forts between Jodhpur and Jaisalmer.",
                    List.of("Kuldhara ruins detour", "Sam dune sunset", "Lassi stand at Pokhran"),
                    List.of("Desert", "Forts", "Culture"),
                    "https://images.unsplash.com/photo-1477587458883-47145ed94245"
            ),
            new ScenicSpot(
                    "tea-garden-circuit",
                    "Nilgiri Tea Garden Circuit",
                    8.1,
                    22,
                    27.0,
                    9,
                    "Hairpin bends through emerald tea estates, toy-train whistles and eucalyptus mist.",
                    List.of("Coonoor view tower", "Homestay filter coffee", "Heritage toy train yard"),
                    List.of("Tea gardens", "Toy train", "Hills"),
                    "https://images.unsplash.com/photo-1506744038136-46273834b3fb"
            )
    );

    public List<RouteOption> findRoutes(String start, String end) {
        String safeStart = start == null || start.isBlank() ? "Point A" : start.trim();
        String safeEnd = end == null || end.isBlank() ? "Point B" : end.trim();

        int baseMinutes = 28 + Math.abs((safeStart + safeEnd).hashCode() % 35);
        double baseDistance = 18 + (Math.abs((safeStart + safeEnd).hashCode() % 80) * 0.7);

        return SCENIC_SPOTS.stream()
                .map(spot -> buildOption(spot, safeStart, safeEnd, baseMinutes, baseDistance))
                .sorted(Comparator
                        .comparingDouble(RouteOption::scenicScore)
                        .thenComparingInt(RouteOption::totalMinutes)
                        .reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    private RouteOption buildOption(ScenicSpot spot,
                                    String start,
                                    String end,
                                    int baseMinutes,
                                    double baseDistance) {
        double scenicBonus = spot.scenicScore();
        int addedMinutes = spot.detourMinutes();
        int totalMinutes = baseMinutes + addedMinutes;
        double distance = Math.round((baseDistance + spot.detourDistanceKm()) * 10d) / 10d;
        double fuelImpact = Math.round(((spot.detourDistanceKm() / baseDistance) * 100d) * 10d) / 10d;

        String headline = String.format(Locale.US,
                "%s via %s",
                start + " → " + end,
                spot.name());

        String description = "Adds " + addedMinutes + " min for " + spot.description();

        return new RouteOption(
                spot.id(),
                headline,
                totalMinutes,
                distance,
                scenicBonus,
                addedMinutes,
                fuelImpact,
                spot.comfortScore(),
                description,
                spot.highlights(),
                spot.views(),
                spot.thumbnail(),
                googleMapsUrl(start, end, spot)
        );
    }

    private String googleMapsUrl(String start, String end, ScenicSpot spot) {
        String baseUrl = "https://www.google.com/maps/dir/?api=1&travelmode=driving";
        return baseUrl +
                "&origin=" + encode(start) +
                "&destination=" + encode(end) +
                "&waypoints=" + encode(spot.name());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
