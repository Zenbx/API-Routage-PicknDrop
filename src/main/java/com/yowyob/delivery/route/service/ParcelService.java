package com.yowyob.delivery.route.service;

import com.yowyob.delivery.route.controller.dto.ParcelRequestDTO;
import com.yowyob.delivery.route.controller.dto.ParcelResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service interface for managing parcels and packages.
 * Orchestrates parcel registration, tracking, and full lifecycle management.
 */
public interface ParcelService {
    /**
     * Registers a new parcel, generates a unique tracking code, and saves it.
     *
     * @param request the parcel registration details
     * @return a Mono emitting the created parcel details
     */
    Mono<ParcelResponseDTO> createParcel(ParcelRequestDTO request);

    /**
     * Finds a parcel by its internal unique identifier.
     *
     * @param id the UUID of the parcel
     * @return a Mono emitting the parcel details
     */
    Mono<ParcelResponseDTO> getParcel(UUID id);

    /**
     * Lists all registered parcels in the system.
     *
     * @return a Flux emitting all parcel details
     */
    Flux<ParcelResponseDTO> getAllParcels();
}
