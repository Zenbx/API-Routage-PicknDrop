package com.yowyob.delivery.route.mapper;

import com.yowyob.delivery.route.controller.dto.GeoPointResponseDTO;
import com.yowyob.delivery.route.domain.entity.Hub;
import com.yowyob.delivery.route.domain.enums.HubType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HubMapperTest {

    private HubMapper hubMapper;

    @BeforeEach
    void setUp() {
        hubMapper = Mappers.getMapper(HubMapper.class);
    }

    @Test
    void shouldMapHubToGeoPointResponseDTO() {
        Hub hub = Hub.builder()
                .address("Test Address")
                .type(HubType.WAREHOUSE)
                .location("POINT(10.5 45.2)")
                .build();

        GeoPointResponseDTO dto = hubMapper.toResponseDTO(hub);

        assertNotNull(dto);
        assertEquals("Test Address", dto.getAddress());
        assertEquals("WAREHOUSE", dto.getType());
        assertEquals(45.2, dto.getLatitude());
        assertEquals(10.5, dto.getLongitude());
    }

    @Test
    void shouldMapWktToGeoPoint() {
        String wkt = "POINT(10.5 45.2)";
        GeoPointResponseDTO dto = hubMapper.wktToGeoPoint(wkt);

        assertNotNull(dto);
        assertEquals(45.2, dto.getLatitude());
        assertEquals(10.5, dto.getLongitude());
    }

    @Test
    void shouldMapWktToGeoPointList() {
        String wkt = "LINESTRING(10 10, 20 20, 30 30)";
        List<GeoPointResponseDTO> points = hubMapper.wktToGeoPointList(wkt);

        assertNotNull(points);
        assertEquals(3, points.size());
        assertEquals(10.0, points.get(0).getLongitude());
        assertEquals(10.0, points.get(0).getLatitude());
        assertEquals(30.0, points.get(2).getLongitude());
        assertEquals(30.0, points.get(2).getLatitude());
    }

    @Test
    void shouldReturnEmptyListForInvalidWktInListMapping() {
        String wkt = "POINT(10 10)";
        List<GeoPointResponseDTO> points = hubMapper.wktToGeoPointList(wkt);

        assertNotNull(points);
        assertTrue(points.isEmpty());
    }

    @Test
    void shouldReturnLatitudeAndLongitudeFromWkt() {
        String wkt = "POINT(10.5 45.2)";
        assertEquals(45.2, hubMapper.wktToLatitude(wkt));
        assertEquals(10.5, hubMapper.wktToLongitude(wkt));
    }

    @Test
    void shouldCreateWktFromLatLong() {
        String wkt = hubMapper.toWkt(45.2, 10.5);
        assertEquals("POINT(10.500000 45.200000)", wkt);
    }
}
