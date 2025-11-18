package com.scenariq.service;

import com.scenariq.model.RouteOption;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScenicRouteServiceTest {

    private final ScenicRouteService service = new ScenicRouteService();

    @Test
    void generatesMultipleOptions() {
        List<RouteOption> routes = service.findRoutes("San Francisco", "Monterey");

        assertThat(routes).hasSize(3);
        assertThat(routes.getFirst().googleMapsUrl()).contains("origin=San+Francisco");
    }
}
