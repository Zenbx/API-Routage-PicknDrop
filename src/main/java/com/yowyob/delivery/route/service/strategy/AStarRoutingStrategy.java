package com.yowyob.delivery.route.service.strategy;

import com.yowyob.delivery.route.controller.dto.IncidentDTO;
import com.yowyob.delivery.route.controller.dto.RoutingConstraintsDTO;
import com.yowyob.delivery.route.domain.entity.Hub;
import com.yowyob.delivery.route.domain.entity.HubConnection;
import com.yowyob.delivery.route.domain.entity.Route;
import com.yowyob.delivery.route.mapper.HubMapper;
import com.yowyob.delivery.route.repository.HubConnectionRepository;
import com.yowyob.delivery.route.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Routing strategy implementing the A* algorithm.
 * Extends Dijkstra's algorithm by using a heuristic function to guide the
 * search towards the destination.
 */
@Component
@RequiredArgsConstructor
public class AStarRoutingStrategy implements RoutingStrategy {

    private final HubConnectionRepository connectionRepository;
    private final HubRepository hubRepository;
    private final HubMapper hubMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * {@inheritDoc}
     * Executes A* search using Euclidean distance as the heuristic (h-score).
     */
    @Override
    public Mono<Route> calculateOptimalRoute(Hub start, Hub end, RoutingConstraintsDTO constraints) {
        return Mono.zip(hubRepository.findAll().collectList(), connectionRepository.findAll().collectList())
                .flatMap(tuple -> {
                    List<Hub> allHubs = tuple.getT1();
                    List<HubConnection> allConnections = tuple.getT2();

                    Map<UUID, Double> gScore = new HashMap<>();
                    Map<UUID, Double> fScore = new HashMap<>();
                    Map<UUID, UUID> previous = new HashMap<>();
                    PriorityQueue<HubScore> pq = new PriorityQueue<>(Comparator.comparing(HubScore::getFScore));

                    for (Hub hub : allHubs) {
                        gScore.put(hub.getId(), Double.MAX_VALUE);
                        fScore.put(hub.getId(), Double.MAX_VALUE);
                    }

                    gScore.put(start.getId(), 0.0);
                    double hStart = calculateHeuristic(start, end);
                    fScore.put(start.getId(), hStart);
                    pq.add(new HubScore(start, hStart));

                    while (!pq.isEmpty()) {
                        Hub current = pq.poll().getHub();
                        if (current.getId().equals(end.getId()))
                            break;

                        allConnections.stream()
                                .filter(c -> c.getFromHubId().equals(current.getId()))
                                .forEach(connection -> {
                                    UUID neighborId = connection.getToHubId();
                                    double tentativeGScore = gScore.get(current.getId()) + connection.getWeight();

                                    if (tentativeGScore < gScore.get(neighborId)) {
                                        previous.put(neighborId, current.getId());
                                        gScore.put(neighborId, tentativeGScore);
                                        Hub neighbor = allHubs.stream().filter(h -> h.getId().equals(neighborId))
                                                .findFirst().orElseThrow();
                                        double hNeighbor = calculateHeuristic(neighbor, end);
                                        fScore.put(neighborId, tentativeGScore + hNeighbor);

                                        if (pq.stream().noneMatch(hs -> hs.getHub().getId().equals(neighborId))) {
                                            pq.add(new HubScore(neighbor, fScore.get(neighborId)));
                                        }
                                    }
                                });
                    }

                    return buildRouteFromPath(start, end, previous, gScore.get(end.getId()), allHubs);
                });
    }

    /**
     * Calculates the heuristic estimate (h-score) using straight-line distance.
     *
     * @param hub current hub
     * @param end destination hub
     * @return the Euclidean distance between points
     */
    private double calculateHeuristic(Hub hub, Hub end) {
        Point p1 = hubMapper.wktToPoint(hub.getLocation());
        Point p2 = hubMapper.wktToPoint(end.getLocation());
        return p1 != null && p2 != null ? p1.distance(p2) : 0.0;
    }

    /**
     * Reconstructs the {@link Route} object by backtracking through the search
     * tree.
     *
     * @param start         origin
     * @param end           destination
     * @param previous      map of path predecessors
     * @param totalDistance accumulated g-score
     * @param allHubs       helper list for point lookup
     * @return a Mono emitting the final route
     */
    private Mono<Route> buildRouteFromPath(Hub start, Hub end, Map<UUID, UUID> previous, Double totalDistance,
            List<Hub> allHubs) {
        if (!previous.containsKey(end.getId()) && !start.getId().equals(end.getId())) {
            return Mono.error(new RuntimeException("No path found"));
        }

        List<Coordinate> coordinates = new ArrayList<>();
        UUID currentId = end.getId();
        while (currentId != null) {
            final UUID finalId = currentId;
            Hub hub = allHubs.stream().filter(h -> h.getId().equals(finalId)).findFirst().orElseThrow();
            Point pt = hubMapper.wktToPoint(hub.getLocation());
            coordinates.add(0, pt.getCoordinate());
            currentId = previous.get(currentId);
        }

        LineString path = geometryFactory.createLineString(coordinates.toArray(new Coordinate[0]));

        return Mono.just(Route.builder()
                .routeGeometry(path.toText())
                .totalDistanceKm(totalDistance)
                .estimatedDurationMinutes((int) (totalDistance * 10))
                .routingService("ASTAR")
                .isActive(true)
                .build());
    }

    /**
     * {@inheritDoc}
     * Recalculates the route using A* algorithm with the stored start and end hubs.
     */
    @Override
    public Mono<Route> recalculateRoute(Route currentRoute, IncidentDTO incident) {
        if (currentRoute.getStartHubId() == null || currentRoute.getEndHubId() == null) {
            // Fallback for legacy routes without stored hubs
            return Mono.just(currentRoute);
        }

        return Mono.zip(
            hubRepository.findById(currentRoute.getStartHubId()),
            hubRepository.findById(currentRoute.getEndHubId())
        ).flatMap(tuple -> {
             // Recalculate using A* - the heuristic will naturally find alternative paths
             return calculateOptimalRoute(tuple.getT1(), tuple.getT2(), null)
                 .map(newRoute -> {
                     newRoute.setId(currentRoute.getId());
                     newRoute.setParcelId(currentRoute.getParcelId());
                     newRoute.setDriverId(currentRoute.getDriverId());
                     newRoute.setStartHubId(currentRoute.getStartHubId());
                     newRoute.setEndHubId(currentRoute.getEndHubId());
                     newRoute.setCreatedAt(currentRoute.getCreatedAt());
                     newRoute.setRoutingService("ASTAR_RECALC"); // Mark as recalculated
                     return newRoute;
                 });
        });
    }

    /**
     * Helper class representing a hub and its current A* f-score (g + h).
     */
    @lombok.Value
    private static class HubScore {
        Hub hub;
        Double fScore;
    }
}
