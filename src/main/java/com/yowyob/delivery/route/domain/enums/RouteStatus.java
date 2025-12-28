package com.yowyob.delivery.route.domain.enums;

/**
 * Status of a delivery route.
 * Tracks the execution progress of a calculated path.
 */
public enum RouteStatus {
    /** Route is calculated but execution hasn't started. */
    PLANNED,
    /** Delivery is currently in progress along this route. */
    ACTIVE,
    /** Destination reached and delivery finalized. */
    COMPLETED,
    /** Route is being updated due to a traffic or weather incident. */
    RECALCULATING,
    /** Delivery along this route was aborted. */
    CANCELLED
}
