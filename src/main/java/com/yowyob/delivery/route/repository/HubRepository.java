package com.yowyob.delivery.route.repository;

import com.yowyob.delivery.route.domain.entity.Hub;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Reactive repository for {@link Hub} entities.
 * Utilizes R2DBC for asynchronous database access.
 * Extends CustomHubRepository for PostGIS-specific queries.
 */
@Repository
public interface HubRepository extends R2dbcRepository<Hub, UUID>, CustomHubRepository {
    // Standard CRUD operations are inherited from R2dbcRepository
    // Custom PostGIS operations are provided by CustomHubRepository
}