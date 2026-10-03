package com.poc.practica5.order.dto.client;

import java.math.BigDecimal;

public record PaymentResponse(Long paymentId, Long orderId, BigDecimal amount, String status) {
}
