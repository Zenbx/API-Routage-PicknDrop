package com.yowyob.delivery.route.mapper;

import com.yowyob.delivery.route.controller.dto.RouteResponseDTO;
import com.yowyob.delivery.route.domain.entity.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for Route conversion.
 */
@Mapper(componentModel = "spring", uses = { HubMapper.class })
public interface RouteMapper {

    @Mapping(target = "path", source = "routeGeometry", qualifiedByName = "wktToGeoPointList")
    RouteResponseDTO toResponseDTO(Route route);
}
