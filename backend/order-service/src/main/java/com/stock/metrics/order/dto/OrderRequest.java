package com.stock.metrics.order.dto;

import com.stock.metrics.order.domain.OrderSide;
import com.stock.metrics.order.domain.OrderType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {

    private String userId;
    private String symbol;
    private OrderSide side;
    private OrderType type;
    private BigDecimal price;   // optional for MARKET
    private Long quantity;
    private String clientOrderId;  // optional
}
