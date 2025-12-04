package com.stock.metrics.marketdata.controller;

import com.stock.metrics.marketdata.dto.QuoteDto;
import com.stock.metrics.marketdata.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
@RequestMapping("/api/marketdata")
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketDataService marketDataService;

    @GetMapping(value = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> ping() {
        return Mono.just("{\"service\":\"marketdata-service\",\"status\":\"ok\"}");
    }

    /**
     * Trigger refresh from external API and stream back saved quotes.
     * Example: POST /api/marketdata/refresh/AAPL
     */
    @PostMapping(value = "/refresh/{symbol}", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<QuoteDto> refreshHistory(@PathVariable String symbol) {
        return marketDataService.refreshDailyHistory(symbol);
    }

    /**
     * Get historical data from Mongo.
     * Example:
     * GET /api/marketdata/AAPL/history?start=2024-01-01T00:00:00Z&end=2024-02-01T00:00:00Z
     */
    @GetMapping(value = "/{symbol}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<QuoteDto> getHistory(
            @PathVariable String symbol,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ) {
        return marketDataService.getHistory(symbol, start, end);
    }

    /**
     * Get latest quote from Mongo.
     * Example:
     * GET /api/marketdata/AAPL/quote
     */
    @GetMapping(value = "/{symbol}/quote", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<QuoteDto> getLatestQuote(@PathVariable String symbol) {
        return marketDataService.getLatestQuote(symbol);
    }
}
