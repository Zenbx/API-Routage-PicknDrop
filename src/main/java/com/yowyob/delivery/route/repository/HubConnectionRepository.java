package com.yowyob.delivery.route.repository;

import com.yowyob.delivery.route.domain.entity.HubConnection;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.util.UUID;

/**
 * Reactive repository for {@link HubConnection} entities.
 * Manages the graph edges for routing calculations.
 */
@Repository
public interface HubConnectionRepository extends R2dbcRepository<HubConnection, UUID> {
    Flux<HubConnection> findByFromHubId(UUID fromHubId);
}
