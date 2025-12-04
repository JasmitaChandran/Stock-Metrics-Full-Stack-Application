package com.stock.metrics.marketdata.repository;

import com.stock.metrics.marketdata.model.HistoricalQuote;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface HistoricalQuoteRepository extends ReactiveMongoRepository<HistoricalQuote, String> {

    Flux<HistoricalQuote> findBySymbolAndTimestampBetweenOrderByTimestampAsc(
            String symbol,
            Instant start,
            Instant end
    );

    Flux<HistoricalQuote> findBySymbolOrderByTimestampAsc(String symbol);
}
