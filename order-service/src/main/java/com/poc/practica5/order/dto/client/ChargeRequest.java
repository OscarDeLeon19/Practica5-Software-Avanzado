package com.poc.practica5.order.dto.client;

import java.math.BigDecimal;

public record ChargeRequest(Long orderId, BigDecimal amount) {
}
