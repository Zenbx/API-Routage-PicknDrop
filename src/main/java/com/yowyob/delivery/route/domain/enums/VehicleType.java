package com.yowyob.delivery.route.domain.enums;

/**
 * Type of vehicle used by the driver.
 * Determines capacity and suitable routing paths (e.g., bike paths vs
 * highways).
 */
public enum VehicleType {
    /** Low capacity, suitable for short-distance urban delivery. */
    BICYCLE,
    /** Medium speed, suitable for small parcels in dense traffic. */
    MOTORCYCLE,
    /** Standard delivery vehicle for small to medium parcels. */
    CAR,
    /** High capacity for local multi-stop deliveries. */
    VAN,
    /** Maximum capacity for hub-to-hub heavy transport. */
    TRUCK
}
