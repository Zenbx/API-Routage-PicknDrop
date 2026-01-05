package com.yowyob.delivery.route.repository;

import com.yowyob.delivery.route.domain.entity.Hub;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Reactive repository for {@link Hub} entities.
 * Utilizes R2DBC for asynchronous database access.
 * Extends CustomHubRepository for PostGIS-specific queries.
 */
@Repository
public interface HubRepository extends R2dbcRepository<Hub, UUID>, CustomHubRepository {
    @Query("SELECT id, address, type, ST_AsText(location) as location, created_at, updated_at FROM hubs")
    Flux<Hub> findAllWithLocation();
    // Standard CRUD operations are inherited from R2dbcRepository
    // Custom PostGIS operations are provided by CustomHubRepository
}