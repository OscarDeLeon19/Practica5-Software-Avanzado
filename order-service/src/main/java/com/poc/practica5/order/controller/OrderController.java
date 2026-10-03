package com.poc.practica5.order.controller;

import com.poc.practica5.order.dto.ApiError;
import com.poc.practica5.order.dto.OrderRequest;
import com.poc.practica5.order.dto.OrderResponse;
import com.poc.practica5.order.dto.SagaLogResponse;
import com.poc.practica5.order.service.OrderService;
import com.poc.practica5.order.service.SagaOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Orders", description = "Order creation through the Saga orchestrator and order queries")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final SagaOrchestrator sagaOrchestrator;
    private final OrderService orderService;

    @Operation(summary = "Create an order (runs the Saga)",
            description = "Creates the order and executes the Saga steps Payment -> Inventory -> Shipping. "
                    + "If a step fails, the completed steps are compensated in reverse order and the order "
                    + "is returned inside the error body.")
    @ApiResponse(responseCode = "201", description = "Saga completed, order COMPLETED")
    @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Business failure (out of stock, payment rejected), order CANCELLED",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "503", description = "A service failed or its circuit breaker is open, "
            + "order CANCELLED or COMPENSATION_PENDING",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(sagaOrchestrator.execute(request)));
    }

    @Operation(summary = "Cancel an order (Order compensation)",
            description = "Marks the order as CANCELLED. Idempotent: cancelling an already cancelled order returns 200.")
    @ApiResponse(responseCode = "200", description = "Order cancelled")
    @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{id}")
    public OrderResponse cancel(@Parameter(description = "Order id", example = "1") @PathVariable Long id) {
        return orderService.cancel(id);
    }

    @Operation(summary = "Get an order by id")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{id}")
    public OrderResponse findById(@Parameter(description = "Order id", example = "1") @PathVariable Long id) {
        return orderService.findById(id);
    }

    @Operation(summary = "Get the Saga log of an order",
            description = "Returns every executed step and compensation of the order in chronological order.")
    @ApiResponse(responseCode = "200", description = "Saga log entries")
    @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{id}/saga")
    public List<SagaLogResponse> findSagaLog(@Parameter(description = "Order id", example = "1") @PathVariable Long id) {
        return orderService.findSagaLog(id);
    }

    @Operation(summary = "List all orders")
    @GetMapping
    public List<OrderResponse> findAll() {
        return orderService.findAll();
    }
}
