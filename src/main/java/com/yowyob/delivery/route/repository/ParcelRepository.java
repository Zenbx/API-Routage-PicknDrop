package com.yowyob.delivery.route.repository;

import com.yowyob.delivery.route.domain.entity.Parcel;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Reactive repository for {@link Parcel} entities.
 * Utilizes R2DBC for asynchronous database access.
 * 
 * Standard CRUD operations are inherited from R2dbcRepository.
 * Custom PostGIS operations (like findAllWithLocations) are provided 
 * by CustomParcelRepository to handle geometry type conversions.
 */
@Repository
public interface ParcelRepository extends R2dbcRepository<Parcel, UUID>, CustomParcelRepository {
    // Standard CRUD methods: save(), findById(), findAll(), delete(), etc.
    // Custom methods with PostGIS support from CustomParcelRepository
}