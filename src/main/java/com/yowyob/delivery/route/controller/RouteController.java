package com.yowyob.delivery.route.controller;

import com.yowyob.delivery.route.controller.dto.IncidentDTO;
import com.yowyob.delivery.route.controller.dto.RouteCalculationRequestDTO;
import com.yowyob.delivery.route.controller.dto.RouteResponseDTO;
import com.yowyob.delivery.route.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Controller for specialized routing operations and path calculations.
 * Provides advanced endpoints for route calculation, retrieval, and dynamic
 * recalculation.
 */
@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
@Tag(name = "Routes", description = "Endpoints for advanced route calculation and management")
public class RouteController {

    private final RouteService routeService;

    /**
     * Calculation of an optimal route between two logistical hubs.
     *
     * @param request the routing parameters including hubs and constraints
     * @return the calculated route details and path geometry
     */
    @PostMapping("/calculate")
    @Operation(summary = "Calculate optimal route", description = "Performs a pathfinding algorithm to find the best route between two points based on distance, time, and constraints.")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RouteResponseDTO> calculateRoute(@Valid @RequestBody RouteCalculationRequestDTO request) {
        // Validate request parameters early (fail-fast)
        if (request.getParcelId() == null) {
            return Mono.error(new IllegalArgumentException("parcelId must not be null"));
        }
        if (request.getStartHubId() == null) {
            return Mono.error(new IllegalArgumentException("startHubId must not be null"));
        }
        if (request.getEndHubId() == null) {
            return Mono.error(new IllegalArgumentException("endHubId must not be null"));
        }
        if (request.getDriverId() == null) {
            return Mono.error(new IllegalArgumentException("driverId must not be null"));
        }
        
        // Call service and ensure proper error handling
        return routeService.calculateRoute(request)
                .doOnError(error -> {
                    // Log any errors that occur during calculation or persistence
                    if (!(error instanceof IllegalArgumentException)) {
                        error.printStackTrace();
                    }
                });
    }

    /**
     * Retrieval of specific route details by its unique identifier.
     *
     * @param id the UUID of the route
     * @return the route details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get route details", description = "Retrieves stored details of a specific calculated route.")
    public Mono<RouteResponseDTO> getRoute(@PathVariable UUID id) {
        return routeService.getRoute(id);
    }

    /**
     * Dynamic recalculation of an existing route, typically triggered by an
     * incident or detour.
     *
     * @param id       the UUID of the route to recalculate
     * @param incident details about the disruption (e.g., road closure)
     * @return the updated route details
     */
    @PostMapping("/{id}/recalculate")
    @Operation(summary = "Recalculate route", description = "Updates an existing route path in response to real-time events like traffic or road incidents.")
    public Mono<RouteResponseDTO> recalculateRoute(@PathVariable UUID id, @RequestBody IncidentDTO incident) {
        System.out.println("=== RECALCULATE ROUTE REQUEST ===");
        System.out.println("Route ID: " + id);
        System.out.println("Incident data: " + incident);
        System.out.println("Incident type: " + (incident != null ? incident.getType() : "null"));
        System.out.println("Linear incident: " + (incident != null && incident.getLineStart() != null && incident.getLineEnd() != null));
        return routeService.recalculateRoute(id, incident)
            .doOnSuccess(result -> {
                System.out.println("=== RECALCULATION SUCCESS ===");
                System.out.println("Result: " + result);
            })
            .doOnError(error -> {
                System.err.println("=== RECALCULATION ERROR ===");
                error.printStackTrace();
            });
    }
}
