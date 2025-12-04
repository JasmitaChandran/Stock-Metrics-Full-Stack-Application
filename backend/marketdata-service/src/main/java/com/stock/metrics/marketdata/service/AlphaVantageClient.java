package com.stock.metrics.marketdata.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.metrics.marketdata.model.HistoricalQuote;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlphaVantageClient {

    private final WebClient alphaVantageWebClient;
    private final ObjectMapper objectMapper;


    @Value("${external.marketdata.alpha.api-key}")
    private String apiKey;

    // This method is automatically called after the bean is created
    @PostConstruct
    public void verifyApiKeyLoaded() {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("YOUR_ALPHA_VANTAGE_API_KEY_HERE")) {
            log.warn("❗ Alpha Vantage API key is NOT loaded or still default placeholder.");
        } else {
            log.info("✅ Alpha Vantage API key loaded successfully in AlphaVantageClient.");
        }
    }

    /**
     * Fetch daily historical data for a symbol.
     * Uses Alpha Vantage TIME_SERIES_DAILY_ADJUSTED endpoint.
     */
    public Flux<HistoricalQuote> fetchDailyHistory(String symbol) {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("YOUR_ALPHA_VANTAGE_API_KEY_HERE")) {
            log.warn("Alpha Vantage API key is not set. Returning empty history for symbol {}", symbol);
            return Flux.empty();
        }

        return alphaVantageWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/query")
                        .queryParam("function", "TIME_SERIES_DAILY")
                        .queryParam("symbol", symbol)
                        .queryParam("outputsize", "compact")
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(body -> {
                    // 🔍 ADD THIS:
                    log.info("Alpha Vantage raw response for {}: {}", symbol, body);

                    try {
                        JsonNode root = objectMapper.readTree(body);
                        JsonNode timeSeries = root.get("Time Series (Daily)");
                        if (timeSeries == null) {
                            log.warn("Alpha Vantage response missing Time Series (Daily) for symbol {}: {}", symbol, body);
                            return Flux.empty();
                        }

                        Iterator<Map.Entry<String, JsonNode>> fields = timeSeries.fields();

                        return Flux.fromIterable(() -> fields)
                                .map(entry -> {
                                    String dateStr = entry.getKey();
                                    JsonNode data = entry.getValue();

                                    LocalDate localDate = LocalDate.parse(dateStr);
                                    Instant ts = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);

                                    BigDecimal open = new BigDecimal(data.get("1. open").asText());
                                    BigDecimal high = new BigDecimal(data.get("2. high").asText());
                                    BigDecimal low = new BigDecimal(data.get("3. low").asText());
                                    BigDecimal close = new BigDecimal(data.get("4. close").asText());
                                    long volume = data.get("5. volume").asLong();

                                    return HistoricalQuote.builder()
                                            .symbol(symbol.toUpperCase())
                                            .timestamp(ts)
                                            .open(open)
                                            .high(high)
                                            .low(low)
                                            .close(close)
                                            .volume(volume)
                                            .interval("1d")
                                            .build();
                                });
                    } catch (Exception e) {
                        log.error("Failed to parse Alpha Vantage response for symbol {}: {}", symbol, e.getMessage());
                        return Flux.empty();
                    }
                });
    }

}
