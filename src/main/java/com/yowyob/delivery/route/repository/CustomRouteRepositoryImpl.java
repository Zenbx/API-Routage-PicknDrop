package com.yowyob.delivery.route.repository;

import com.yowyob.delivery.route.domain.entity.Route;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class CustomRouteRepositoryImpl implements CustomRouteRepository {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Route> saveWithGeometry(Route route) {
        if (route.getId() == null) {
            return insertWithGeometry(route);
        } else {
            return updateWithGeometry(route);
        }
    }

    private Mono<Route> insertWithGeometry(Route route) {
        UUID id = UUID.randomUUID();
        return databaseClient.sql("INSERT INTO routes (id, parcel_id, driver_id, route_geometry, waypoints, total_distance_km, estimated_duration_minutes, routing_service, traffic_factor, is_active, created_at) " +
                "VALUES (:id, :parcel_id, :driver_id, ST_GeomFromText(:route_geometry, 4326), :waypoints::jsonb, :total_distance_km, :estimated_duration_minutes, :routing_service, :traffic_factor, :is_active, :created_at)")
                .bind("id", id)
                .bind("parcel_id", route.getParcelId())
                .bind( "driver_id", route.getDriverId() == null ? null : route.getDriverId())
                .bind("route_geometry", route.getRouteGeometry())
                .bind("waypoints", route.getWaypoints() == null ? "[]" : route.getWaypoints())
                .bind("total_distance_km", route.getTotalDistanceKm())
                .bind("estimated_duration_minutes", route.getEstimatedDurationMinutes())
                .bind("routing_service", route.getRoutingService())
                .bind("traffic_factor", route.getTrafficFactor() == null ? 1.0 : route.getTrafficFactor())
                .bind("is_active", route.getIsActive() == null ? true : route.getIsActive())
                .bind("created_at", LocalDateTime.now())
                .fetch()
                .rowsUpdated()
                .thenReturn(route)
                .map(r -> {
                    r.setId(id);
                    return r;
                });
    }

    private Mono<Route> updateWithGeometry(Route route) {
        return databaseClient.sql("UPDATE routes SET parcel_id = :parcel_id, driver_id = :driver_id, route_geometry = ST_GeomFromText(:route_geometry, 4326), " +
                "waypoints = :waypoints::jsonb, total_distance_km = :total_distance_km, estimated_duration_minutes = :estimated_duration_minutes, " +
                "routing_service = :routing_service, traffic_factor = :traffic_factor, is_active = :is_active WHERE id = :id")
                .bind("id", route.getId())
                .bind("parcel_id", route.getParcelId())
                .bind("driver_id", route.getDriverId() == null ? null : route.getDriverId())
                .bind("route_geometry", route.getRouteGeometry())
                .bind("waypoints", route.getWaypoints() == null ? "[]" : route.getWaypoints())
                .bind("total_distance_km", route.getTotalDistanceKm())
                .bind("estimated_duration_minutes", route.getEstimatedDurationMinutes())
                .bind("routing_service", route.getRoutingService())
                .bind("traffic_factor", route.getTrafficFactor() == null ? 1.0 : route.getTrafficFactor())
                .bind("is_active", route.getIsActive() == null ? true : route.getIsActive())
                .fetch()
                .rowsUpdated()
                .thenReturn(route);
    }
}
