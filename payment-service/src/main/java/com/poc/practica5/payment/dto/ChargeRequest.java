package com.poc.practica5.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Charge request")
public record ChargeRequest(
        @Schema(description = "Order id", example = "1") @NotNull Long orderId,
        @Schema(description = "Amount to charge; greater than 10000 is rejected", example = "1500")
        @NotNull @Positive BigDecimal amount) {
}
