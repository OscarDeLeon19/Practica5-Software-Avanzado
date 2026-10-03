package com.poc.practica5.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Order to create through the Saga")
public record OrderRequest(
        @Schema(description = "Customer id", example = "C-100") @NotBlank String customerId,
        @Schema(description = "Product id (P-001 has stock, P-002 has no stock, P-003 has 5 units)", example = "P-001")
        @NotBlank String productId,
        @Schema(description = "Units to order", example = "1") @NotNull @Positive Integer quantity,
        @Schema(description = "Amount to charge; greater than 10000 is rejected by Payment", example = "1500")
        @NotNull @Positive BigDecimal amount,
        @Schema(description = "Delivery address", example = "742 Evergreen Terrace") @NotBlank String address) {
}
