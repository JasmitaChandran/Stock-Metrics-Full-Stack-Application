package com.stock.metrics.order.events;

import com.stock.metrics.order.domain.OrderSide;
import com.stock.metrics.order.domain.OrderStatus;
import com.stock.metrics.order.domain.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private Long orderId;
    private String userId;
    private String symbol;
    private OrderSide side;
    private OrderType type;
    private BigDecimal price;
    private Long quantity;
    private OrderStatus status;
    private Instant createdAt;
    private String clientOrderId;
}