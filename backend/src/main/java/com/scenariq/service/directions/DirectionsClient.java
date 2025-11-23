package com.scenariq.service.directions;

import com.scenariq.model.DirectionsRoute;

import java.util.List;

public interface DirectionsClient {
    List<DirectionsRoute> fetchRoutes(String start, String end);
}
