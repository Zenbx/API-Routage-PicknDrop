package com.yowyob.delivery.route.domain.entity;

import com.yowyob.delivery.route.domain.enums.DriverState;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a Delivery Driver in the logistics system.
 * Manages driver contact info, current status, geographical location, and
 * performance metrics.
 */
@Table("drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDriver {

    /**
     * Unique identifier for the driver.
     */
    @Id
    private UUID id;

    /**
     * Driver's first name.
     */
    @Column("first_name")
    private String firstName;

    /**
     * Driver's last name or surname.
     */
    @Column("last_name")
    private String lastName;

    /**
     * Contact phone number of the driver.
     */
    @Column("phone_number")
    private String phoneNumber;

    /**
     * Professional email address of the driver.
     */
    @Column("email")
    private String email;

    /**
     * Professional driving license identification number.
     */
    @Column("license_number")
    private String licenseNumber;

    /**
     * Current availability or activity state (e.g., AVAILABLE, BUSY, OFFLINE).
     */
    @Column("current_state")
    private DriverState currentState;

    /**
     * Last known geographical location of the driver (WKT POINT).
     */
    @Column("current_location")
    private String currentLocation;

    /**
     * Average performance rating of the driver (0.0 to 5.0).
     */
    @Column("rating")
    private Double rating;

    /**
     * Total number of successfully completed deliveries.
     */
    @Column("total_deliveries")
    private Integer totalDeliveries;

    /**
     * ID of the vehicle currently assigned to or owned by the driver.
     */
    @Column("vehicle_id")
    private UUID vehicleId;

    /**
     * Timestamp of the most recent geographical location update.
     */
    @Column("last_location_update")
    private LocalDateTime lastLocationUpdate;

    /**
     * Indicates whether the driver's profile is active in the system.
     */
    @Column("is_active")
    private Boolean isActive;

    /**
     * Timestamp indicating when the driver record was created.
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Timestamp indicating the last modification to the driver's record.
     */
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
