package com.yowyob.delivery.route.repository;

import com.yowyob.delivery.route.domain.entity.DeliveryDriver;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for DeliveryDriver.
 */
/**
 * Reactive repository for {@link DeliveryDriver} entities.
 */
@Repository
public interface DriverRepository extends R2dbcRepository<DeliveryDriver, UUID> {
}
