package com.yowyob.delivery.route.mapper;

import com.yowyob.delivery.route.controller.dto.RouteResponseDTO;
import com.yowyob.delivery.route.domain.entity.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RouteMapperTest {

    private RouteMapper routeMapper;

    @Spy
    private HubMapper hubMapper = Mappers.getMapper(HubMapper.class);

    @BeforeEach
    void setUp() {
        routeMapper = Mappers.getMapper(RouteMapper.class);
        ReflectionTestUtils.setField(routeMapper, "hubMapper", hubMapper);
    }

    @Test
    void shouldMapRouteToResponseDTO() {
        Route route = Route.builder()
                .id(UUID.randomUUID())
                .parcelId(UUID.randomUUID())
                .driverId(UUID.randomUUID())
                .routeGeometry("LINESTRING(10 10, 20 20)")
                .totalDistanceKm(15.5)
                .estimatedDurationMinutes(30)
                .routingService("DIJKSTRA")
                .isActive(true)
                .build();

        RouteResponseDTO dto = routeMapper.toResponseDTO(route);

        assertNotNull(dto);
        assertEquals(route.getId(), dto.getId());
        assertEquals(route.getParcelId(), dto.getParcelId());
        assertEquals(route.getDriverId(), dto.getDriverId());
        assertEquals(15.5, dto.getTotalDistanceKm());
        assertEquals(30, dto.getEstimatedDurationMinutes());
        assertEquals("DIJKSTRA", dto.getRoutingService());
        assertTrue(dto.getIsActive());
        assertNotNull(dto.getPath());
        assertEquals(2, dto.getPath().size());
        assertEquals(10.0, dto.getPath().get(0).getLongitude());
    }
}
