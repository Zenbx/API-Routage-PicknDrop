package com.yowyob.delivery.route.domain.entity;

import com.yowyob.delivery.route.domain.enums.ParcelPriority;
import com.yowyob.delivery.route.domain.enums.ParcelState;

import lombok.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.geo.Point;

/**
 * Entity representing a Parcel (or Package) to be delivered.
 * This is the central entity in the logistics system, containing details about
 * sender, recipient, locations, weight, and tracking.
 */
@Table("parcels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parcel {

    /**
     * Unique identifier for the parcel.
     */
    @Id
    private UUID id;

    /**
     * Unique tracking code for public-facing tracking.
     */
    @Column("tracking_code")
    private String trackingCode;

    /**
     * ID of the driver currently assigned to this parcel.
     */
    @Column("driver_id")
    private UUID driverId;

    /**
     * ID of the vehicle used for transporting this parcel.
     */
    @Column("vehicle_id")
    private UUID vehicleId;

    /**
     * Current lifecycle state of the parcel (e.g., PLANNED, IN_TRANSIT, DELIVERED).
     */
    @Column("current_state")
    private ParcelState currentState;

    /**
     * The priority level of the parcel delivery.
     */
    @Column("priority")
    private ParcelPriority priority;

    /**
     * Full name of the person or entity sending the parcel.
     */
    @Column("sender_name")
    private String senderName;

    /**
     * Phone number of the sender.
     */
    @Column("sender_phone")
    private String senderPhone;

    /**
     * Full name of the person or entity receiving the parcel.
     */
    @Column("recipient_name")
    private String recipientName;

    /**
     * Phone number of the recipient.
     */
    @Column("recipient_phone")
    private String recipientPhone;

    /**
     * Geographical coordinates of the pickup point (WKT POINT).
     */
    @Column("pickup_location")
    private String pickupLocation;

    /**
     * Readable address of the pickup point.
     */
    @Column("pickup_address")
    private String pickupAddress;

    /**
     * Geographical coordinates of the delivery point (WKT POINT).
     */
    @Column("delivery_location")
    private String deliveryLocation;

    /**
     * Readable address of the delivery point.
     */
    @Column("delivery_address")
    private String deliveryAddress;

    /**
     * Weight of the parcel in kilograms.
     */
    @Column("weight_kg")
    private Double weightKg;

    /**
     * Declared content value in XAF.
     */
    @Column("declared_value_xaf")
    private Double declaredValueXaf;

    /**
     * Estimated or actual route distance in kilometers.
     */
    @Column("distance_km")
    private Double distanceKm;

    /**
     * Total delivery cost in XAF.
     */
    @Column("delivery_fee_xaf")
    private Double deliveryFeeXaf;

    /**
     * Expected timestamp of delivery.
     */
    @Column("estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    /**
     * Actual timestamp of delivery verification.
     */
    @Column("actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    /**
     * Optional additional notes.
     */
    @Column("notes")
    private String notes;

    /**
     * ID of the associated Petri Net instance.
     */
    @Column("petri_net_id")
    private String petriNetId;

    /**
     * Timestamp of parcel record creation.
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Timestamp of last modification.
     */
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
