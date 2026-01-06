package com.yowyob.delivery.route.domain.enums;

/**
 * Represents the current operational state of a delivery driver.
 */
public enum DriverState {
    /** Driver is available for new assignments. */
    AVAILABLE,
    /** Driver has been assigned a task but hasn't started yet. */
    ASSIGNED,
    /** Driver is currently performing a delivery or task. */
    BUSY,
    /** Driver is in an inconsistent or lost communication state. */
    ZOMBIE,
    /** Driver is on a planned break. */
    ON_BREAK,
    /** Driver is traveling to pick up a parcel. */
    EN_ROUTE_PICKUP,
    /** Driver is traveling to deliver a parcel. */
    EN_ROUTE_DELIVERY,
    /** Driver is currently unable to work (e.g., vehicle issues). */
    UNAVAILABLE,
    /** Driver is logged out or inactive. */
    OFFLINE
}
