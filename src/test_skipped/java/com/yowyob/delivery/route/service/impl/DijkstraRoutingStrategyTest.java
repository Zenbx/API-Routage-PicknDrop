package com.yowyob.delivery.route.service.impl;

import com.yowyob.delivery.route.controller.dto.RoutingConstraintsDTO;
import com.yowyob.delivery.route.domain.entity.Hub;
import com.yowyob.delivery.route.domain.entity.HubConnection;
import com.yowyob.delivery.route.domain.entity.Route;
import com.yowyob.delivery.route.mapper.HubMapper;
import com.yowyob.delivery.route.repository.HubConnectionRepository;
import com.yowyob.delivery.route.repository.HubRepository;
import com.yowyob.delivery.route.service.strategy.DijkstraRoutingStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DijkstraRoutingStrategyTest {

    @Mock
    private HubConnectionRepository connectionRepository;
    @Mock
    private HubRepository hubRepository;
    @Mock
    private HubMapper hubMapper;

    @InjectMocks
    private DijkstraRoutingStrategy dijkstraStrategy;

    private GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    void shouldCalculateRouteUsingDijkstra() {
        // Simple graph: A -> B (weight 10)
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        Point ptA = geometryFactory.createPoint(new Coordinate(0, 0));
        Point ptB = geometryFactory.createPoint(new Coordinate(1, 1));

        Hub hubA = Hub.builder().id(idA).location("POINT(0 0)").address("A").build();
        Hub hubB = Hub.builder().id(idB).location("POINT(1 1)").address("B").build();

        HubConnection conn = HubConnection.builder()
                .fromHubId(idA)
                .toHubId(idB)
                .weight(10.0)
                .build();

        when(hubRepository.findAllWithLocation()).thenReturn(reactor.core.publisher.Flux.just(hubA, hubB));
        when(connectionRepository.findAll()).thenReturn(reactor.core.publisher.Flux.just(conn));
        when(hubMapper.wktToPoint("POINT(0 0)")).thenReturn(ptA);
        when(hubMapper.wktToPoint("POINT(1 1)")).thenReturn(ptB);

        Mono<Route> routeMono = dijkstraStrategy.calculateOptimalRoute(hubA, hubB, new RoutingConstraintsDTO());
        Route route = routeMono.block();

        assertNotNull(route);
        assertNotNull(route.getRouteGeometry());

        // Reverse direction should also succeed when treating edges as undirected
        Mono<Route> reverse = dijkstraStrategy.calculateOptimalRoute(hubB, hubA, new RoutingConstraintsDTO());
        Route reverseRoute = reverse.block();
        assertNotNull(reverseRoute);
        assertNotNull(reverseRoute.getRouteGeometry());
    }
}
