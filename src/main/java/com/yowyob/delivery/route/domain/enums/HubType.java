package com.yowyob.delivery.route.domain.enums;

/**
 * Enum representing the different types of logistics hubs.
 * These types define the functional role of each hub in the delivery network.
 */
public enum HubType {
    /**
     * Main warehouse - primary storage facility for long-term inventory.
     * Used for: Bulk storage, inventory management, large shipments.
     */
    WAREHOUSE,

    /**
     * Sorting center - facility for sorting and organizing packages.
     * Used for: Package consolidation, routing decisions, load balancing.
     */
    SORTING_CENTER,

    /**
     * Transit point - temporary waypoint for packages in transit.
     * Used for: Quick stops, transfer points, route optimization.
     */
    TRANSIT_POINT,

    /**
     * Distribution center - last-mile delivery hub closer to customers.
     * Used for: Final sorting, local deliveries, customer pickups.
     */
    DISTRIBUTION_CENTER,

    /**
     * Pickup point - customer-facing location for package collection.
     * Used for: Customer self-service pickups, returns processing.
     */
    PICKUP_POINT,

    /**
     * Drop-off point - location where customers can drop packages.
     * Used for: Reverse logistics, returns, customer drop-offs.
     */
    DROP_OFF_POINT
}