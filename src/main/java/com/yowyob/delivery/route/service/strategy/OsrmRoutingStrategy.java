package com.yowyob.delivery.route.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.delivery.route.controller.dto.IncidentDTO;
import com.yowyob.delivery.route.controller.dto.RoutingConstraintsDTO;
import com.yowyob.delivery.route.domain.entity.Hub;
import com.yowyob.delivery.route.domain.entity.Route;
import com.yowyob.delivery.route.mapper.HubMapper;
import com.yowyob.delivery.route.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Routing strategy using OSRM (Open Source Routing Machine) API.
 * Fetches real-world driving routes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OsrmRoutingStrategy implements RoutingStrategy {

    private final WebClient.Builder webClientBuilder;
    private final HubMapper hubMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WKTReader wktReader = new WKTReader();
    private final HubRepository hubRepository;

    @Value("${osrm.api-url:http://router.project-osrm.org/route/v1/driving}")
    private String osrmApiUrl;

    @Override
    public Mono<Route> calculateOptimalRoute(Hub start, Hub end, RoutingConstraintsDTO constraints) {
        try {
            org.locationtech.jts.geom.Point startPoint = (org.locationtech.jts.geom.Point) wktReader
                    .read(start.getLocation());
            org.locationtech.jts.geom.Point endPoint = (org.locationtech.jts.geom.Point) wktReader
                    .read(end.getLocation());

            // Prepare coordinates for OSRM: longitude,latitude;longitude,latitude
            String coordinates = String.format(java.util.Locale.US, "%f,%f;%f,%f",
                    startPoint.getX(), startPoint.getY(),
                    endPoint.getX(), endPoint.getY());

            String url = String.format("%s/%s?overview=full&geometries=geojson", osrmApiUrl, coordinates);

            log.info("Requesting OSRM route: {}", url);

            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(json -> parseOsrmResponse(json, startPoint, endPoint, start.getId(), end.getId()));
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to parse hub locations", e));
        }
    }

    private Mono<Route> parseOsrmResponse(String jsonResponse, org.locationtech.jts.geom.Point startPoint,
            org.locationtech.jts.geom.Point endPoint, java.util.UUID startHubId, java.util.UUID endHubId) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode routesNode = root.path("routes");

            if (routesNode.isEmpty()) {
                return Mono.error(new RuntimeException("No route found by OSRM"));
            }

            JsonNode primaryRoute = routesNode.get(0);
            double distanceMeters = primaryRoute.path("distance").asDouble();
            double durationSeconds = primaryRoute.path("duration").asDouble();
            JsonNode geometryNode = primaryRoute.path("geometry");

            // Convert GeoJSON LineString to WKT or JTS Geometry
            // OSRM returns coordinates as [lon, lat]
            List<Coordinate> coordinates = new ArrayList<>();
            JsonNode coordsArray = geometryNode.path("coordinates");
            for (JsonNode coordNode : coordsArray) {
                double lon = coordNode.get(0).asDouble();
                double lat = coordNode.get(1).asDouble();
                coordinates.add(new Coordinate(lon, lat));
            }

            if (coordinates.size() < 2) {
                // Determine if fallback is needed, but for now just error or create simple line
                coordinates.add(new Coordinate(startPoint.getX(), startPoint.getY()));
                coordinates.add(new Coordinate(endPoint.getX(), endPoint.getY()));
            }

            LineString lineString = geometryFactory.createLineString(coordinates.toArray(new Coordinate[0]));

            return Mono.just(Route.builder()
                    .routeGeometry(lineString.toText())
                    .totalDistanceKm(distanceMeters / 1000.0)
                    .estimatedDurationMinutes((int) (durationSeconds / 60))
                    .routingService("OSRM")
                    .isActive(true)
                    .startHubId(startHubId)
                    .endHubId(endHubId)
                    .build());

        } catch (Exception e) {
            log.error("Failed to parse OSRM response", e);
            return Mono.error(new RuntimeException("Failed to process routing response", e));
        }
    }

    @Override
    public Mono<Route> recalculateRoute(Route currentRoute, IncidentDTO incident) {
        log.info("=== OSRM STRATEGY: recalculateRoute called ===");

        if (currentRoute.getStartHubId() == null || currentRoute.getEndHubId() == null) {
            log.warn("No hub IDs in route, cannot recalculate");
            return Mono.just(currentRoute);
        }

        // Validate incident data
        if (incident == null || incident.getLineStart() == null || incident.getLineEnd() == null) {
            log.warn("Incomplete incident data, returning original route");
            return Mono.just(currentRoute);
        }

        return Mono.zip(
                hubRepository.findById(currentRoute.getStartHubId()),
                hubRepository.findById(currentRoute.getEndHubId())).flatMap(tuple -> {
                    try {
                        Hub startHub = tuple.getT1();
                        Hub endHub = tuple.getT2();

                        // Extract current position from route geometry (first point)
                        String wkt = currentRoute.getRouteGeometry();
                        if (wkt == null || !wkt.startsWith("LINESTRING")) {
                            log.warn("Invalid route geometry");
                            return Mono.just(currentRoute);
                        }

                        // Parse WKT to get current position (first coordinate) using WKTReader
                        LineString routeLine = (LineString) wktReader.read(wkt);
                        if (routeLine.getNumPoints() < 2) {
                            log.warn("Route geometry has insufficient points");
                            return Mono.just(currentRoute);
                        }

                        // Current position is the first point in the route
                        Coordinate firstCoord = routeLine.getCoordinateN(0);
                        double currentLng = firstCoord.x;
                        double currentLat = firstCoord.y;

                        // End position
                        org.locationtech.jts.geom.Point endPoint = (org.locationtech.jts.geom.Point) wktReader
                                .read(endHub.getLocation());
                        double endLng = endPoint.getX();
                        double endLat = endPoint.getY();

                        log.info("Current position: ({}, {})", currentLng, currentLat);
                        log.info("End position: ({}, {})", endLng, endLat);

                        // Check if route intersects incident using GeometryUtils
                        boolean intersects = GeometryUtils.doesRouteIntersectIncident(
                                currentLat, currentLng, endLat, endLng, incident);

                        if (!intersects) {
                            log.info("Route does NOT intersect incident buffer - no recalculation needed");
                            return Mono.just(currentRoute);
                        }

                        log.info("Route INTERSECTS incident - calculating intelligent waypoint");

                        // Calculate incident line midpoint and direction
                        double incidentStartLat = incident.getLineStart().getLatitude();
                        double incidentStartLng = incident.getLineStart().getLongitude();
                        double incidentEndLat = incident.getLineEnd().getLatitude();
                        double incidentEndLng = incident.getLineEnd().getLongitude();

                        double incidentMidLat = (incidentStartLat + incidentEndLat) / 2;
                        double incidentMidLng = (incidentStartLng + incidentEndLng) / 2;

                        // Calculate incident line direction vector
                        double incidentDx = incidentEndLng - incidentStartLng;
                        double incidentDy = incidentEndLat - incidentStartLat;
                        double incidentLength = Math.sqrt(incidentDx * incidentDx + incidentDy * incidentDy);

                        // Normalize
                        double incidentUnitX = incidentDx / incidentLength;
                        double incidentUnitY = incidentDy / incidentLength;

                        // Calculate perpendicular vector (rotate 90° counterclockwise)
                        double perpX = -incidentUnitY;
                        double perpY = incidentUnitX;

                        log.info("Incident midpoint: ({}, {})", incidentMidLng, incidentMidLat);
                        log.info("Perpendicular direction: ({}, {})", perpX, perpY);

                        // Calculate waypoint distance: buffer + safety margin
                        double bufferMeters = incident.getBufferDistance();
                        double bufferDeg = bufferMeters / 111000.0; // Convert to degrees
                        double safetyMargin = 0.002; // ~220m
                        double waypointDistance = bufferDeg + safetyMargin;

                        log.info("Buffer: {}m ({}°), Total waypoint distance: {}°", bufferMeters, bufferDeg,
                                waypointDistance);

                        // Determine which side of the incident to place the waypoint
                        // Project current position onto perpendicular axis
                        double currentToIncidentX = currentLng - incidentMidLng;
                        double currentToIncidentY = currentLat - incidentMidLat;
                        double projectionOnPerp = currentToIncidentX * perpX + currentToIncidentY * perpY;

                        // Choose the same side as current position, or opposite if too close
                        double direction = (projectionOnPerp >= 0) ? 1.0 : -1.0;

                        // Calculate waypoint position
                        double waypointLng = incidentMidLng + perpX * waypointDistance * direction;
                        double waypointLat = incidentMidLat + perpY * waypointDistance * direction;

                        log.info("Waypoint calculated at: ({}, {}) with direction: {}", waypointLng, waypointLat,
                                direction);

                        // Verify waypoint is outside incident buffer
                        double waypointToIncidentDist = Math.sqrt(
                                Math.pow(waypointLng - incidentMidLng, 2) +
                                        Math.pow(waypointLat - incidentMidLat, 2));
                        log.info("Waypoint distance from incident: {}° (should be > {}°)", waypointToIncidentDist,
                                bufferDeg);

                        // Create OSRM request with 3 points: current position -> waypoint -> end
                        String coordinates = String.format(java.util.Locale.US, "%f,%f;%f,%f;%f,%f",
                                currentLng, currentLat,
                                waypointLng, waypointLat,
                                endLng, endLat);

                        String url = String.format("%s/%s?overview=full&geometries=geojson", osrmApiUrl, coordinates);
                        log.info("Requesting OSRM detour route: {}", url);

                        return webClientBuilder.build()
                                .get()
                                .uri(url)
                                .retrieve()
                                .bodyToMono(String.class)
                                .flatMap(json -> {
                                    try {
                                        org.locationtech.jts.geom.Point startPoint = geometryFactory
                                                .createPoint(new Coordinate(currentLng, currentLat));
                                        return parseOsrmResponse(json, startPoint, endPoint, startHub.getId(),
                                                endHub.getId());
                                    } catch (Exception e) {
                                        log.error("Failed to parse detour response", e);
                                        return Mono.error(new RuntimeException("Failed to parse detour response", e));
                                    }
                                })
                                .map(newRoute -> {
                                    newRoute.setId(currentRoute.getId());
                                    newRoute.setParcelId(currentRoute.getParcelId());
                                    newRoute.setDriverId(currentRoute.getDriverId());
                                    newRoute.setStartHubId(currentRoute.getStartHubId());
                                    newRoute.setEndHubId(currentRoute.getEndHubId());
                                    newRoute.setCreatedAt(currentRoute.getCreatedAt());
                                    newRoute.setRoutingService("OSRM_DETOUR");
                                    log.info("=== OSRM DETOUR: Route updated successfully ===");
                                    return newRoute;
                                });

                    } catch (Exception e) {
                        log.error("Error during route recalculation", e);
                        return Mono.error(new RuntimeException("Failed to recalculate route", e));
                    }
                });
    }

    private Mono<Route> calculateRouteWithWaypoint(Hub start, Hub end, double waypointLat, double waypointLng,
            Route currentRoute) {
        try {
            org.locationtech.jts.geom.Point startPoint = (org.locationtech.jts.geom.Point) wktReader
                    .read(start.getLocation());
            org.locationtech.jts.geom.Point endPoint = (org.locationtech.jts.geom.Point) wktReader
                    .read(end.getLocation());

            // Create OSRM request with 3 points: start -> waypoint -> end
            String coordinates = String.format(java.util.Locale.US, "%f,%f;%f,%f;%f,%f",
                    startPoint.getX(), startPoint.getY(),
                    waypointLng, waypointLat,
                    endPoint.getX(), endPoint.getY());

            String url = String.format("%s/%s?overview=full&geometries=geojson", osrmApiUrl, coordinates);

            log.info("Requesting OSRM route with detour waypoint: {}", url);

            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(json -> parseOsrmResponse(json, startPoint, endPoint, start.getId(), end.getId()))
                    .map(newRoute -> {
                        newRoute.setId(currentRoute.getId());
                        newRoute.setParcelId(currentRoute.getParcelId());
                        newRoute.setDriverId(currentRoute.getDriverId());
                        newRoute.setStartHubId(currentRoute.getStartHubId());
                        newRoute.setEndHubId(currentRoute.getEndHubId());
                        newRoute.setCreatedAt(currentRoute.getCreatedAt());
                        newRoute.setRoutingService("OSRM_DETOUR");
                        return newRoute;
                    });
        } catch (Exception e) {
            log.error("Failed to calculate route with waypoint", e);
            return Mono.error(new RuntimeException("Failed to calculate detour route", e));
        }
    }
}
