package com.stock.metrics.marketdata.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.metrics.marketdata.dto.QuoteDto;
import com.stock.metrics.marketdata.model.HistoricalQuote;
import com.stock.metrics.marketdata.repository.HistoricalQuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final HistoricalQuoteRepository historicalQuoteRepository;
    private final AlphaVantageClient alphaVantageClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;


    @Value("${app.kafka.topics.prices}")
    private String pricesTopic;

    /**
     * Load daily history for a symbol from external API, save to Mongo, and publish price events to Kafka.
     */
    public Flux<QuoteDto> refreshDailyHistory(String symbol) {
        return alphaVantageClient.fetchDailyHistory(symbol)
                .flatMap(historicalQuoteRepository::save)
                .map(this::toDto)
                .doOnNext(dto -> publishPriceEvent(dto));
    }

    /**
     * Read history from Mongo.
     */
    public Flux<QuoteDto> getHistory(String symbol, Instant start, Instant end) {
        if (start != null && end != null) {
            return historicalQuoteRepository
                    .findBySymbolAndTimestampBetweenOrderByTimestampAsc(symbol.toUpperCase(), start, end)
                    .map(this::toDto);
        } else {
            return historicalQuoteRepository
                    .findBySymbolOrderByTimestampAsc(symbol.toUpperCase())
                    .map(this::toDto);
        }
    }

    /**
     * Latest quote = most recent timestamp in Mongo.
     */
    public Mono<QuoteDto> getLatestQuote(String symbol) {
        return historicalQuoteRepository.findBySymbolOrderByTimestampAsc(symbol.toUpperCase())
                .sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .last()
                .map(this::toDto);
    }

    private QuoteDto toDto(HistoricalQuote quote) {
        return QuoteDto.builder()
                .symbol(quote.getSymbol())
                .timestamp(quote.getTimestamp())
                .open(quote.getOpen())
                .high(quote.getHigh())
                .low(quote.getLow())
                .close(quote.getClose())
                .volume(quote.getVolume())
                .interval(quote.getInterval())
                .build();
    }

    private void publishPriceEvent(QuoteDto dto) {
        try {
            String payload = objectMapper.writeValueAsString(dto);
            kafkaTemplate.send(pricesTopic, dto.getSymbol(), payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish price event for {}: {}", dto.getSymbol(), ex.getMessage());
                        } else {
                            log.debug("Published price event for {} to topic {}", dto.getSymbol(), pricesTopic);
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize price event for {}: {}", dto.getSymbol(), e.getMessage());
        }
    }
}
