package com.yowyob.delivery.route.controller;

import com.yowyob.delivery.route.controller.dto.ParcelRequestDTO;
import com.yowyob.delivery.route.controller.dto.ParcelResponseDTO;
import com.yowyob.delivery.route.service.ParcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Controller for managing parcels and packages within the logistics system.
 * Handles parcel registration, detail retrieval, and listing.
 */
@RestController
@RequestMapping("/api/v1/parcels")
@RequiredArgsConstructor
@Tag(name = "Parcels", description = "Endpoints for parcel management and registration")
public class ParcelController {

    private final ParcelService parcelService;

    /**
     * Creation of a new parcel record.
     * Generates a tracking code and sets initial state to PLANNED.
     *
     * @param request the parcel registration details
     * @return the created parcel information including its tracking code
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new parcel", description = "Registers a new parcel in the system, generates a tracking code, and calculates initial fees.")
    public Mono<ParcelResponseDTO> createParcel(@Valid @RequestBody ParcelRequestDTO request) {
        return parcelService.createParcel(request);
    }

    /**
     * Retrieval of specific parcel information by its unique identifier.
     *
     * @param id the UUID of the parcel
     * @return the complete parcel details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get parcel details", description = "Retrieves the current status, locations, and details of a parcel by its internal UUID.")
    public Mono<ParcelResponseDTO> getParcel(@PathVariable UUID id) {
        return parcelService.getParcel(id);
    }

    /**
     * Listing of all parcels currently registered in the logistics network.
     *
     * @return a stream containing all parcel records
     */
    @GetMapping
    @Operation(summary = "List all parcels", description = "Returns a list of all parcels across all states (PLANNED, TRANSIT, DELIVERED).")
    public Flux<ParcelResponseDTO> getAllParcels() {
        return parcelService.getAllParcels();
    }
}
