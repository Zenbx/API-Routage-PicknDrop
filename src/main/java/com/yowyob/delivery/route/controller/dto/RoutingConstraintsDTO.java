package com.yowyob.delivery.route.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing constraints and preferences for the routing
 * algorithm.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Optional constraints and preferences for route calculation")
public class RoutingConstraintsDTO {

    /**
     * Whether the algorithm should prioritize non-highway paths.
     */
    @Schema(description = "Set to true to avoid highways during routing", defaultValue = "false")
    private boolean avoidHighways;

    /**
     * Whether the algorithm should avoid toll roads.
     */
    @Schema(description = "Set to true to avoid toll roads", defaultValue = "false")
    private boolean avoidTolls;

    /**
     * Constraint on the type of vehicle (e.g., MOTORCYCLE can navigate smaller
     * paths).
     */
    @Schema(description = "The type of vehicle to optimize the route for", example = "TRUCK")
    private String vehicleType;

    /**
     * The specific routing algorithm to use.
     * BASIC: Direct path.
     * DIJKSTRA: Shortest path based on weight.
     * ASTAR: Shortest path based on weight and heuristics.
     */
    @Schema(description = "Routing algorithm to use", allowableValues = { "BASIC", "DIJKSTRA",
            "ASTAR" }, defaultValue = "BASIC")
    private String algorithm;
}
