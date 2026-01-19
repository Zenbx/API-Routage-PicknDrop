package com.yowyob.delivery.route.mapper;

import com.yowyob.delivery.route.controller.dto.ParcelRequestDTO;
import com.yowyob.delivery.route.controller.dto.ParcelResponseDTO;
import com.yowyob.delivery.route.domain.entity.Parcel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { GeoPointMapper.class } // ← Gardez juste GeoPointMapper
)
public interface ParcelMapper {

    // Entity → Response DTO
    @Mapping(target = "pickupLocation", source = "pickupLocation", qualifiedByName = "wktStringToGeoDto" // ← NOUVEAU
                                                                                                         // NOM
    )
    @Mapping(target = "deliveryLocation", source = "deliveryLocation", qualifiedByName = "wktStringToGeoDto" // ←
                                                                                                             // NOUVEAU
                                                                                                             // NOM
    )
    @Mapping(target = "priority", expression = "java(parcel.getPriority().name())")
    @Mapping(target = "petriNetId", source = "petriNetId")
    ParcelResponseDTO toResponseDTO(Parcel parcel);

    // Request DTO → Entity
    @Mapping(target = "pickupLocation", source = "pickupLocation") // ← String → String, pas de conversion
    @Mapping(target = "deliveryLocation", source = "deliveryLocation")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trackingCode", ignore = true)
    @Mapping(target = "currentState", ignore = true)
    @Mapping(target = "driverId", ignore = true)
    @Mapping(target = "vehicleId", ignore = true)
    @Mapping(target = "distanceKm", ignore = true)
    @Mapping(target = "deliveryFeeXaf", ignore = true)
    @Mapping(target = "estimatedDeliveryTime", ignore = true)
    @Mapping(target = "actualDeliveryTime", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "priority", ignore = true)
    Parcel toEntity(ParcelRequestDTO request);
}