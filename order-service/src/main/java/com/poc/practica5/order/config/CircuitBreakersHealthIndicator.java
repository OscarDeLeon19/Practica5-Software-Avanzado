package com.poc.practica5.order.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

@Component("circuitBreakers")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "management.health.circuitbreakers.enabled", havingValue = "true", matchIfMissing = true)
public class CircuitBreakersHealthIndicator implements HealthIndicator {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public Health health() {
        Map<String, Object> details = new TreeMap<>();
        for (CircuitBreaker breaker : circuitBreakerRegistry.getAllCircuitBreakers()) {
            CircuitBreaker.Metrics metrics = breaker.getMetrics();
            details.put(breaker.getName(), Map.of(
                    "state", breaker.getState().name(),
                    "failureRate", metrics.getFailureRate() + "%",
                    "slowCallRate", metrics.getSlowCallRate() + "%",
                    "bufferedCalls", metrics.getNumberOfBufferedCalls(),
                    "notPermittedCalls", metrics.getNumberOfNotPermittedCalls()));
        }
        return Health.up().withDetails(details).build();
    }
}
