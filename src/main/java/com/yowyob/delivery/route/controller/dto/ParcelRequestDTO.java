package com.yowyob.delivery.route.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new parcel.
 * Contains all necessary information about the sender, recipient, and the
 * shipment itself.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new Parcel")
public class ParcelRequestDTO {

    /**
     * The full name of the sender.
     */
    @NotBlank(message = "Sender name is required")
    @Schema(description = "Full name of the sender", example = "Jean Dupont")
    private String senderName;

    /**
     * The contact phone number of the sender.
     */
    @NotBlank(message = "Sender phone is required")
    @Schema(description = "Contact phone number of the sender", example = "+237 600 000 000")
    private String senderPhone;

    /**
     * The full name of the recipient.
     */
    @NotBlank(message = "Recipient name is required")
    @Schema(description = "Full name of the recipient", example = "Marie Claire")
    private String recipientName;

    /**
     * The contact phone number of the recipient.
     */
    @NotBlank(message = "Recipient phone is required")
    @Schema(description = "Contact phone number of the recipient", example = "+237 611 111 111")
    private String recipientPhone;

    /**
     * The geographical location for picking up the parcel, formatted as a
     * Well-Known Text (WKT) Point.
     */
    @NotBlank(message = "Pickup location is required")
    @Schema(description = "Pickup location in WKT Point format", example = "POINT(9.7 4.0)")
    private String pickupLocation;

    /**
     * The readable address for picking up the parcel.
     */
    @Schema(description = "Readable address for pickup", example = "Rue de la Joie, Douala")
    private String pickupAddress;

    /**
     * The geographical location for delivering the parcel, formatted as a
     * Well-Known Text (WKT) Point.
     */
    @NotBlank(message = "Delivery location is required")
    @Schema(description = "Delivery location in WKT Point format", example = "POINT(11.5 3.8)")
    private String deliveryLocation;

    /**
     * The readable address for delivering the parcel.
     */
    @Schema(description = "Readable address for delivery", example = "Bastos, Yaoundé")
    private String deliveryAddress;

    /**
     * The weight of the parcel in kilograms.
     */
    @NotNull(message = "Weight is required")
    @Schema(description = "Weight of the parcel in kilograms", example = "12.5")
    private Double weightKg;

    /**
     * The estimated value of the goods being shipped, in XAF.
     */
    @Schema(description = "Declared value of the parcel content in XAF", example = "50000.0")
    private Double declaredValueXaf;

    /**
     * Any additional instructions or notes for the delivery.
     */
    @Schema(description = "Optional notes or delivery instructions", example = "Fragile content, handle with care")
    private String notes;
}
