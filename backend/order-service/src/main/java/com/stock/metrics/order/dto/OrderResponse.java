package com.stock.metrics.order.dto;

import com.stock.metrics.order.domain.OrderSide;
import com.stock.metrics.order.domain.OrderStatus;
import com.stock.metrics.order.domain.OrderType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class OrderResponse {

    private Long id;
    private String userId;
    private String symbol;
    private OrderSide side;
    private OrderType type;
    private BigDecimal price;
    private Long quantity;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private String clientOrderId;
}