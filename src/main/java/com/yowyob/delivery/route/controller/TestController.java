package com.yowyob.delivery.route.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/ping")
    public Mono<String> ping() {
        return Mono.just("PONG - Backend is alive");
    }
}
