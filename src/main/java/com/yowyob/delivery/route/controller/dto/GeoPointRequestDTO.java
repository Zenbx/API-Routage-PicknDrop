package com.yowyob.delivery.route.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating or updating a geographical point or hub.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating or updating a GeoPoint/Hub")
public class GeoPointRequestDTO {

    /**
     * Readable address of the point.
     */
    @NotBlank(message = "Address is required")
    @Schema(description = "Human-readable address", example = "Carrefour Bastos, Yaoundé")
    private String address;

    /**
     * Latitude coordinate of the point.
     */
    @Schema(description = "Latitude coordinate", example = "3.8860")
    private Double latitude;

    /**
     * Longitude coordinate of the point.
     */
    @Schema(description = "Longitude coordinate", example = "11.5140")
    private Double longitude;

    /**
     * The type of the hub being created (e.g., WAREHOUSE, DISTRIBUTION_CENTER).
     */
    @NotBlank(message = "Hub type is required")
    @Schema(description = "Specific type of the logistical hub", example = "DISTRIBUTION_CENTER")
    private String type;
}
