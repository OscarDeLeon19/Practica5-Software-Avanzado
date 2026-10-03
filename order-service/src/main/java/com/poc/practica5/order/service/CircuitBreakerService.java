package com.poc.practica5.order.service;

import com.poc.practica5.order.dto.CircuitBreakerStatus;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CircuitBreakerService {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public List<CircuitBreakerStatus> findAll() {
        return circuitBreakerRegistry.getAllCircuitBreakers().stream()
                .sorted(Comparator.comparing(CircuitBreaker::getName))
                .map(this::toStatus)
                .toList();
    }

    public List<CircuitBreakerStatus> resetAll() {
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(breaker -> {
            breaker.reset();
            log.info("[CB] {} reiniciado manualmente -> CLOSED", breaker.getName());
        });
        return findAll();
    }

    private CircuitBreakerStatus toStatus(CircuitBreaker breaker) {
        CircuitBreaker.Metrics metrics = breaker.getMetrics();
        return new CircuitBreakerStatus(
                breaker.getName(),
                breaker.getState().name(),
                metrics.getFailureRate(),
                metrics.getSlowCallRate(),
                metrics.getNumberOfBufferedCalls(),
                metrics.getNumberOfFailedCalls(),
                metrics.getNumberOfSlowCalls(),
                metrics.getNumberOfNotPermittedCalls());
    }
}
