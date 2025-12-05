package com.stock.metrics.pricestream.service;

import com.stock.metrics.pricestream.dto.QuoteDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
@Slf4j
public class PriceStreamPublisher {

    // Multicast sink: all active subscribers receive new events
    private final Sinks.Many<QuoteDto> sink =
            Sinks.many().multicast().onBackpressureBuffer();

    public void publish(QuoteDto quoteDto) {
        Sinks.EmitResult result = sink.tryEmitNext(quoteDto);
        if (result.isFailure()) {
            log.warn("Failed to emit quote to sink for {}: {}", quoteDto.getSymbol(), result);
        }
    }

    /**
     * Returns a stream of quotes for all symbols.
     * Consumers (controller) can filter by symbol.
     */
    @Getter
    private final Flux<QuoteDto> stream = sink.asFlux();
}