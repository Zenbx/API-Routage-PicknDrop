package com.yowyob.delivery.route.service.strategy;

import com.yowyob.delivery.route.controller.dto.IncidentDTO;
import com.yowyob.delivery.route.controller.dto.RoutingConstraintsDTO;
import com.yowyob.delivery.route.domain.entity.Hub;
import com.yowyob.delivery.route.domain.entity.Route;
import reactor.core.publisher.Mono;

/**
 * Interface for delivery routing algorithms.
 * Defines the contract for calculating optimal paths and handling route
 * updates.
 */
public interface RoutingStrategy {
    /**
     * Calculates the most optimal route between two hubs based on specific
     * constraints.
     *
     * @param start       the origin hub
     * @param end         the destination hub
     * @param constraints routing parameters (algorithm, preferences, etc.)
     * @return a Mono emitting the calculated route
     */
    Mono<Route> calculateOptimalRoute(Hub start, Hub end, RoutingConstraintsDTO constraints);

    /**
     * Updates an existing route in response to a real-time incident.
     *
     * @param currentRoute the route to be recalculated
     * @param incident     details about the disruption (linear or circular)
     * @return a Mono emitting the adjusted route
     */
    Mono<Route> recalculateRoute(Route currentRoute, IncidentDTO incident);
}
