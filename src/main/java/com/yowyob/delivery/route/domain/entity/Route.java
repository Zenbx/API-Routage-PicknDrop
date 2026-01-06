package com.yowyob.delivery.route.domain.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a calculated delivery route.
 * Stores the path geometry, distance, duration, and associated metadata.
 */
@Table("routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    /**
     * Unique identifier for the Route.
     */
    @Id
    private UUID id;

    /**
     * ID of the parcel associated with this route.
     */
    @Column("parcel_id")
    private UUID parcelId;

    /**
     * ID of the driver assigned to follow this route.
     */
    @Column("driver_id")
    private UUID driverId;

    /**
     * ID of the starting hub.
     */
    @Column("start_hub_id")
    private UUID startHubId;

    /**
     * ID of the destination hub.
     */
    @Column("end_hub_id")
    private UUID endHubId;

    /**
     * Path geometry of the route, stored as a Well-Known Text (WKT) LineString.
     */
    @Column("route_geometry")
    private String routeGeometry;

    /**
     * Optional JSON representation of specific waypoints or stops.
     */
    @Column("waypoints")
    private String waypoints;

    /**
     * Total distance of the calculated route in kilometers.
     */
    @Column("total_distance_km")
    private Double totalDistanceKm;

    /**
     * Estimated time to complete the route in minutes.
     */
    @Column("estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    /**
     * The routing service or algorithm used (e.g., DIJKSTRA, GOOGLE_MAPS).
     */
    @Column("routing_service")
    private String routingService;

    /**
     * Traffic factor considered during calculation (default 1.0).
     */
    @Column("traffic_factor")
    private Double trafficFactor;

    /**
     * Indicates if the route is currently active for navigation.
     */
    @Column("is_active")
    private Boolean isActive;

    /**
     * Timestamp indicating when the route was calculated.
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
}
