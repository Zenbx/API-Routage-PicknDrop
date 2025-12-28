package com.yowyob.delivery.route.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for route information in API responses.
 * Provides the calculated path, distance, duration, and other logistical
 * details of a route.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing route details and the path geometry")
public class RouteResponseDTO {

    /**
     * Unique identifier of the route.
     */
    @Schema(description = "Unique identifier of the route", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    /**
     * Unique identifier of the parcel associated with this route.
     */
    @Schema(description = "ID of the parcel associated with this route", example = "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6")
    private UUID parcelId;

    /**
     * Unique identifier of the driver assigned to this route.
     */
    @Schema(description = "ID of the driver assigned to this route", example = "z9y8x7w6-v5u4-t3s2-r1q0-p9o8n7m6l5k4")
    private UUID driverId;

    /**
     * Total distance of the route in kilometers.
     */
    @Schema(description = "Total distance of the route in kilometers", example = "45.8")
    private Double totalDistanceKm;

    /**
     * Estimated time to complete the route in minutes.
     */
    @Schema(description = "Estimated duration in minutes", example = "120")
    private Integer estimatedDurationMinutes;

    /**
     * The name or identifier of the routing service/algorithm used.
     */
    @Schema(description = "Routing service or algorithm used", example = "DIJKSTRA")
    private String routingService;

    /**
     * Factor representing traffic conditions (1.0 = no traffic).
     */
    @Schema(description = "Traffic condition factor", example = "1.2")
    private Double trafficFactor;

    /**
     * Indicates whether the route is currently active.
     */
    @Schema(description = "Whether the route is currently active", example = "true")
    private Boolean isActive;

    /**
     * List of geographical points that form the polyline of the route.
     */
    @Schema(description = "Ordered list of points forming the route path")
    private List<GeoPointResponseDTO> path;
}
