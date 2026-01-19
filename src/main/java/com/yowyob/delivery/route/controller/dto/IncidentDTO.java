package com.yowyob.delivery.route.controller.dto;

import lombok.Data;

/**
 * DTO for incident data with linear segment representation.
 * Represents road blockages as line segments with buffer zones.
 */
@Data
public class IncidentDTO {
    private String type;
    private GeoLocation lineStart;
    private GeoLocation lineEnd;
    private Double bufferDistance; // Buffer width in meters
    private String description;

    @Data
    public static class GeoLocation {
        private Double latitude;
        private Double longitude;
    }
}
