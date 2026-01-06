package com.yowyob.delivery.route.controller;

import com.yowyob.delivery.route.controller.dto.RouteCalculationRequestDTO;
import com.yowyob.delivery.route.controller.dto.RouteResponseDTO;
import com.yowyob.delivery.route.service.RouteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(DeliveryController.class)
class DeliveryControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RouteService routeService;

    @Test
    void shouldCreateDelivery() {
        RouteCalculationRequestDTO request = new RouteCalculationRequestDTO();
        request.setParcelId(UUID.randomUUID());
        request.setStartHubId(UUID.randomUUID());
        request.setEndHubId(UUID.randomUUID());
        request.setDriverId(UUID.randomUUID());

        RouteResponseDTO responseDTO = new RouteResponseDTO();
        responseDTO.setId(UUID.randomUUID());
        responseDTO.setTotalDistanceKm(15.2);

        when(routeService.calculateRoute(any())).thenReturn(Mono.just(responseDTO));

        webTestClient.post()
                .uri("/api/v1/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.totalDistanceKm").isEqualTo(15.2);
    }

    @Test
    void shouldGetDeliveryById() {
        UUID id = UUID.randomUUID();
        RouteResponseDTO responseDTO = new RouteResponseDTO();
        responseDTO.setId(id);

        when(routeService.getRoute(id)).thenReturn(Mono.just(responseDTO));

        webTestClient.get()
                .uri("/api/v1/deliveries/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void shouldGetTracking() {
        UUID id = UUID.randomUUID();
        RouteResponseDTO responseDTO = new RouteResponseDTO();
        responseDTO.setId(id);

        when(routeService.getRoute(id)).thenReturn(Mono.just(responseDTO));

        webTestClient.get()
                .uri("/api/v1/deliveries/{id}/tracking", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString());
    }
}
