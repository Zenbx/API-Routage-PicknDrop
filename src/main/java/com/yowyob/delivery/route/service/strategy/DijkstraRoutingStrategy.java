package com.yowyob.delivery.route.service.strategy;

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

    /**
     * {@inheritDoc}
     * Performs Dijkstra search over all hubs and connections to find the path with
     * minimum total weight.
     */
    @Override
    public Mono<Route> calculateOptimalRoute(Hub start, Hub end, RoutingConstraintsDTO constraints) {
        return Mono.zip(hubRepository.findAllWithLocation().collectList(), connectionRepository.findAll().collectList())
                .flatMap(tuple -> {
                    List<Hub> allHubs = tuple.getT1();
                    List<HubConnection> allConnections = tuple.getT2();

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

                        allConnections.stream()
                                .filter(c -> c.getFromHubId().equals(current.getId()))
                                .forEach(connection -> {
                                    UUID neighborId = connection.getToHubId();
                                    double newDist = distances.get(current.getId()) + connection.getWeight();

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
                .routingService("DIJKSTRA")
                .isActive(true)
                .build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Route> recalculateRoute(Route currentRoute, Object incident) {
        return Mono.just(currentRoute);
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
