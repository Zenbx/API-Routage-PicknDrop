package com.yowyob.delivery.route.service.strategy;

import com.yowyob.delivery.route.domain.entity.Hub;
import com.yowyob.delivery.route.domain.entity.HubConnection;
import com.yowyob.delivery.route.mapper.HubMapper;
import com.yowyob.delivery.route.repository.HubConnectionRepository;
import com.yowyob.delivery.route.repository.HubRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DijkstraRoutingStrategyTest {

    @Mock
    HubConnectionRepository connectionRepository;

    @Mock
    HubRepository hubRepository;

    @Mock
    HubMapper hubMapper;

    @InjectMocks
    DijkstraRoutingStrategy strategy;

    @Test
    public void findsPathWhenBidirectionalConnectionsExist() throws Exception {
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();

        Hub hubA = Hub.builder().id(idA).location("POINT(9.0 4.0)").build();
        Hub hubB = Hub.builder().id(idB).location("POINT(11.0 3.8)").build();

        HubConnection connAtoB = HubConnection.builder().id(UUID.randomUUID()).fromHubId(idA).toHubId(idB).weight(100.0).build();
        HubConnection connBtoA = HubConnection.builder().id(UUID.randomUUID()).fromHubId(idB).toHubId(idA).weight(100.0).build();

        when(hubRepository.findAllWithLocation()).thenReturn(Flux.just(hubA, hubB));
        when(connectionRepository.findAll()).thenReturn(Flux.just(connAtoB, connBtoA));

        // Use actual WKT parsing for the mapper
        WKTReader reader = new WKTReader();
        Point ptA = (Point) reader.read(hubA.getLocation());
        Point ptB = (Point) reader.read(hubB.getLocation());
        when(hubMapper.wktToPoint(hubA.getLocation())).thenReturn(ptA);
        when(hubMapper.wktToPoint(hubB.getLocation())).thenReturn(ptB);

        StepVerifier.create(strategy.calculateOptimalRoute(hubA, hubB, null))
                .assertNext(route -> {
                    assert route.getTotalDistanceKm() != null;
                    assert route.getTotalDistanceKm().equals(100.0);
                    assert route.getRouteGeometry() != null && !route.getRouteGeometry().isEmpty();
                })
                .verifyComplete();

        // Reverse direction should also succeed
        StepVerifier.create(strategy.calculateOptimalRoute(hubB, hubA, null))
                .assertNext(route -> {
                    assert route.getTotalDistanceKm() != null;
                    assert route.getTotalDistanceKm().equals(100.0);
                    assert route.getRouteGeometry() != null && !route.getRouteGeometry().isEmpty();
                })
                .verifyComplete();
    }
}