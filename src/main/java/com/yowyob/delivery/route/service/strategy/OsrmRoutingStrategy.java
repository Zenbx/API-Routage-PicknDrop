package com.yowyob.delivery.route.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public Mono<Route> recalculateRoute(Route currentRoute, Object incident) {
        if (currentRoute.getStartHubId() == null || currentRoute.getEndHubId() == null) {
            return Mono.just(currentRoute);
        }

        return Mono.zip(
                hubRepository.findById(currentRoute.getStartHubId()),
                hubRepository.findById(currentRoute.getEndHubId())).flatMap(tuple -> {
                    // Re-calculate using OSRM
                    return calculateOptimalRoute(tuple.getT1(), tuple.getT2(), null)
                            .map(newRoute -> {
                                newRoute.setId(currentRoute.getId());
                                newRoute.setParcelId(currentRoute.getParcelId());
                                newRoute.setDriverId(currentRoute.getDriverId());
                                newRoute.setStartHubId(currentRoute.getStartHubId());
                                newRoute.setEndHubId(currentRoute.getEndHubId());
                                newRoute.setCreatedAt(currentRoute.getCreatedAt());
                                return newRoute;
                            });
                });
    }
}
