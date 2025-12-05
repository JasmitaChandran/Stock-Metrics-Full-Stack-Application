package com.stock.metrics.pricestream.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.metrics.pricestream.dto.QuoteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final PriceStreamPublisher priceStreamPublisher;

    @Value("${app.redis.quote-latest-prefix}")
    private String quoteLatestPrefix;

    @KafkaListener(
            topics = "${app.kafka.topics.prices}",
            groupId = "price-stream-group"
    )
    public void handlePriceEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key
    ) {
        try {
            QuoteDto quote = objectMapper.readValue(message, QuoteDto.class);
            String symbol = quote.getSymbol();
            log.info("✅ Received price event from Kafka for {} at {} close={}",
                    symbol, quote.getTimestamp(), quote.getClose());

            String redisKey = quoteLatestPrefix + symbol.toUpperCase();
            redisTemplate.opsForValue()
                    .set(redisKey, message)
                    .doOnError(err -> log.error("Failed to store quote for {} in Redis: {}", symbol, err.getMessage()))
                    .subscribe();

            priceStreamPublisher.publish(quote);

        } catch (Exception e) {
            log.error("❌ Failed to process price event from Kafka. Key={}, message={}, error={}",
                    key, message, e.getMessage());
        }
    }
}
