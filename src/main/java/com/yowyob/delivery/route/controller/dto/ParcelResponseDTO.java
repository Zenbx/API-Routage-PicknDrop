package com.yowyob.delivery.route.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for parcel information in API responses.
 * Provides details about the parcel's status, tracking, and logistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing parcel details")
public class ParcelResponseDTO {

    /**
     * Unique identifier of the parcel.
     */
    @Schema(description = "Unique identifier of the parcel", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    /**
     * Unique tracking code assigned to the parcel for public tracking.
     */
    @Schema(description = "Unique tracking code for the parcel", example = "TRK-A1B2C3D4")
    private String trackingCode;

    /**
     * Current state of the parcel in the logistics lifecycle.
     */
    @Schema(description = "Current state of the parcel", example = "EN_TRANSIT")
    private String currentState;

    /**
     * Priority level of the parcel delivery.
     */
    @Schema(description = "Priority level of the parcel", example = "NORMAL")
    private String priority;

    /**
     * Geographical point and details of the pickup location.
     */
    @Schema(description = "Detailed information about the pickup location")
    private GeoPointResponseDTO pickupLocation;

    /**
     * Geographical point and details of the delivery location.
     */
    @Schema(description = "Detailed information about the delivery location")
    private GeoPointResponseDTO deliveryLocation;

    /**
     * Weight of the parcel in kilograms.
     */
    @Schema(description = "Weight of the parcel in kilograms", example = "10.5")
    private Double weightKg;

    /**
     * Calculated cost for delivering the parcel in XAF.
     */
    @Schema(description = "Calculated delivery fee in XAF", example = "2500.0")
    private Double deliveryFeeXaf;

    /**
     * Estimated timestamp when the parcel is expected to be delivered.
     */
    @Schema(description = "Estimated time of delivery")
    private LocalDateTime estimatedDeliveryTime;

    /**
     * ID of the associated Petri Net instance for lifecycle tracking.
     */
    @Schema(description = "ID of the associated Petri Net instance", example = "net-123456")
    private String petriNetId;

    /**
     * Timestamp when the parcel record was created.
     */
    @Schema(description = "Timestamp when the parcel was registered")
    private LocalDateTime createdAt;
}
