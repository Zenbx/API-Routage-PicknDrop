package com.yowyob.delivery.route.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object representing a geographical point or a hub in the
 * system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Object representing a geographical point or a logistical hub")
public class GeoPointResponseDTO {

    /**
     * Unique identifier of the hub or point (null if it's transient).
     */
    @Schema(description = "Unique identifier", example = "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6")
    private UUID id;

    /**
     * Readable address associated with the point.
     */
    @Schema(description = "Human-readable address", example = "Avenue Kennedy, Yaoundé")
    private String address;

    /**
     * Latitude coordinate of the point.
     */
    @Schema(description = "Latitude coordinate", example = "3.8")
    private Double latitude;

    /**
     * Longitude coordinate of the point.
     */
    @Schema(description = "Longitude coordinate", example = "11.5")
    private Double longitude;

    /**
     * The type of hub (e.g., WAREHOUSE, DISTRIBUTION_CENTER).
     */
    @Schema(description = "The type of the hub", example = "WAREHOUSE")
    private String type;
}
