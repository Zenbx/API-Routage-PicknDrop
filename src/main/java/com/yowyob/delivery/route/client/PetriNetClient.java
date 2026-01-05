package com.yowyob.delivery.route.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class PetriNetClient {

    private final WebClient webClient;

    public PetriNetClient(WebClient.Builder webClientBuilder, @Value("${app.petri-net.url:http://localhost:8081}") String petriNetUrl) {
        this.webClient = webClientBuilder.baseUrl(petriNetUrl).build();
    }

    public Mono<String> initializeParcelNet(UUID parcelId) {
        NetCreationDTO request = new NetCreationDTO();
        // Define standard parcel lifecycle
        request.setPlaces(List.of("PLANNED", "PENDING_PICKUP", "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED", "FAILED", "CANCELLED", "RETURNED"));
        
        // Simplified transitions: P1 -> T1 -> P2
        request.setTransitions(List.of(
            new TransitionDTO("T_PLAN_TO_PICKUP", "Plan to Pickup"),
            new TransitionDTO("T_PICKUP_TO_IN_TRANSIT", "Pickup to In Transit"),
            new TransitionDTO("T_TRANSIT_TO_DELIVERED", "Transit to Delivered")
        ));
        
        request.setArcs(List.of(
            new ArcDTO("PLANNED", "T_PLAN_TO_PICKUP", "INPUT"),
            new ArcDTO("PENDING_PICKUP", "T_PLAN_TO_PICKUP", "OUTPUT"),
            new ArcDTO("PENDING_PICKUP", "T_PICKUP_TO_IN_TRANSIT", "INPUT"),
            new ArcDTO("IN_TRANSIT", "T_PICKUP_TO_IN_TRANSIT", "OUTPUT"),
            new ArcDTO("IN_TRANSIT", "T_TRANSIT_TO_DELIVERED", "INPUT"),
            new ArcDTO("DELIVERED", "T_TRANSIT_TO_DELIVERED", "OUTPUT")
        ));

        return webClient.post()
                .uri("/api/nets")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(netId -> log.info("Initialized Petri Net {} for parcel {}", netId, parcelId))
                .doOnError(e -> log.error("Failed to initialize Petri Net for parcel {}", parcelId, e));
    }

    @Data
    public static class NetCreationDTO {
        private List<String> places;
        private List<TransitionDTO> transitions;
        private List<ArcDTO> arcs;
    }

    @Data
    public static class TransitionDTO {
        private String id;
        private String name;
        private long minFiringDelay = 0;
        private long maxFiringDelay = 1000;
        public TransitionDTO() {}
        public TransitionDTO(String id, String name) { this.id = id; this.name = name; }
    }

    @Data
    public static class ArcDTO {
        private String placeId;
        private String transitionId;
        private String type;
        private int weight = 1;
        public ArcDTO() {}
        public ArcDTO(String placeId, String transitionId, String type) {
            this.placeId = placeId;
            this.transitionId = transitionId;
            this.type = type;
        }
    }
}
