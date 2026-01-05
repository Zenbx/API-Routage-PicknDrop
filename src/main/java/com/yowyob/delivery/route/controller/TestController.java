package com.yowyob.delivery.route.controller;

import com.yowyob.delivery.route.domain.entity.HubConnection;
import com.yowyob.delivery.route.repository.HubConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final HubConnectionRepository hubConnectionRepository;

    @GetMapping("/ping")
    public Mono<String> ping() {
        return Mono.just("PONG - Backend is alive");
    }

    @GetMapping("/hub-connections")
    public Flux<HubConnection> getAllConnections() {
        return hubConnectionRepository.findAll();
    }
}
