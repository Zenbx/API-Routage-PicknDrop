package com.yowyob.delivery.route.repository;

import com.yowyob.delivery.route.domain.entity.Hub;
import com.yowyob.delivery.route.domain.enums.HubType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Custom implementation for Hub repository methods that require
 * manual row mapping, particularly for PostGIS geometry functions.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomHubRepositoryImpl implements CustomHubRepository {

    private final DatabaseClient databaseClient;

    @Override
    public Flux<Hub> findAllWithLocation() {
        String sql = """
            SELECT 
                id, 
                address, 
                type, 
                ST_AsText(location) as location, 
                created_at, 
                updated_at 
            FROM hubs
            ORDER BY created_at DESC
            """;
        
        return databaseClient.sql(sql)
                .map(this::mapRowToHub)
                .all()
                .doOnSubscribe(s -> log.debug("Fetching all hubs with location"))
                .doOnError(e -> log.error("Error fetching hubs", e));
    }

    @Override
    public Mono<Hub> findByIdWithLocation(UUID id) {
        String sql = """
            SELECT 
                id, 
                address, 
                type, 
                ST_AsText(location) as location, 
                created_at, 
                updated_at 
            FROM hubs 
            WHERE id = :id
            """;
        
        return databaseClient.sql(sql)
                .bind("id", id)
                .map(this::mapRowToHub)
                .one()
                .doOnSubscribe(s -> log.debug("Fetching hub with id: {}", id))
                .doOnError(e -> log.error("Error fetching hub {}", id, e));
    }

    @Override
    public Mono<Hub> saveWithGeometry(Hub hub) {
        if (hub.getId() == null) {
            // INSERT avec nouveau UUID
            String sql = """
                INSERT INTO hubs (id, address, type, location, created_at, updated_at)
                VALUES (:id, :address, :type, ST_GeomFromText(:location, 4326), NOW(), NOW())
                RETURNING id, address, type, ST_AsText(location) as location, created_at, updated_at
                """;
            
            UUID newId = UUID.randomUUID();
            
            return databaseClient.sql(sql)
                    .bind("id", newId)
                    .bind("address", hub.getAddress())
                    .bind("type", hub.getType().name())
                    .bind("location", hub.getLocation())
                    .map(this::mapRowToHub)
                    .one()
                    .doOnSuccess(saved -> log.info("Hub created with ID: {}", saved.getId()))
                    .doOnError(e -> log.error("Error saving hub", e));
        } else {
            // UPDATE
            String sql = """
                UPDATE hubs 
                SET address = :address, 
                    type = :type, 
                    location = ST_GeomFromText(:location, 4326),
                    updated_at = NOW()
                WHERE id = :id
                RETURNING id, address, type, ST_AsText(location) as location, created_at, updated_at
                """;
            
            return databaseClient.sql(sql)
                    .bind("id", hub.getId())
                    .bind("address", hub.getAddress())
                    .bind("type", hub.getType().name())
                    .bind("location", hub.getLocation())
                    .map(this::mapRowToHub)
                    .one()
                    .doOnSuccess(saved -> log.info("Hub updated with ID: {}", saved.getId()))
                    .doOnError(e -> log.error("Error updating hub {}", hub.getId(), e));
        }
    }

    /**
     * Maps a database row to a Hub entity.
     * Handles the conversion of PostGIS geometry to WKT string.
     */
    private Hub mapRowToHub(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata metadata) {
        try {
            String typeString = row.get("type", String.class);
            HubType hubType;
            
            // Safely parse HubType enum
            try {
                hubType = HubType.valueOf(typeString);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown HubType '{}', defaulting to WAREHOUSE", typeString);
                hubType = HubType.WAREHOUSE; // Default fallback
            }
            
            return Hub.builder()
                    .id(row.get("id", UUID.class))
                    .address(row.get("address", String.class))
                    .type(hubType)
                    .location(row.get("location", String.class)) // WKT format from ST_AsText
                    .createdAt(row.get("created_at", LocalDateTime.class))
                    .updatedAt(row.get("updated_at", LocalDateTime.class))
                    .build();
        } catch (Exception e) {
            log.error("Error mapping row to Hub: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to map row to Hub entity", e);
        }
    }
}