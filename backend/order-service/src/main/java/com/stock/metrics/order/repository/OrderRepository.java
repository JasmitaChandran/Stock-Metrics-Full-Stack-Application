package com.stock.metrics.order.repository;

import com.stock.metrics.order.entity.OrderEntity;
import com.stock.metrics.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    List<OrderEntity> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, OrderStatus status);
}