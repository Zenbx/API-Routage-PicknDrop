package com.yowyob.delivery.route.controller;

import com.yowyob.delivery.route.controller.dto.ParcelRequestDTO;
import com.yowyob.delivery.route.controller.dto.ParcelResponseDTO;
import com.yowyob.delivery.route.service.ParcelService;
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

@WebFluxTest(ParcelController.class)
class ParcelControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ParcelService parcelService;

    @Test
    void shouldCreateParcel() {
        ParcelRequestDTO request = new ParcelRequestDTO();
        request.setSenderName("Jeff Belekotan");
        request.setSenderPhone("+237 600000000");
        request.setRecipientName("Farelle Ngapgou");
        request.setRecipientPhone("+237 611111111");
        request.setPickupLocation("POINT(9.7 4.0)");
        request.setDeliveryLocation("POINT(11.5 3.8)");
        request.setWeightKg(10.5);

        ParcelResponseDTO responseDTO = new ParcelResponseDTO();
        responseDTO.setId(UUID.randomUUID());
        responseDTO.setTrackingCode("TRK-12345678");

        when(parcelService.createParcel(any())).thenReturn(Mono.just(responseDTO));

        webTestClient.post()
                .uri("/api/v1/parcels")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.trackingCode").isEqualTo("TRK-12345678");
    }

    @Test
    void shouldGetParcelById() {
        UUID id = UUID.randomUUID();
        ParcelResponseDTO responseDTO = new ParcelResponseDTO();
        responseDTO.setId(id);
        responseDTO.setTrackingCode("TRK-87654321");

        when(parcelService.getParcel(id)).thenReturn(Mono.just(responseDTO));

        webTestClient.get()
                .uri("/api/v1/parcels/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString())
                .jsonPath("$.trackingCode").isEqualTo("TRK-87654321");
    }

    @Test
    void shouldListAllParcels() {
        ParcelResponseDTO p1 = new ParcelResponseDTO();
        p1.setTrackingCode("P1");
        ParcelResponseDTO p2 = new ParcelResponseDTO();
        p2.setTrackingCode("P2");

        when(parcelService.getAllParcels()).thenReturn(Flux.just(p1, p2));

        webTestClient.get()
                .uri("/api/v1/parcels")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ParcelResponseDTO.class)
                .hasSize(2);
    }
}
