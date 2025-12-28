package com.yowyob.delivery.route.controller;

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
 * Controller for managing deliveries and their execution lifecycle.
 * Provides endpoints for initiating delivery plans (route calculation) and
 * tracking.
 */
@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
@Tag(name = "Deliveries", description = "Endpoints for initiating and tracking deliveries")
public class DeliveryController {

    private final RouteService routeService;

    /**
     * Creation of a delivery plan for a specific parcel.
     * This involves calculating the optimal route based on origin, destination, and
     * constraints.
     *
     * @param request the route calculation parameters
     * @return the calculated route details
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Initiate a delivery", description = "Calculates an optimal route for a parcel and creates a delivery plan.")
    public Mono<RouteResponseDTO> createDelivery(@Valid @RequestBody RouteCalculationRequestDTO request) {
        // In this context, calculating a route IS creating a delivery plan
        return routeService.calculateRoute(request);
    }

    /**
     * Retrieval of specific delivery details by its unique identifier.
     *
     * @param id the UUID of the delivery (route)
     * @return the delivery details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get delivery details", description = "Retrieves the execution details and status of a specific delivery.")
    public Mono<RouteResponseDTO> getDelivery(@PathVariable UUID id) {
        return routeService.getRoute(id);
    }

    /**
     * Retrieval of real-time tracking information for a delivery.
     *
     * @param id the UUID of the delivery
     * @return tracking details, including the current path and estimated arrival
     */
    @GetMapping("/{id}/tracking")
    @Operation(summary = "Get delivery tracking", description = "Returns real-time or last-known tracking coordinates for a delivery.")
    public Mono<RouteResponseDTO> getTracking(@PathVariable UUID id) {
        // Tracking currently returns the route details with the path
        return routeService.getRoute(id);
    }
}
