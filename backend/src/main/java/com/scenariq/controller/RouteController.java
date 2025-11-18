package com.scenariq.controller;

import com.scenariq.model.RouteOption;
import com.scenariq.service.ScenicRouteService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteController {

    private final ScenicRouteService scenicRouteService;

    public RouteController(ScenicRouteService scenicRouteService) {
        this.scenicRouteService = scenicRouteService;
    }

    @GetMapping
    public List<RouteOption> findRoutes(@RequestParam(required = false) String start,
                                        @RequestParam(required = false) String end) {
        return scenicRouteService.findRoutes(start, end);
    }
}
