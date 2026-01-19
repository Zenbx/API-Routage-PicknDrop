package com.yowyob.delivery.route.service;

import com.yowyob.delivery.route.controller.dto.IncidentDTO;
import com.yowyob.delivery.route.controller.dto.RouteCalculationRequestDTO;
import com.yowyob.delivery.route.controller.dto.RouteResponseDTO;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Service interface for advanced routing and delivery path calculations.
 * Orchestrates pathfinding algorithms and handles route lifecycle.
 */
public interface RouteService {
    /**
     * Calculates an optimal route based on origin, destination, and constraints.
     *
     * @param request the routing parameters including hubs and constraints
     * @return a Mono emitting the calculated route and path details
     */
    Mono<RouteResponseDTO> calculateRoute(RouteCalculationRequestDTO request);

    /**
     * Recalculates an existing route in response to a real-time incident (e.g.,
     * road closure).
     *
     * @param routeId  the UUID of the route to recalculate
     * @param incident information about the disruption (linear or circular)
     * @return a Mono emitting the updated route
     */
    Mono<RouteResponseDTO> recalculateRoute(UUID routeId, IncidentDTO incident);

    /**
     * Retrieves an existing route by its unique identifier.
     *
     * @param id the UUID of the route
     * @return a Mono emitting the route details
     */
    Mono<RouteResponseDTO> getRoute(UUID id);
}
