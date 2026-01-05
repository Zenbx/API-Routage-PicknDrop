package com.yowyob.delivery.route.service.impl;

import com.yowyob.delivery.route.client.PetriNetClient;
import com.yowyob.delivery.route.controller.dto.ParcelRequestDTO;
import com.yowyob.delivery.route.controller.dto.ParcelResponseDTO;
import com.yowyob.delivery.route.domain.entity.Parcel;
import com.yowyob.delivery.route.domain.enums.ParcelState;
import com.yowyob.delivery.route.mapper.ParcelMapper;
import com.yowyob.delivery.route.repository.ParcelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParcelServiceImplTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private ParcelMapper parcelMapper;

    @Mock
    private PetriNetClient petriNetClient;

    @InjectMocks
    private ParcelServiceImpl parcelService;

    @Test
    void shouldCreateParcel() {
        ParcelRequestDTO request = ParcelRequestDTO.builder()
                .senderName("John")
                .recipientName("Doe")
                .weightKg(10.0)
                .build();

        Parcel parcel = Parcel.builder().build();
        Parcel savedParcel = Parcel.builder()
                .id(UUID.randomUUID())
                .trackingCode("TRK-XYZ")
                .currentState(ParcelState.PLANNED)
                .build();
        ParcelResponseDTO responseDTO = ParcelResponseDTO.builder()
                .id(savedParcel.getId())
                .trackingCode("TRK-XYZ")
                .currentState("PLANIFIE")
                .build();

        when(parcelMapper.toEntity(request)).thenReturn(parcel);
        when(parcelRepository.saveWithGeometry(any(Parcel.class))).thenReturn(Mono.just(savedParcel));
        when(petriNetClient.initializeParcelNet(savedParcel.getId())).thenReturn(Mono.just("net-123"));
        when(parcelMapper.toResponseDTO(savedParcel)).thenReturn(responseDTO);

        Mono<ParcelResponseDTO> result = parcelService.createParcel(request);

        StepVerifier.create(result)
                .assertNext(res -> {
                    assertNotNull(res.getId());
                    assertEquals("TRK-XYZ", res.getTrackingCode());
                })
                .verifyComplete();

        verify(parcelRepository)
                .saveWithGeometry(argThat(p -> p.getTrackingCode() != null && p.getTrackingCode().startsWith("TRK-") &&
                        p.getCurrentState() == ParcelState.PLANNED));
        verify(petriNetClient).initializeParcelNet(savedParcel.getId());
    }

    @Test
    void shouldGetParcelById() {
        UUID id = UUID.randomUUID();
        Parcel parcel = Parcel.builder().id(id).build();
        ParcelResponseDTO responseDTO = ParcelResponseDTO.builder().id(id).build();

        when(parcelRepository.findById(id)).thenReturn(Mono.just(parcel));
        when(parcelMapper.toResponseDTO(parcel)).thenReturn(responseDTO);

        Mono<ParcelResponseDTO> result = parcelService.getParcel(id);

        StepVerifier.create(result)
                .expectNext(responseDTO)
                .verifyComplete();
    }

    @Test
    void shouldGetAllParcels() {
        Parcel p1 = Parcel.builder().id(UUID.randomUUID()).build();
        Parcel p2 = Parcel.builder().id(UUID.randomUUID()).build();
        ParcelResponseDTO r1 = ParcelResponseDTO.builder().id(p1.getId()).build();
        ParcelResponseDTO r2 = ParcelResponseDTO.builder().id(p2.getId()).build();

        when(parcelRepository.findAll()).thenReturn(Flux.just(p1, p2));
        when(parcelMapper.toResponseDTO(p1)).thenReturn(r1);
        when(parcelMapper.toResponseDTO(p2)).thenReturn(r2);

        Flux<ParcelResponseDTO> result = parcelService.getAllParcels();

        StepVerifier.create(result)
                .expectNext(r1)
                .expectNext(r2)
                .verifyComplete();
    }
}
