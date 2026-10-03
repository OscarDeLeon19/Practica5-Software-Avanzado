package com.poc.practica5.order.dto;

import com.poc.practica5.order.entity.Order;
import com.poc.practica5.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String customerId,
        String productId,
        Integer quantity,
        BigDecimal amount,
        String address,
        OrderStatus status,
        String failureReason,
        Long paymentId,
        Long shipmentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getCustomerId(), order.getProductId(), order.getQuantity(),
                order.getAmount(), order.getAddress(), order.getStatus(), order.getFailureReason(),
                order.getPaymentId(), order.getShipmentId(), order.getCreatedAt(), order.getUpdatedAt());
    }
}
