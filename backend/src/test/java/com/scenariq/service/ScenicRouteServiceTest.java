package com.scenariq.service;

import com.scenariq.model.DirectionsRoute;
import com.scenariq.model.LatLng;
import com.scenariq.model.RouteOption;
import com.scenariq.service.directions.DirectionsClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScenicRouteServiceTest {

    private final DirectionsClient stubClient = (start, end) -> List.of(
            new DirectionsRoute(
                    "NH 66",
                    585.0,
                    600,
                    start,
                    end,
                    List.of(),
                    List.of(new LatLng(18.5, 73.2), new LatLng(17.2, 74.1))
            ),
            new DirectionsRoute(
                    "NH 48",
                    610.0,
                    640,
                    start,
                    end,
                    List.of("Toll road"),
                    List.of(new LatLng(18.9, 73.1))
            ),
            new DirectionsRoute(
                    "NH 48/NH 66 hybrid",
                    630.0,
                    660,
                    start,
                    end,
                    List.of(),
                    List.of()
            )
    );

    private final ScenicRouteService service = new ScenicRouteService(stubClient);

    @Test
    void generatesMultipleOptionsFromGoogleRoutes() {
        List<RouteOption> routes = service.findRoutes("Mumbai", "Goa");

        assertThat(routes).hasSize(3);
        assertThat(routes.get(0).googleMapsUrl()).contains("origin=Mumbai");
        assertThat(routes.get(1).highlights()).anyMatch(h -> h.contains("Adds"));
    }

    @Test
    void fallsBackToCuratedRoutesWhenGoogleIsUnavailable() {
        ScenicRouteService fallbackService = new ScenicRouteService((start, end) -> {
            throw new IllegalStateException("Directions API unavailable");
        });

        List<RouteOption> routes = fallbackService.findRoutes("Pune", "Gokarna");

        assertThat(routes).hasSize(3);
        assertThat(routes.get(0).name()).contains("Pune");
        assertThat(routes.get(0).googleMapsUrl()).contains("origin=Pune");
    }
}
