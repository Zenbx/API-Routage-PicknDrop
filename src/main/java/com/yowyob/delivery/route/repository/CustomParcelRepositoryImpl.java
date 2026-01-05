package com.yowyob.delivery.route.repository;

import com.yowyob.delivery.route.domain.entity.Parcel;
import com.yowyob.delivery.route.domain.enums.ParcelPriority;
import com.yowyob.delivery.route.domain.enums.ParcelState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;


/**
 * Custom implementation for Parcel repository methods that require
 * manual row mapping, particularly for PostGIS geometry functions.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomParcelRepositoryImpl implements CustomParcelRepository {

    private final DatabaseClient databaseClient;

    @Override
    public Flux<Parcel> findAllWithLocations() {
        String sql = """
            SELECT 
                id, 
                tracking_code,
                driver_id,
                vehicle_id,
                current_state,
                priority,
                sender_name,
                sender_phone,
                recipient_name,
                recipient_phone,
                ST_AsText(pickup_location) as pickup_location,
                pickup_address,
                ST_AsText(delivery_location) as delivery_location,
                delivery_address,
                weight_kg,
                declared_value_xaf,
                distance_km,
                delivery_fee_xaf,
                estimated_delivery_time,
                notes,
                created_at,
                updated_at
            FROM parcels
            ORDER BY created_at DESC
            """;
        
        return databaseClient.sql(sql)
                .map(this::mapRowToParcel)
                .all()
                .doOnSubscribe(s -> log.debug("Fetching all parcels with locations"))
                .doOnError(e -> log.error("Error fetching parcels", e));
    }

    @Override
    public Mono<Parcel> findByIdWithLocations(UUID id) {
        String sql = """
            SELECT 
                id, 
                tracking_code,
                driver_id,
                vehicle_id,
                current_state,
                priority,
                sender_name,
                sender_phone,
                recipient_name,
                recipient_phone,
                ST_AsText(pickup_location) as pickup_location,
                pickup_address,
                ST_AsText(delivery_location) as delivery_location,
                delivery_address,
                weight_kg,
                declared_value_xaf,
                distance_km,
                delivery_fee_xaf,
                estimated_delivery_time,
                notes,
                created_at,
                updated_at
            FROM parcels 
            WHERE id = :id
            """;
        
        return databaseClient.sql(sql)
                .bind("id", id)
                .map(this::mapRowToParcel)
                .one()
                .doOnSubscribe(s -> log.debug("Fetching parcel with id: {}", id))
                .doOnError(e -> log.error("Error fetching parcel {}", id, e));
    }

    @Override
    public Mono<Parcel> saveWithGeometry(Parcel parcel) {
        if (parcel.getId() == null) {
            // INSERT
            String sql = """
                INSERT INTO parcels (
                    id, tracking_code, driver_id, vehicle_id, current_state, priority,
                    sender_name, sender_phone, recipient_name, recipient_phone,
                    pickup_location, pickup_address, delivery_location, delivery_address,
                    weight_kg, declared_value_xaf, distance_km, delivery_fee_xaf,
                    estimated_delivery_time, notes, created_at, updated_at
                )
                VALUES (
                    :id, :tracking_code, :driver_id, :vehicle_id, :current_state::parcel_state, :priority::parcel_priority,
                    :sender_name, :sender_phone, :recipient_name, :recipient_phone,
                    ST_GeomFromText(:pickup_location, 4326), :pickup_address,
                    ST_GeomFromText(:delivery_location, 4326), :delivery_address,
                    :weight_kg, :declared_value_xaf, :distance_km, :delivery_fee_xaf,
                    :estimated_delivery_time, :notes, NOW(), NOW()
                )
                RETURNING id, tracking_code, driver_id, vehicle_id, current_state, priority,
                    sender_name, sender_phone, recipient_name, recipient_phone,
                    ST_AsText(pickup_location) as pickup_location, pickup_address,
                    ST_AsText(delivery_location) as delivery_location, delivery_address,
                    weight_kg, declared_value_xaf, distance_km, delivery_fee_xaf,
                    estimated_delivery_time, notes, created_at, updated_at
                """;
            
            UUID newId = UUID.randomUUID();
            
            DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql)
                    .bind("id", newId)
                    .bind("tracking_code", parcel.getTrackingCode());
            
            spec = bindNullable(spec, "driver_id", parcel.getDriverId(), UUID.class);
            spec = bindNullable(spec, "vehicle_id", parcel.getVehicleId(), UUID.class);
            
            spec = spec.bind("current_state", parcel.getCurrentState().name())
                    .bind("priority", parcel.getPriority().name())
                    .bind("sender_name", parcel.getSenderName())
                    .bind("sender_phone", parcel.getSenderPhone())
                    .bind("recipient_name", parcel.getRecipientName())
                    .bind("recipient_phone", parcel.getRecipientPhone())
                    .bind("pickup_location", parcel.getPickupLocation())
                    .bind("pickup_address", parcel.getPickupAddress())
                    .bind("delivery_location", parcel.getDeliveryLocation())
                    .bind("delivery_address", parcel.getDeliveryAddress())
                    .bind("weight_kg", parcel.getWeightKg());

            // Bind other nullable fields
            spec = bindNullable(spec, "declared_value_xaf", parcel.getDeclaredValueXaf(), Double.class);
            spec = bindNullable(spec, "distance_km", parcel.getDistanceKm(), Double.class);
            spec = bindNullable(spec, "delivery_fee_xaf", parcel.getDeliveryFeeXaf(), Double.class);
            spec = bindNullable(spec, "estimated_delivery_time", parcel.getEstimatedDeliveryTime(), LocalDateTime.class);
            spec = bindNullable(spec, "notes", parcel.getNotes(), String.class);

            return spec.map(this::mapRowToParcel)
                    .one()
                    .doOnSuccess(saved -> log.info("Parcel created with ID: {}", saved.getId()))
                    .doOnError(e -> log.error("Error saving parcel", e));
        } else {
            // UPDATE
            String sql = """
                UPDATE parcels 
                SET tracking_code = :tracking_code,
                    driver_id = :driver_id,
                    vehicle_id = :vehicle_id,
                    current_state = :current_state::parcel_state,
                    priority = :priority::parcel_priority,
                    sender_name = :sender_name,
                    sender_phone = :sender_phone,
                    recipient_name = :recipient_name,
                    recipient_phone = :recipient_phone,
                    pickup_location = ST_GeomFromText(:pickup_location, 4326),
                    pickup_address = :pickup_address,
                    delivery_location = ST_GeomFromText(:delivery_location, 4326),
                    delivery_address = :delivery_address,
                    weight_kg = :weight_kg,
                    declared_value_xaf = :declared_value_xaf,
                    distance_km = :distance_km,
                    delivery_fee_xaf = :delivery_fee_xaf,
                    estimated_delivery_time = :estimated_delivery_time,
                    notes = :notes,
                    updated_at = NOW()
                WHERE id = :id
                RETURNING id, tracking_code, driver_id, vehicle_id, current_state, priority,
                    sender_name, sender_phone, recipient_name, recipient_phone,
                    ST_AsText(pickup_location) as pickup_location, pickup_address,
                    ST_AsText(delivery_location) as delivery_location, delivery_address,
                    weight_kg, declared_value_xaf, distance_km, delivery_fee_xaf,
                    estimated_delivery_time, notes, created_at, updated_at
                """;
            
            DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql)
                    .bind("id", parcel.getId())
                    .bind("tracking_code", parcel.getTrackingCode());

            spec = bindNullable(spec, "driver_id", parcel.getDriverId(), UUID.class);
            spec = bindNullable(spec, "vehicle_id", parcel.getVehicleId(), UUID.class);

            spec = spec.bind("current_state", parcel.getCurrentState().name())
                    .bind("priority", parcel.getPriority().name())
                    .bind("sender_name", parcel.getSenderName())
                    .bind("sender_phone", parcel.getSenderPhone())
                    .bind("recipient_name", parcel.getRecipientName())
                    .bind("recipient_phone", parcel.getRecipientPhone())
                    .bind("pickup_location", parcel.getPickupLocation())
                    .bind("pickup_address", parcel.getPickupAddress())
                    .bind("delivery_location", parcel.getDeliveryLocation())
                    .bind("delivery_address", parcel.getDeliveryAddress());

            spec = bindNullable(spec, "weight_kg", parcel.getWeightKg(), Double.class);
            spec = bindNullable(spec, "declared_value_xaf", parcel.getDeclaredValueXaf(), Double.class);
            spec = bindNullable(spec, "distance_km", parcel.getDistanceKm(), Double.class);
            spec = bindNullable(spec, "delivery_fee_xaf", parcel.getDeliveryFeeXaf(), Double.class);
            spec = bindNullable(spec, "estimated_delivery_time", parcel.getEstimatedDeliveryTime(), LocalDateTime.class);
            spec = bindNullable(spec, "notes", parcel.getNotes(), String.class);
            
            return spec.map(this::mapRowToParcel)
                    .one()
                    .doOnSuccess(saved -> log.info("Parcel updated with ID: {}", saved.getId()))
                    .doOnError(e -> log.error("Error updating parcel {}", parcel.getId(), e));
        }
    }

    /**
     * Helper method to bind nullable fields.
     * If the value is null, it binds a null value of the specified type.
     */
    private <T> DatabaseClient.GenericExecuteSpec bindNullable(
            DatabaseClient.GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value != null) {
            return spec.bind(name, value);
        } else {
            return spec.bindNull(name, type);
        }
    }

    /**
     * Maps a database row to a Parcel entity.
     * Handles the conversion of PostGIS geometry to WKT string and enums.
     */
    private Parcel mapRowToParcel(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata metadata) {
        try {
            // Safely parse enums with fallback
            String stateString = row.get("current_state", String.class);
            ParcelState state;
            try {
                state = ParcelState.valueOf(stateString);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown ParcelState '{}', defaulting to PLANNED", stateString);
                state = ParcelState.PLANNED;
            }
            
            String priorityString = row.get("priority", String.class);
            ParcelPriority priority;
            try {
                priority = ParcelPriority.valueOf(priorityString);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown ParcelPriority '{}', defaulting to NORMAL", priorityString);
                priority = ParcelPriority.NORMAL;
            }
            
            return Parcel.builder()
                    .id(row.get("id", UUID.class))
                    .trackingCode(row.get("tracking_code", String.class))
                    .driverId(row.get("driver_id", UUID.class))
                    .vehicleId(row.get("vehicle_id", UUID.class))
                    .currentState(state)
                    .priority(priority)
                    .senderName(row.get("sender_name", String.class))
                    .senderPhone(row.get("sender_phone", String.class))
                    .recipientName(row.get("recipient_name", String.class))
                    .recipientPhone(row.get("recipient_phone", String.class))
                    .pickupLocation(row.get("pickup_location", String.class))
                    .pickupAddress(row.get("pickup_address", String.class))
                    .deliveryLocation(row.get("delivery_location", String.class))
                    .deliveryAddress(row.get("delivery_address", String.class))
                    .weightKg(
                            Optional.ofNullable(row.get("weight_kg", BigDecimal.class))
                                    .map(BigDecimal::doubleValue)
                                    .orElse(null)
                        )
                    .distanceKm(
                            Optional.ofNullable(row.get("distance_km", BigDecimal.class))
                                    .map(BigDecimal::doubleValue)
                                    .orElse(null)
                        )

                   .declaredValueXaf(
                        Optional.ofNullable(row.get("declared_value_xaf", BigDecimal.class))
                                .map(BigDecimal::doubleValue)
                                .orElse(null)
                    )
                    .deliveryFeeXaf(
                        Optional.ofNullable(row.get("delivery_fee_xaf", BigDecimal.class))
                                .map(BigDecimal::doubleValue)
                                .orElse(null)
                    )
                    .estimatedDeliveryTime(row.get("estimated_delivery_time", LocalDateTime.class))
                    .notes(row.get("notes", String.class))
                    .createdAt(row.get("created_at", LocalDateTime.class))
                    .updatedAt(row.get("updated_at", LocalDateTime.class))
                    .build();
        } catch (Exception e) {
            log.error("Error mapping row to Parcel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to map row to Parcel entity", e);
        }
    }
}