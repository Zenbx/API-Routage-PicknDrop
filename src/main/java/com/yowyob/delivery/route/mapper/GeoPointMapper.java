package com.yowyob.delivery.route.mapper;

import com.yowyob.delivery.route.controller.dto.GeoPointResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public class GeoPointMapper {

    /**
     * Convertit un String WKT en GeoPointResponseDTO
     */
    @Named("wktStringToGeoDto")
    public GeoPointResponseDTO wktStringToGeoDto(String wkt) {
        if (wkt == null || wkt.isBlank()) {
            return null;
        }

        try {
            // Parse "POINT(longitude latitude)"
            String coords = wkt.replace("POINT(", "").replace(")", "").trim();
            String[] parts = coords.split("\\s+");
            
            if (parts.length != 2) {
                return null;
            }
            
            double longitude = Double.parseDouble(parts[0]);
            double latitude = Double.parseDouble(parts[1]);
            
            return GeoPointResponseDTO.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}