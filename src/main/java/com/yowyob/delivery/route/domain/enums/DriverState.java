package com.yowyob.delivery.route.domain.enums;

/**
 * Represents the current operational state of a delivery driver.
 */
public enum DriverState {
    /** Driver is available for new assignments. */
    LIBRE,
    /** Driver has been assigned a task but hasn't started yet. */
    AFFECTE,
    /** Driver is currently performing a delivery or task. */
    OCCUPE,
    /** Driver is in an inconsistent or lost communication state. */
    ZOMBIE,
    /** Driver is on a planned break. */
    PAUSE,
    /** Driver is traveling to pick up a parcel. */
    EN_ROUTE_COLLECTE,
    /** Driver is traveling to deliver a parcel. */
    EN_ROUTE_LIVRAISON,
    /** Driver is currently unable to work (e.g., vehicle issues). */
    INDISPONIBLE,
    /** Driver is logged out or inactive. */
    OFFLINE
}
