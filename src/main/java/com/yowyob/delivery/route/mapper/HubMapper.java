package com.yowyob.delivery.route.mapper;

import com.yowyob.delivery.route.controller.dto.GeoPointResponseDTO;
import com.yowyob.delivery.route.domain.entity.Hub;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface HubMapper {

    @Mapping(target = "latitude", source = "location", qualifiedByName = "wktToLatitude")
    @Mapping(target = "longitude", source = "location", qualifiedByName = "wktToLongitude")
    GeoPointResponseDTO toResponseDTO(Hub hub);

    @Named("wktToGeoPoint")
    default GeoPointResponseDTO wktToGeoPoint(String wkt) {
        if (wkt == null)
            return null;
        return toResponseDTO(Hub.builder().location(wkt).build());
    }

    @Named("wktToGeoPointList")
    default java.util.List<GeoPointResponseDTO> wktToGeoPointList(String wkt) {
        if (wkt == null || wkt.isEmpty())
            return java.util.Collections.emptyList();
        try {
            org.locationtech.jts.geom.Geometry geometry = new WKTReader().read(wkt);
            if (geometry instanceof org.locationtech.jts.geom.LineString) {
                org.locationtech.jts.geom.LineString lineString = (org.locationtech.jts.geom.LineString) geometry;
                return java.util.Arrays.stream(lineString.getCoordinates())
                        .map(c -> {
                            GeoPointResponseDTO dto = new GeoPointResponseDTO();
                            dto.setLongitude(c.x);
                            dto.setLatitude(c.y);
                            return dto;
                        })
                        .collect(java.util.stream.Collectors.toList());
            }
        } catch (Exception e) {
            // Log error
        }
        return java.util.Collections.emptyList();
    }

    @Named("wktToLatitude")
    default Double wktToLatitude(String wkt) {
        Point point = wktToPoint(wkt);
        return point != null ? point.getY() : null;
    }

    @Named("wktToLongitude")
    default Double wktToLongitude(String wkt) {
        Point point = wktToPoint(wkt);
        return point != null ? point.getX() : null;
    }

    default Point wktToPoint(String wkt) {
        if (wkt == null || wkt.isEmpty())
            return null;
        try {
            return (Point) new WKTReader().read(wkt);
        } catch (Exception e) {
            return null;
        }
    }

    default String toWkt(Double latitude, Double longitude) {
        if (latitude == null || longitude == null)
            return null;
        return String.format(java.util.Locale.US, "POINT(%f %f)", longitude, latitude);
    }
}
