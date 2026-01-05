package com.yowyob.delivery.route.repository;

import com.yowyob.delivery.route.domain.entity.Route;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Reactive repository for {@link Route} entities.
 */
@Repository
public interface RouteRepository extends R2dbcRepository<Route, UUID>, CustomRouteRepository {
}
