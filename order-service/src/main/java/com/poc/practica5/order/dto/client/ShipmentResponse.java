package com.poc.practica5.order.dto.client;

import java.time.LocalDate;

public record ShipmentResponse(Long shipmentId, Long orderId, String address, String status, LocalDate scheduledDate) {
}
