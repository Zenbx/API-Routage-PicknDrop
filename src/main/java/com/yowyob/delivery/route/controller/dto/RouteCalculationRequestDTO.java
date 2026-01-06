package com.yowyob.delivery.route.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object for initiating a route calculation request.
 * specifies the parcel, origin, destination, and any routing constraints or
 * algorithmic preferences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for calculating an optimal route")
public class RouteCalculationRequestDTO {

    /**
     * The unique identifier of the parcel for which the route is being calculated.
     */
    @NotNull(message = "Parcel ID is required")
    @Schema(description = "ID of the parcel to be delivered", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID parcelId;

    /**
     * The unique identifier of the starting hub or point of the route.
     */
    @NotNull(message = "Start point ID is required")
    @Schema(description = "ID of the starting hub/origin point", example = "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6")
    private UUID startHubId;

    /**
     * The unique identifier of the destination hub or point of the route.
     */
    @NotNull(message = "End point ID is required")
    @Schema(description = "ID of the ending hub/destination point", example = "z9y8x7w6-v5u4-t3s2-r1q0-p9o8n7m6l5k4")
    private UUID endHubId;

    /**
     * Optional constraints to influence the routing algorithm (e.g., algorithm
     * choice, vehicle type).
     */
    @Schema(description = "Optional preferences and constraints for the routing algorithm")
    private RoutingConstraintsDTO constraints;

    /**
     * The unique identifier of the driver assigned to this route.
     */
    @Schema(description = "ID of the driver assigned to this delivery", example = "d1e2f3g4-h5i6-j7k8-l9m0-n1o2p3q4r5s6")
    private UUID driverId;
}
