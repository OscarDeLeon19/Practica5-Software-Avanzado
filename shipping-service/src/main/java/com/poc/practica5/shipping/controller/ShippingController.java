package com.poc.practica5.shipping.controller;

import com.poc.practica5.shipping.dto.ScheduleRequest;
import com.poc.practica5.shipping.dto.ShipmentResponse;
import com.poc.practica5.shipping.exception.ApiError;
import com.poc.practica5.shipping.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Shipping", description = "Shipment scheduling and cancellation")
@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShippingController {
    private final ShippingService shippingService;

    @Operation(summary = "Schedule a shipment",
            description = "Creates a SCHEDULED shipment with delivery date today + 3 days.")
    @ApiResponse(responseCode = "201", description = "Shipment SCHEDULED")
    @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "500", description = "Simulated failure (chaos mode ERROR)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse schedule(@Valid @RequestBody ScheduleRequest request) {
        return shippingService.schedule(request);
    }

    @Operation(summary = "Cancel a shipment (compensation)",
            description = "Marks the shipment as CANCELLED. Idempotent: cancelling an already cancelled shipment returns 200.")
    @ApiResponse(responseCode = "200", description = "Shipment CANCELLED")
    @ApiResponse(responseCode = "404", description = "Shipment not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "500", description = "Simulated failure (chaos mode ERROR)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{id}")
    public ShipmentResponse cancel(@Parameter(description = "Shipment id", example = "1") @PathVariable Long id) {
        return shippingService.cancel(id);
    }

    @Operation(summary = "List all shipments")
    @GetMapping
    public List<ShipmentResponse> findAll() {
        return shippingService.findAll();
    }
}
