package com.yowyob.delivery.route.service.impl;

import com.yowyob.delivery.route.controller.dto.RouteCalculationRequestDTO;
import com.yowyob.delivery.route.controller.dto.RouteResponseDTO;
import com.yowyob.delivery.route.controller.dto.RoutingConstraintsDTO;
import com.yowyob.delivery.route.domain.entity.Hub;
import com.yowyob.delivery.route.mapper.RouteMapper;
import com.yowyob.delivery.route.repository.HubRepository;
import com.yowyob.delivery.route.repository.RouteRepository;
import com.yowyob.delivery.route.service.RouteService;
import com.yowyob.delivery.route.service.strategy.AStarRoutingStrategy;
import com.yowyob.delivery.route.service.strategy.BasicRoutingStrategy;
import com.yowyob.delivery.route.service.strategy.DijkstraRoutingStrategy;
import com.yowyob.delivery.route.service.strategy.RoutingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link RouteService} using R2DBC for reactive persistence.
 * Leverages multiple {@link RoutingStrategy} implementations to calculate
 * paths.
 */
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

        private final RouteRepository routeRepository;
        private final HubRepository hubRepository;
        private final List<RoutingStrategy> routingStrategies;
        private final RouteMapper routeMapper;

        /**
         * {@inheritDoc}
         * Fetches start and end hubs, selects an appropriate routing strategy based on
         * constraints,
         * calculates the route, associates it with the parcel, and saves it.
         */
        @Override
        public Mono<RouteResponseDTO> calculateRoute(RouteCalculationRequestDTO request) {
                return Mono.zip(
                                hubRepository.findById(request.getStartHubId()),
                                hubRepository.findById(request.getEndHubId())).flatMap(tuple -> {
                                        Hub start = tuple.getT1();
                                        Hub end = tuple.getT2();

                                        RoutingStrategy strategy = selectStrategy(request.getConstraints());
                                        return strategy.calculateOptimalRoute(start, end, request.getConstraints())
                                                        .map(route -> {
                                                                route.setParcelId(request.getParcelId());
                                                                return route;
                                                        })
                                                        .flatMap(routeRepository::saveWithGeometry)
                                                        .map(routeMapper::toResponseDTO);
                                });
        }

        /**
         * {@inheritDoc}
         * Finds the existing route and applies a recalculation strategy in response to
         * an incident.
         */
        @Override
        public Mono<RouteResponseDTO> recalculateRoute(UUID routeId, Object incident) {
                return routeRepository.findById(routeId)
                                .flatMap(route -> {
                                        RoutingStrategy strategy = selectStrategy(null); // Default or based on route
                                        return strategy.recalculateRoute(route, incident)
                                                        .flatMap(routeRepository::saveWithGeometry)
                                                        .map(routeMapper::toResponseDTO);
                                });
        }

        /**
         * Selects the appropriate routing algorithm strategy based on provided
         * constraints.
         *
         * @param constraints the routing constraints (e.g., preference for DIJKSTRA or
         *                    ASTAR)
         * @return the selected routing strategy implementation
         */
        private RoutingStrategy selectStrategy(RoutingConstraintsDTO constraints) {
                String algo = (constraints != null && constraints.getAlgorithm() != null)
                                ? constraints.getAlgorithm().toUpperCase()
                                : "BASIC";

                return switch (algo) {
                        case "DIJKSTRA" -> routingStrategies.stream()
                                        .filter(s -> s instanceof DijkstraRoutingStrategy)
                                        .findFirst()
                                        .orElseThrow();
                        case "ASTAR" -> routingStrategies.stream()
                                        .filter(s -> s instanceof AStarRoutingStrategy)
                                        .findFirst()
                                        .orElseThrow();
                        default -> routingStrategies.stream()
                                        .filter(s -> s instanceof BasicRoutingStrategy)
                                        .findFirst()
                                        .orElseThrow();
                };
        }

        /**
         * {@inheritDoc}
         * Retrieves a specific route from the database.
         */
        @Override
        public Mono<RouteResponseDTO> getRoute(UUID id) {
                return routeRepository.findById(id)
                                .map(routeMapper::toResponseDTO);
        }
}
