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
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Routing strategy implementing Dijkstra's algorithm for finding the shortest
 * path in a graph.
 * Considers {@link HubConnection} weights as costs for pathfinding.
 */
@Component
@RequiredArgsConstructor
public class DijkstraRoutingStrategy implements RoutingStrategy {

    private final HubConnectionRepository connectionRepository;
    private final HubRepository hubRepository;
    private final HubMapper hubMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final WKTReader wktReader = new WKTReader();

    /**
     * {@inheritDoc}
     * Performs Dijkstra search over all hubs and connections to find the path with
     * minimum total weight.
     */
    @Override
    public Mono<Route> calculateOptimalRoute(Hub start, Hub end, RoutingConstraintsDTO constraints) {
        return calculateRouteWithExclusions(start, end, null);
    }

    private Mono<Route> calculateRouteWithExclusions(Hub start, Hub end, IncidentDTO incident) {
        return Mono.zip(hubRepository.findAllWithLocation().collectList(), connectionRepository.findAll().collectList())
                .flatMap(tuple -> {
                    List<Hub> allHubsOriginal = tuple.getT1();
                    List<HubConnection> allConnectionsOriginal = tuple.getT2();

                    final List<Hub> allHubs;
                    final List<HubConnection> allConnections;

                    // Filter hubs and connections if there's an incident
                    if (incident != null) {
                        allHubs = allHubsOriginal.stream()
                                .filter(h -> h.getId().equals(start.getId()) || h.getId().equals(end.getId()) ||
                                        !GeometryUtils.isHubInIncidentBuffer(h, incident, wktReader))
                                .toList();

                        Set<UUID> validHubIds = new HashSet<>();
                        allHubs.forEach(h -> validHubIds.add(h.getId()));

                        allConnections = allConnectionsOriginal.stream()
                                .filter(c -> {
                                    if (!validHubIds.contains(c.getFromHubId())
                                            || !validHubIds.contains(c.getToHubId())) {
                                        return false;
                                    }
                                    // Also check if the connection line itself intersects the incident
                                    Hub from = allHubs.stream().filter(h -> h.getId().equals(c.getFromHubId()))
                                            .findFirst().orElse(null);
                                    Hub to = allHubs.stream().filter(h -> h.getId().equals(c.getToHubId())).findFirst()
                                            .orElse(null);
                                    if (from != null && to != null) {
                                        try {
                                            Point p1 = (Point) wktReader.read(from.getLocation());
                                            Point p2 = (Point) wktReader.read(to.getLocation());
                                            return !GeometryUtils.doesRouteIntersectIncident(p1.getY(), p1.getX(),
                                                    p2.getY(), p2.getX(), incident);
                                        } catch (Exception e) {
                                            return true;
                                        }
                                    }
                                    return true;
                                })
                                .toList();
                    } else {
                        allHubs = allHubsOriginal;
                        allConnections = allConnectionsOriginal;
                    }

                    Map<UUID, Double> distances = new HashMap<>();
                    Map<UUID, UUID> previous = new HashMap<>();
                    PriorityQueue<HubDistance> pq = new PriorityQueue<>(Comparator.comparing(HubDistance::getDistance));

                    for (Hub hub : allHubs) {
                        distances.put(hub.getId(), Double.MAX_VALUE);
                    }
                    distances.put(start.getId(), 0.0);
                    pq.add(new HubDistance(start, 0.0));

                    while (!pq.isEmpty()) {
                        Hub current = pq.poll().getHub();
                        if (current.getId().equals(end.getId()))
                            break;

                        // Consider both directions so the graph behaves as undirected when appropriate
                        allConnections.stream()
                                .filter(c -> c.getFromHubId().equals(current.getId())
                                        || c.getToHubId().equals(current.getId()))
                                .forEach(connection -> {
                                    UUID neighborId;
                                    if (connection.getFromHubId().equals(current.getId())) {
                                        neighborId = connection.getToHubId();
                                    } else {
                                        neighborId = connection.getFromHubId();
                                    }

                                    double weight = connection.getWeight() == null ? 0.0 : connection.getWeight();
                                    double newDist = distances.get(current.getId()) + weight;

                                    if (newDist < distances.get(neighborId)) {
                                        distances.put(neighborId, newDist);
                                        previous.put(neighborId, current.getId());
                                        Hub neighbor = allHubs.stream().filter(h -> h.getId().equals(neighborId))
                                                .findFirst().orElseThrow();
                                        pq.add(new HubDistance(neighbor, newDist));
                                    }
                                });
                    }

                    return buildRouteFromPath(start, end, previous, distances.get(end.getId()), allHubs);
                });
    }

    /**
     * Reconstructs the {@link Route} object by backtracking through the 'previous'
     * map.
     *
     * @param start         origin hub
     * @param end           destination hub
     * @param previous      map of paths found during search
     * @param totalDistance accumulated path weight
     * @param allHubs       list of all available hubs
     * @return a Mono emitting the assembled route
     */
    private Mono<Route> buildRouteFromPath(Hub start, Hub end, Map<UUID, UUID> previous, Double totalDistance,
            List<Hub> allHubs) {
        if (!previous.containsKey(end.getId()) && !start.getId().equals(end.getId())) {
            return Mono.error(new com.yowyob.delivery.route.controller.exception.NoPathFoundException(
                    "No path found between hubs"));
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

        if (coordinates.isEmpty()) {
            return Mono.error(new com.yowyob.delivery.route.controller.exception.NoPathFoundException(
                    "No path found between hubs"));
        }

        // JTS LineString requires at least 2 points. If start == end we duplicate the
        // coordinate.
        if (coordinates.size() == 1) {
            Coordinate c = coordinates.get(0);
            coordinates.add(new Coordinate(c.x, c.y));
        }

        LineString path = geometryFactory.createLineString(coordinates.toArray(new Coordinate[0]));

        return Mono.just(Route.builder()
                .routeGeometry(path.toText())
                .totalDistanceKm(totalDistance)
                .estimatedDurationMinutes((int) (totalDistance * 10))
                .routingService("DIJKSTRA")
                .isActive(true)
                .build());
    }

    /**
     * {@inheritDoc}
     * Recalculates the route using the stored start and end hubs.
     * Takes incident into account by recalculating the path and marking it as
     * recalculated.
     */
    @Override
    public Mono<Route> recalculateRoute(Route currentRoute, IncidentDTO incident) {
        if (currentRoute.getStartHubId() == null || currentRoute.getEndHubId() == null) {
            // Fallback for legacy routes without stored hubs
            return Mono.just(currentRoute);
        }

        return Mono.zip(
                hubRepository.findById(currentRoute.getStartHubId()),
                hubRepository.findById(currentRoute.getEndHubId())).flatMap(tuple -> {
                    return calculateRouteWithExclusions(tuple.getT1(), tuple.getT2(), incident)
                            .map(newRoute -> {
                                newRoute.setId(currentRoute.getId()); // Keep same ID
                                newRoute.setParcelId(currentRoute.getParcelId());
                                newRoute.setDriverId(currentRoute.getDriverId());
                                newRoute.setStartHubId(currentRoute.getStartHubId());
                                newRoute.setEndHubId(currentRoute.getEndHubId());
                                newRoute.setCreatedAt(currentRoute.getCreatedAt());
                                newRoute.setRoutingService("DIJKSTRA_RECALC"); // Mark as recalculated
                                return newRoute;
                            });
                });
    }

    /**
     * Helper class representing a hub and its current calculated distance from the
     * start.
     */
    @lombok.Value
    private static class HubDistance {
        Hub hub;
        Double distance;
    }
}
