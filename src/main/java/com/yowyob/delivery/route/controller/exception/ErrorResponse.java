package com.yowyob.delivery.route.controller.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response structure returned by the API when an exception
 * occurs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Details about an error or exception")
public class ErrorResponse {
    @Schema(description = "Timestamp when the error occurred", example = "2023-10-27T10:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "500")
    private int status;

    @Schema(description = "Human-readable error category", example = "Internal Server Error")
    private String error;

    @Schema(description = "Detailed error message providing more context", example = "Database connection failed")
    private String message;

    @Schema(description = "The URI path where the error occurred", example = "/api/v1/hubs")
    private String path;
}
