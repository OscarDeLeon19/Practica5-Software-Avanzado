package com.poc.practica5.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Stock reservation request")
public record ReserveRequest(
        @Schema(description = "Order id", example = "1") @NotNull Long orderId,
        @Schema(description = "Product id", example = "P-001") @NotBlank String productId,
        @Schema(description = "Units to reserve", example = "1") @NotNull @Positive Integer quantity) {
}
