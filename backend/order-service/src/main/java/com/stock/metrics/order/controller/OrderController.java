package com.stock.metrics.order.controller;

import com.stock.metrics.order.domain.OrderStatus;
import com.stock.metrics.order.dto.OrderRequest;
import com.stock.metrics.order.dto.OrderResponse;
import com.stock.metrics.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Place a new order.
     *
     * POST /api/orders
     *
     * Example JSON:
     * {
     *   "userId": "user-123",
     *   "symbol": "MSFT",
     *   "side": "BUY",
     *   "type": "LIMIT",
     *   "price": 500.00,
     *   "quantity": 10,
     *   "clientOrderId": "client-1"
     * }
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request) {
        OrderResponse response = orderService.placeOrder(request);
        return ResponseEntity
                .created(URI.create("/api/orders/" + response.getId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return orderService.getOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get orders for a user, optionally filtered by status.
     *
     * GET /api/orders/users/{userId}?status=OPEN
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersForUser(
            @PathVariable String userId,
            @RequestParam(required = false) OrderStatus status
    ) {
        return ResponseEntity.ok(orderService.getOrdersForUser(userId, status));
    }
}