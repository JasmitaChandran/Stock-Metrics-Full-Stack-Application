package com.stock.metrics.pricestream.controller;

import com.stock.metrics.pricestream.dto.QuoteDto;
import com.stock.metrics.pricestream.service.PriceStreamPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/stream/prices")
@RequiredArgsConstructor
public class PriceStreamController {

    private final PriceStreamPublisher priceStreamPublisher;

    @GetMapping(value = "/{symbol}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<QuoteDto>> streamPrices(@PathVariable String symbol) {
        String upperSymbol = symbol.toUpperCase();

        return priceStreamPublisher.getStream()
                .filter(quote -> quote.getSymbol().equalsIgnoreCase(upperSymbol))
                .map(quote ->
                        ServerSentEvent.<QuoteDto>builder()
                                .event("price")
                                .id(quote.getTimestamp().toString())
                                .data(quote)
                                .build()
                );
    }
}
