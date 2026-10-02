package com.poc.practica5.payment.controller;

import com.poc.practica5.payment.dto.ChargeRequest;
import com.poc.practica5.payment.dto.PaymentResponse;
import com.poc.practica5.payment.exception.ApiError;
import com.poc.practica5.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Payments", description = "Charges and refunds")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(summary = "Charge an order",
            description = "Creates a CHARGED payment. Amounts greater than 10000 are rejected with 402.")
    @ApiResponse(responseCode = "201", description = "Payment CHARGED")
    @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "402", description = "Payment rejected: insufficient funds",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "500", description = "Simulated failure (chaos mode ERROR)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse charge(@Valid @RequestBody ChargeRequest request) {
        return paymentService.charge(request);
    }

    @Operation(summary = "Refund a payment (compensation)",
            description = "Marks the payment as REFUNDED. Idempotent: refunding an already refunded payment returns 200.")
    @ApiResponse(responseCode = "200", description = "Payment REFUNDED")
    @ApiResponse(responseCode = "404", description = "Payment not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "500", description = "Simulated failure (chaos mode ERROR or ERROR_ON_REFUND)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/{id}/refund")
    public PaymentResponse refund(@Parameter(description = "Payment id", example = "1") @PathVariable Long id) {
        return paymentService.refund(id);
    }

    @Operation(summary = "List all payments")
    @GetMapping
    public List<PaymentResponse> findAll() {
        return paymentService.findAll();
    }
}
