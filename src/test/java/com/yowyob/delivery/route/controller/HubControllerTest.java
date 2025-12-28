package com.yowyob.delivery.route.controller;

import com.yowyob.delivery.route.controller.dto.GeoPointRequestDTO;
import com.yowyob.delivery.route.controller.dto.GeoPointResponseDTO;
import com.yowyob.delivery.route.service.HubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(HubController.class)
class HubControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private HubService hubService;

    @Test
    void shouldCreateHub() {
        GeoPointRequestDTO request = new GeoPointRequestDTO();
        request.setAddress("Yaounde Centre");
        request.setLatitude(3.848);
        request.setLongitude(11.502);
        request.setType("WAREHOUSE");

        GeoPointResponseDTO responseDTO = new GeoPointResponseDTO();
        responseDTO.setId(UUID.randomUUID());
        responseDTO.setAddress("Yaounde Centre");

        when(hubService.createHub(any())).thenReturn(Mono.just(responseDTO));

        webTestClient.post()
                .uri("/api/v1/hubs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.address").isEqualTo("Yaounde Centre");
    }

    @Test
    void shouldGetHubById() {
        UUID id = UUID.randomUUID();
        GeoPointResponseDTO responseDTO = new GeoPointResponseDTO();
        responseDTO.setId(id);
        responseDTO.setAddress("Douala Port");

        when(hubService.getHub(id)).thenReturn(Mono.just(responseDTO));

        webTestClient.get()
                .uri("/api/v1/hubs/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString())
                .jsonPath("$.address").isEqualTo("Douala Port");
    }

    @Test
    void shouldListAllHubs() {
        GeoPointResponseDTO h1 = new GeoPointResponseDTO();
        h1.setAddress("Hub 1");
        GeoPointResponseDTO h2 = new GeoPointResponseDTO();
        h2.setAddress("Hub 2");

        when(hubService.getAllHubs()).thenReturn(Flux.just(h1, h2));

        webTestClient.get()
                .uri("/api/v1/hubs")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(GeoPointResponseDTO.class)
                .hasSize(2);
    }
}
