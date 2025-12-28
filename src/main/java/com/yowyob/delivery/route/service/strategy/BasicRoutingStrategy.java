package com.yowyob.delivery.route.service.strategy;

import com.yowyob.delivery.route.controller.dto.RoutingConstraintsDTO;
import com.yowyob.delivery.route.domain.entity.Hub;
import com.yowyob.delivery.route.domain.entity.Route;
import com.yowyob.delivery.route.mapper.HubMapper;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Basic routing strategy implementing a direct point-to-point calculation.
 * Uses Euclidean distance (as the crow flies) and a simple duration multiplier.
 */
@Component
@RequiredArgsConstructor
public class BasicRoutingStrategy implements RoutingStrategy {

    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final HubMapper hubMapper;

    /**
     * {@inheritDoc}
     * Computes a direct LineString between start and end points and calculates
     * distance.
     */
    @Override
    public Mono<Route> calculateOptimalRoute(Hub start, Hub end, RoutingConstraintsDTO constraints) {
        Point startPt = hubMapper.wktToPoint(start.getLocation());
        Point endPt = hubMapper.wktToPoint(end.getLocation());

        Coordinate[] coordinates = new Coordinate[] {
                startPt.getCoordinate(),
                endPt.getCoordinate()
        };
        LineString path = geometryFactory.createLineString(coordinates);
        double distance = startPt.distance(endPt);

        return Mono.just(Route.builder()
                .routeGeometry(path.toText())
                .totalDistanceKm(distance)
                .estimatedDurationMinutes((int) (distance * 10))
                .routingService("BASIC")
                .isActive(true)
                .build());
    }

    /**
     * {@inheritDoc}
     * Basic strategy does not implement advanced recalculation; returns the current
     * route.
     */
    @Override
    public Mono<Route> recalculateRoute(Route currentRoute, Object incident) {
        return Mono.just(currentRoute);
    }
}
