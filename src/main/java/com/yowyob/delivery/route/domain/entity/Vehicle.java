package com.yowyob.delivery.route.domain.entity;

import com.yowyob.delivery.route.domain.enums.VehicleType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a Vehicle used for parcel deliveries.
 * Tracks physical attributes, capacity, and maintenance schedules.
 */
@Table("vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    /**
     * Unique identifier for the vehicle.
     */
    @Id
    private UUID id;

    /**
     * Official license plate number of the vehicle.
     */
    @Column("license_plate")
    private String licensePlate;

    /**
     * The category of the vehicle (e.g., MOTORCYCLE, VAN, TRUCK).
     */
    @Column("vehicle_type")
    private VehicleType vehicleType;

    /**
     * Manufacturer brand of the vehicle.
     */
    @Column("brand")
    private String brand;

    /**
     * Specific model name of the vehicle.
     */
    @Column("model")
    private String model;

    /**
     * The year the vehicle was manufactured.
     */
    @Column("manufacture_year")
    private Integer manufactureYear;

    /**
     * Maximum weight capacity in kilograms.
     */
    @Column("max_capacity_kg")
    private Double maxCapacityKg;

    /**
     * Available cargo volume in cubic meters.
     */
    @Column("cargo_volume_m3")
    private Double cargoVolumeM3;

    /**
     * Total distance recorded on the vehicle (odometer).
     */
    @Column("total_distance_km")
    private Double totalDistanceKm;

    /**
     * The date of the last recorded maintenance service.
     */
    @Column("last_maintenance_date")
    private LocalDateTime lastMaintenanceDate;

    /**
     * The projected date for the next maintenance service.
     */
    @Column("next_maintenance_date")
    private LocalDateTime nextMaintenanceDate;

    /**
     * Indicates whether the vehicle is currently in service.
     */
    @Column("is_active")
    private Boolean isActive;

    /**
     * Timestamp indicating when the vehicle record was created.
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Timestamp indicating the last modification to the vehicle's record.
     */
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
