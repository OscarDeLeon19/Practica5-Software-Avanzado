package com.poc.practica5.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Stock release request")
public record ReleaseRequest(@Schema(description = "Order id", example = "1") @NotNull Long orderId) {
}
