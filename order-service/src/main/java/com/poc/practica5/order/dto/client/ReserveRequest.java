package com.poc.practica5.order.dto.client;

public record ReserveRequest(Long orderId, String productId, Integer quantity) {
}
