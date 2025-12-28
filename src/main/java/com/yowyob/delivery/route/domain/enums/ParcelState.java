package com.yowyob.delivery.route.domain.enums;

/**
 * Enum representing the different states of a parcel in the delivery lifecycle.
 * These states must match exactly with the database values.
 */
public enum ParcelState {
    /**
     * Parcel has been planned but not yet assigned to a driver.
     * Initial state when a parcel is first registered.
     */
    PLANNED,

    /**
     * Parcel is waiting to be picked up by a driver.
     * The parcel is ready at the pickup location.
     */
    PENDING_PICKUP,

    /**
     * Parcel has been picked up by the driver.
     * The driver has collected the parcel from the sender.
     */
    PICKED_UP,

    /**
     * Parcel is currently in transit to the destination.
     * The parcel is being transported through the network.
     */
    IN_TRANSIT,

    /**
     * Parcel is out for delivery (final leg of the journey).
     * The driver is currently delivering to the recipient.
     */
    OUT_FOR_DELIVERY,

    /**
     * Parcel has been successfully delivered to the recipient.
     * Final successful state.
     */
    DELIVERED,

    /**
     * Delivery attempt failed.
     * Reasons: recipient unavailable, wrong address, refused delivery, etc.
     */
    FAILED,

    /**
     * Parcel delivery has been cancelled.
     * Can be cancelled by sender, recipient, or system.
     */
    CANCELLED,

    /**
     * Parcel is being returned to sender.
     * Return process initiated after failed delivery or cancellation.
     */
    RETURNING,

    /**
     * Parcel has been returned to sender.
     * Final state for returned parcels.
     */
    RETURNED
}