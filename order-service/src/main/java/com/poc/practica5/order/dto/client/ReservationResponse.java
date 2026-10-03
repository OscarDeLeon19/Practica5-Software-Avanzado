package com.poc.practica5.order.dto.client;

public record ReservationResponse(Long reservationId, Long orderId, String productId, int quantity, String status) {
}
