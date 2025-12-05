package com.stock.metrics.order.entity;

import com.stock.metrics.order.domain.OrderSide;
import com.stock.metrics.order.domain.OrderStatus;
import com.stock.metrics.order.domain.OrderType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // For now userId is a String (can be Keycloak sub later)
    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 16)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private OrderType type;

    @Column(precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false)
    private Long quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @Column(length = 64)
    private String clientOrderId;
}