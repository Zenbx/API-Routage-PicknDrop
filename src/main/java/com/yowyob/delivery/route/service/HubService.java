package com.yowyob.delivery.route.service;

import com.yowyob.delivery.route.controller.dto.GeoPointRequestDTO;
import com.yowyob.delivery.route.controller.dto.GeoPointResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Service interface for managing logistics hubs and points.
 * Handles the business logic for hub registration and retrieval.
 */
public interface HubService {
    /**
     * Registers a new hub in the logistics network.
     *
     * @param request the hub details (address, coordinates, type)
     * @return a Mono emitting the created hub information
     */
    Mono<GeoPointResponseDTO> createHub(GeoPointRequestDTO request);

    /**
     * Retrieves a hub by its unique identifier.
     *
     * @param id the unique UUID of the hub
     * @return a Mono emitting the hub details if found
     */
    Mono<GeoPointResponseDTO> getHub(UUID id);

    /**
     * Retrieves all registered hubs in the network.
     *
     * @return a Flux emitting all hub details
     */
    Flux<GeoPointResponseDTO> getAllHubs();
}
