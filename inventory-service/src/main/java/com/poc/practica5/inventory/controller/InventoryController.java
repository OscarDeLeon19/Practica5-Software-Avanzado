package com.poc.practica5.inventory.controller;

import com.poc.practica5.inventory.dto.ProductResponse;
import com.poc.practica5.inventory.dto.ReleaseRequest;
import com.poc.practica5.inventory.dto.ReleaseResponse;
import com.poc.practica5.inventory.dto.ReservationResponse;
import com.poc.practica5.inventory.dto.ReserveRequest;
import com.poc.practica5.inventory.exception.ApiError;
import com.poc.practica5.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Inventory", description = "Products, stock reservations and releases")
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @Operation(summary = "Reserve stock for an order",
            description = "Decreases the product stock and creates a RESERVED reservation.")
    @ApiResponse(responseCode = "201", description = "Stock RESERVED")
    @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Not enough stock for the product",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "500", description = "Simulated failure (chaos mode ERROR)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(@Valid @RequestBody ReserveRequest request) {
        return inventoryService.reserve(request);
    }

    @Operation(summary = "Release the stock of an order (compensation)",
            description = "Returns the reserved stock and marks the reservations as RELEASED. "
                    + "Idempotent: if there are no active reservations it returns 200 with 0 released.")
    @ApiResponse(responseCode = "200", description = "Stock released (or nothing to release)")
    @ApiResponse(responseCode = "500", description = "Simulated failure (chaos mode ERROR)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/release")
    public ReleaseResponse release(@Valid @RequestBody ReleaseRequest request) {
        return inventoryService.release(request.orderId());
    }

    @Operation(summary = "List products with their current stock")
    @GetMapping("/products")
    public List<ProductResponse> findProducts() {
        return inventoryService.findProducts();
    }

    @Operation(summary = "List all reservations")
    @GetMapping("/reservations")
    public List<ReservationResponse> findReservations() {
        return inventoryService.findReservations();
    }
}
