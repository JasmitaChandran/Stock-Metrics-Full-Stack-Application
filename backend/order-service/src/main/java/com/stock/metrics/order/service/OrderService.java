package com.stock.metrics.order.service;

import com.stock.metrics.order.domain.OrderStatus;
import com.stock.metrics.order.dto.OrderRequest;
import com.stock.metrics.order.dto.OrderResponse;
import com.stock.metrics.order.entity.OrderEntity;
import com.stock.metrics.order.events.OrderEvent;
import com.stock.metrics.order.kafka.OrderEventProducer;
import com.stock.metrics.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderResponse placeOrder(OrderRequest request) {
        Instant now = Instant.now();

        OrderEntity entity = OrderEntity.builder()
                .userId(request.getUserId())
                .symbol(request.getSymbol().toUpperCase())
                .side(request.getSide())
                .type(request.getType())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .status(OrderStatus.NEW)
                .createdAt(now)
                .updatedAt(now)
                .clientOrderId(request.getClientOrderId())
                .build();

        OrderEntity saved = orderRepository.save(entity);
        log.info("Created order with id={} for user={}", saved.getId(), saved.getUserId());

        // Publish event
        OrderEvent event = OrderEvent.builder()
                .orderId(saved.getId())
                .userId(saved.getUserId())
                .symbol(saved.getSymbol())
                .side(saved.getSide())
                .type(saved.getType())
                .price(saved.getPrice())
                .quantity(saved.getQuantity())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .clientOrderId(saved.getClientOrderId())
                .build();

        orderEventProducer.publish(event);

        return toResponse(saved);
    }

    public Optional<OrderResponse> getOrder(Long id) {
        return orderRepository.findById(id).map(this::toResponse);
    }

    public List<OrderResponse> getOrdersForUser(String userId, OrderStatus status) {
        List<OrderEntity> entities;

        if (status != null) {
            entities = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        } else {
            entities = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse toResponse(OrderEntity e) {
        return OrderResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .symbol(e.getSymbol())
                .side(e.getSide())
                .type(e.getType())
                .price(e.getPrice())
                .quantity(e.getQuantity())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .clientOrderId(e.getClientOrderId())
                .build();
    }
}