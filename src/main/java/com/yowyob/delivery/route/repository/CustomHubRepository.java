package com.yowyob.delivery.route.repository;

import com.yowyob.delivery.route.domain.entity.Hub;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Custom repository interface for Hub-specific queries
 * that require manual mapping (e.g., PostGIS functions).
 */
public interface CustomHubRepository {
    
    /**
     * Find all hubs with location converted to WKT format using ST_AsText.
     * Uses manual mapping to handle PostGIS geometry types.
     */
    Flux<Hub> findAllWithLocation();
    
    /**
     * Find a hub by ID with location converted to WKT format.
     */
    Mono<Hub> findByIdWithLocation(UUID id);
    
    /**
     * Save a hub with PostGIS geometry support.
     * Converts WKT string to PostGIS geometry using ST_GeomFromText.
     */
    Mono<Hub> saveWithGeometry(Hub hub);
}