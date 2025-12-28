package com.yowyob.delivery.route.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for a driver.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for a Delivery Driver")
public class DriverResponseDTO {

    @Schema(description = "Driver ID")
    private UUID id;

    @Schema(description = "Driver name")
    private String name;

    @Schema(description = "Vehicle type")
    private String vehicleType;

    @Schema(description = "Current status")
    private String status;

    @Schema(description = "Current GPS location")
    private GeoPointResponseDTO currentLocation;
}
