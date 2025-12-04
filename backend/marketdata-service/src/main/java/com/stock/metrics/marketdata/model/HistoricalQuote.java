package com.stock.metrics.marketdata.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "historical_quotes")
public class HistoricalQuote {

    @Id
    private String id;

    private String symbol;

    private Instant timestamp;

    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;

    /**
     * e.g. "1d", "1h", "1m"
     */
    private String interval;
}
