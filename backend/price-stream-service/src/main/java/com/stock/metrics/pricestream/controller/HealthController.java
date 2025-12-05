package com.stock.metrics.pricestream.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HealthController {

    @GetMapping(value = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> ping() {
        return Mono.just("{\"service\":\"price-stream-service\",\"status\":\"ok\"}");
    }
}