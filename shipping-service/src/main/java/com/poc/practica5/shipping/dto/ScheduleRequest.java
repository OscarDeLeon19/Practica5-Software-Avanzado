package com.poc.practica5.shipping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Shipment scheduling request")
public record ScheduleRequest(
        @Schema(description = "Order id", example = "1") @NotNull Long orderId,
        @Schema(description = "Delivery address", example = "742 Evergreen Terrace") @NotBlank String address) {
}
