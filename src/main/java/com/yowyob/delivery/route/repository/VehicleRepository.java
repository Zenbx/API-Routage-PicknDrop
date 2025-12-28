package com.yowyob.delivery.route.repository;

import com.yowyob.delivery.route.domain.entity.Vehicle;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Reactive repository for {@link Vehicle} entities.
 */
@Repository
public interface VehicleRepository extends R2dbcRepository<Vehicle, UUID> {
}
