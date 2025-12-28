package com.yowyob.delivery.route.service.impl;

import com.yowyob.delivery.route.controller.dto.GeoPointRequestDTO;
import com.yowyob.delivery.route.controller.dto.GeoPointResponseDTO;
import com.yowyob.delivery.route.domain.entity.Hub;
import com.yowyob.delivery.route.domain.enums.HubType;

import com.yowyob.delivery.route.repository.HubRepository;
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
class HubServiceImplTest {

        @Mock
        private HubRepository hubRepository;



        @InjectMocks
        private HubServiceImpl hubService;

        @Test
        void shouldCreateHub() {
                GeoPointRequestDTO request = GeoPointRequestDTO.builder()
                                .address("Test Address")
                                .type("WAREHOUSE")
                                .latitude(48.0)
                                .longitude(2.0)
                                .build();

                Hub hub = Hub.builder().build();
                Hub savedHub = Hub.builder().id(UUID.randomUUID()).address("Test Address").type(HubType.WAREHOUSE)
                                .location("POINT(2 48)").build();
                GeoPointResponseDTO responseDTO = GeoPointResponseDTO.builder().address("Test Address").build();


                when(hubRepository.saveWithGeometry(any(Hub.class))).thenReturn(Mono.just(savedHub));
                // when(hubMapper.toResponseDTO(savedHub)).thenReturn(responseDTO); // Removing
                // mapper mock as it's private method in service

                Mono<GeoPointResponseDTO> result = hubService.createHub(request);

                StepVerifier.create(result)
                                .expectNextMatches(dto -> "Test Address".equals(dto.getAddress()))
                                .verifyComplete();

                verify(hubRepository).saveWithGeometry(argThat(h -> "Test Address".equals(h.getAddress()) &&
                                HubType.WAREHOUSE == h.getType() &&
                                "POINT(2.000000 48.000000)".equals(h.getLocation())));
        }

        @Test
        void shouldGetHubById() {
                UUID id = UUID.randomUUID();
                Hub hub = Hub.builder().id(id).address("Add").location("POINT(10 20)").type(HubType.WAREHOUSE).build();
                GeoPointResponseDTO responseDTO = GeoPointResponseDTO.builder().id(id).build();

                when(hubRepository.findByIdWithLocation(id)).thenReturn(Mono.just(hub));
                // when(hubMapper.toResponseDTO(hub)).thenReturn(responseDTO);

                Mono<GeoPointResponseDTO> result = hubService.getHub(id);

                StepVerifier.create(result)
                                .expectNextMatches(dto -> dto.getId().equals(id))
                                .verifyComplete();
        }

        @Test
        void shouldGetAllHubs() {
                Hub h1 = Hub.builder().id(UUID.randomUUID()).location("POINT(0 0)").type(HubType.WAREHOUSE).build();
                Hub h2 = Hub.builder().id(UUID.randomUUID()).location("POINT(0 0)").type(HubType.WAREHOUSE).build();

                when(hubRepository.findAllWithLocation()).thenReturn(Flux.just(h1, h2));
                // when(hubMapper.toResponseDTO(h1)).thenReturn(r1);
                // when(hubMapper.toResponseDTO(h2)).thenReturn(r2);

                Flux<GeoPointResponseDTO> result = hubService.getAllHubs();

                StepVerifier.create(result)
                                .expectNextMatches(dto -> dto.getId().equals(h1.getId()))
                                .expectNextMatches(dto -> dto.getId().equals(h2.getId()))
                                .verifyComplete();
        }
}
