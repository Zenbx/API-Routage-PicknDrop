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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(RouteController.class)
class RouteControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RouteService routeService;

    @Test
    void shouldCalculateRoute() {
        RouteCalculationRequestDTO request = new RouteCalculationRequestDTO();
        request.setParcelId(UUID.randomUUID());
        request.setStartHubId(UUID.randomUUID());
        request.setEndHubId(UUID.randomUUID());
        request.setDriverId(UUID.randomUUID());

        RouteResponseDTO responseDTO = new RouteResponseDTO();
        responseDTO.setTotalDistanceKm(20.0);

        when(routeService.calculateRoute(any())).thenReturn(Mono.just(responseDTO));

        webTestClient.post()
                .uri("/api/v1/routes/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.totalDistanceKm").isEqualTo(20.0);
    }

    @Test
    void shouldGetRouteById() {
        UUID id = UUID.randomUUID();
        RouteResponseDTO responseDTO = new RouteResponseDTO();
        responseDTO.setId(id);

        when(routeService.getRoute(id)).thenReturn(Mono.just(responseDTO));

        webTestClient.get()
                .uri("/api/v1/routes/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void shouldRecalculateRoute() {
        UUID id = UUID.randomUUID();
        RouteResponseDTO responseDTO = new RouteResponseDTO();
        responseDTO.setId(id);

        when(routeService.recalculateRoute(eq(id), any())).thenReturn(Mono.just(responseDTO));

        webTestClient.post()
                .uri("/api/v1/routes/{id}/recalculate", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void shouldReturn422WhenNoPathFound() {
        RouteCalculationRequestDTO request = new RouteCalculationRequestDTO();
        request.setParcelId(UUID.randomUUID());
        request.setStartHubId(UUID.randomUUID());
        request.setEndHubId(UUID.randomUUID());
        request.setDriverId(UUID.randomUUID());

        when(routeService.calculateRoute(any())).thenReturn(Mono.error(new com.yowyob.delivery.route.controller.exception.NoPathFoundException("No path found")));

        webTestClient.post()
                .uri("/api/v1/routes/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Unprocessable Entity");
    }
}
