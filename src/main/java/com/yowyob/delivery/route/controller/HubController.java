package com.yowyob.delivery.route.controller;

import com.yowyob.delivery.route.controller.dto.GeoPointRequestDTO;
import com.yowyob.delivery.route.controller.dto.GeoPointResponseDTO;
import com.yowyob.delivery.route.service.HubService;
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
 * Controller for managing logistics hubs and geographical points.
 * Provides endpoints for creating, retrieving, and listing hubs.
 */
@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
@Tag(name = "Hubs", description = "Endpoints for managing logistics hubs and points")
public class HubController {

    private final HubService hubService;

    /**
     * Creation of a new hub in the system.
     *
     * @param request the hub details (address, coordinates, type)
     * @return the created hub information
     */
    @PostMapping
    @Operation(summary = "Create a new hub", description = "Registers a new hub or logistics point in the system.")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GeoPointResponseDTO> createHub(@Valid @RequestBody GeoPointRequestDTO request) {
        return hubService.createHub(request);
    }

    /**
     * Retrieval of a specific hub by its unique identifier.
     *
     * @param id the unique UUID of the hub
     * @return the hub details if found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get hub by ID", description = "Retrieves details of a specific hub using its UUID.")
    public Mono<GeoPointResponseDTO> getHub(@PathVariable UUID id) {
        return hubService.getHub(id);
    }

    /**
     * Listing of all hubs registered in the system.
     *
     * @return a stream of all hubs
     */
    @GetMapping
    @Operation(summary = "List all hubs", description = "Returns a list of all hubs available in the network.")
    public Flux<GeoPointResponseDTO> getAllHubs() {
        return hubService.getAllHubs();
    }
}
