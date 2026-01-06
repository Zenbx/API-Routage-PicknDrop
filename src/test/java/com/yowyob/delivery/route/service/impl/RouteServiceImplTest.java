package com.yowyob.delivery.route.service.impl;

import com.yowyob.delivery.route.controller.dto.RouteCalculationRequestDTO;
import com.yowyob.delivery.route.controller.dto.RouteResponseDTO;
import com.yowyob.delivery.route.controller.dto.RoutingConstraintsDTO;
import com.yowyob.delivery.route.domain.entity.Hub;
import com.yowyob.delivery.route.domain.entity.Route;
import com.yowyob.delivery.route.mapper.RouteMapper;
import com.yowyob.delivery.route.repository.HubRepository;
import com.yowyob.delivery.route.repository.RouteRepository;
import com.yowyob.delivery.route.service.strategy.BasicRoutingStrategy;
import com.yowyob.delivery.route.service.strategy.DijkstraRoutingStrategy;
import com.yowyob.delivery.route.service.strategy.RoutingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceImplTest {

    @Mock
    private RouteRepository routeRepository;
    @Mock
    private HubRepository hubRepository;
    @Mock
    private RouteMapper routeMapper;
    @Mock
    private BasicRoutingStrategy basicStrategy;
    @Mock
    private DijkstraRoutingStrategy dijkstraStrategy;

    private RouteServiceImpl routeService;

    @BeforeEach
    void setUp() {
        List<RoutingStrategy> strategies = Arrays.asList(basicStrategy, dijkstraStrategy);
        routeService = new RouteServiceImpl(routeRepository, hubRepository, strategies, routeMapper);
    }

    @Test
    void shouldCalculateRouteUsingBasicStrategyByDefault() {
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        UUID parcelId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        Hub startHub = Hub.builder().id(startHubId).build();
        Hub endHub = Hub.builder().id(endHubId).build();
        Route route = Route.builder().id(UUID.randomUUID()).build();
        RouteResponseDTO responseDTO = new RouteResponseDTO();

        RouteCalculationRequestDTO request = new RouteCalculationRequestDTO();
        request.setStartHubId(startHubId);
        request.setEndHubId(endHubId);
        request.setParcelId(parcelId);
        request.setDriverId(driverId);

        when(hubRepository.findById(startHubId)).thenReturn(Mono.just(startHub));
        when(hubRepository.findById(endHubId)).thenReturn(Mono.just(endHub));
        when(basicStrategy.calculateOptimalRoute(any(), any(), any())).thenReturn(Mono.just(route));
        when(routeRepository.saveWithGeometry(any())).thenReturn(Mono.just(route));
        when(routeMapper.toResponseDTO(any())).thenReturn(responseDTO);

        Mono<RouteResponseDTO> result = routeService.calculateRoute(request);

        StepVerifier.create(result)
                .expectNext(responseDTO)
                .verifyComplete();

        verify(basicStrategy).calculateOptimalRoute(eq(startHub), eq(endHub), any());
        verify(routeRepository).saveWithGeometry(any());
    }

    @Test
    void shouldCalculateRouteUsingDijkstraStrategyWhenRequested() {
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        UUID parcelId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        Hub startHub = Hub.builder().id(startHubId).build();
        Hub endHub = Hub.builder().id(endHubId).build();
        Route route = Route.builder().id(UUID.randomUUID()).build();
        RouteResponseDTO responseDTO = new RouteResponseDTO();

        RoutingConstraintsDTO constraints = new RoutingConstraintsDTO();
        constraints.setAlgorithm("DIJKSTRA");

        RouteCalculationRequestDTO request = new RouteCalculationRequestDTO();
        request.setStartHubId(startHubId);
        request.setEndHubId(endHubId);
        request.setParcelId(parcelId);
        request.setDriverId(driverId);
        request.setConstraints(constraints);

        when(hubRepository.findById(startHubId)).thenReturn(Mono.just(startHub));
        when(hubRepository.findById(endHubId)).thenReturn(Mono.just(endHub));
        when(dijkstraStrategy.calculateOptimalRoute(any(), any(), any())).thenReturn(Mono.just(route));
        when(routeRepository.saveWithGeometry(any())).thenReturn(Mono.just(route));
        when(routeMapper.toResponseDTO(any())).thenReturn(responseDTO);

        Mono<RouteResponseDTO> result = routeService.calculateRoute(request);

        StepVerifier.create(result)
                .expectNext(responseDTO)
                .verifyComplete();

        verify(dijkstraStrategy).calculateOptimalRoute(eq(startHub), eq(endHub), eq(constraints));
    }

    @Test
    void shouldGetRouteById() {
        UUID routeId = UUID.randomUUID();
        Route route = Route.builder().id(routeId).build();
        RouteResponseDTO responseDTO = new RouteResponseDTO();

        when(routeRepository.findById(routeId)).thenReturn(Mono.just(route));
        when(routeMapper.toResponseDTO(route)).thenReturn(responseDTO);

        Mono<RouteResponseDTO> result = routeService.getRoute(routeId);

        StepVerifier.create(result)
                .expectNext(responseDTO)
                .verifyComplete();
    }
}
