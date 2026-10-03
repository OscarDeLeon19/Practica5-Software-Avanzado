package com.poc.practica5.order.controller;

import com.poc.practica5.order.dto.CircuitBreakerStatus;
import com.poc.practica5.order.dto.PendingCompensationResponse;
import com.poc.practica5.order.service.CircuitBreakerService;
import com.poc.practica5.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Administration", description = "Circuit breaker monitoring and pending compensations")
@RestController
@RequestMapping("/orders/admin")
@RequiredArgsConstructor
public class OrderAdminController {
    private final CircuitBreakerService circuitBreakerService;
    private final OrderService orderService;

    @Operation(summary = "Circuit breaker summary",
            description = "State and metrics of every circuit breaker. failureRate and slowCallRate are -1 "
                    + "until minimumNumberOfCalls is reached.")
    @GetMapping("/circuit-breakers")
    public List<CircuitBreakerStatus> circuitBreakers() {
        return circuitBreakerService.findAll();
    }

    @Operation(summary = "Reset all circuit breakers",
            description = "Moves every circuit breaker back to CLOSED with empty metrics, so scenarios can be repeated.")
    @PostMapping("/circuit-breakers/reset")
    public List<CircuitBreakerStatus> resetCircuitBreakers() {
        return circuitBreakerService.resetAll();
    }

    @Operation(summary = "List pending compensations",
            description = "Compensations that failed after their retries and are queued for the retry job.")
    @GetMapping("/pending-compensations")
    public List<PendingCompensationResponse> pendingCompensations() {
        return orderService.findPendingCompensations();
    }
}
