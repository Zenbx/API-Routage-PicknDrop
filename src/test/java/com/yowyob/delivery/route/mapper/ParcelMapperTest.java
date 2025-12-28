package com.yowyob.delivery.route.mapper;

import com.yowyob.delivery.route.controller.dto.GeoPointResponseDTO;
import com.yowyob.delivery.route.controller.dto.ParcelRequestDTO;
import com.yowyob.delivery.route.controller.dto.ParcelResponseDTO;
import com.yowyob.delivery.route.domain.entity.Parcel;
import com.yowyob.delivery.route.domain.enums.ParcelState;
import com.yowyob.delivery.route.mapper.GeoPointMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParcelMapperTest {

    private ParcelMapper parcelMapper;

    @Spy
    private GeoPointMapper geoPointMapper = Mappers.getMapper(GeoPointMapper.class);

    @BeforeEach
    void setUp() {
        parcelMapper = Mappers.getMapper(ParcelMapper.class);
        // Manually inject the spy/mock of GeoPointMapper into the generated
        // ParcelMapperImpl
        ReflectionTestUtils.setField(parcelMapper, "geoPointMapper", geoPointMapper);
    }

    @Test
    void shouldMapParcelToResponseDTO() {
        UUID id = UUID.randomUUID();
        Parcel parcel = Parcel.builder()
                .id(id)
                .trackingCode("TRK-123")
                .currentState(ParcelState.IN_TRANSIT)
                .pickupLocation("POINT(10 10)")
                .deliveryLocation("POINT(20 20)")
                .weightKg(5.5)
                .priority(com.yowyob.delivery.route.domain.enums.ParcelPriority.NORMAL)
                .build();

        ParcelResponseDTO dto = parcelMapper.toResponseDTO(parcel);

        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("TRK-123", dto.getTrackingCode());
        assertEquals("IN_TRANSIT", dto.getCurrentState());
        assertNotNull(dto.getPickupLocation());
        assertEquals(10.0, dto.getPickupLocation().getLongitude());
        assertNotNull(dto.getDeliveryLocation());
        assertEquals(20.0, dto.getDeliveryLocation().getLongitude());
    }

    @Test
    void shouldMapRequestDTOToEntity() {
        ParcelRequestDTO request = ParcelRequestDTO.builder()
                .senderName("John")
                .recipientName("Doe")
                .pickupLocation("POINT(10 10)")
                .deliveryLocation("POINT(20 20)")
                .weightKg(10.0)
                .build();

        Parcel parcel = parcelMapper.toEntity(request);

        assertNotNull(parcel);
        assertEquals("John", parcel.getSenderName());
        assertEquals("Doe", parcel.getRecipientName());
        assertEquals("POINT(10 10)", parcel.getPickupLocation());
        assertEquals("POINT(20 20)", parcel.getDeliveryLocation());
        assertEquals(10.0, parcel.getWeightKg());
    }
}
