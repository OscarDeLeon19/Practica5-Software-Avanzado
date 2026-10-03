package com.poc.practica5.order.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResilienceEventLogger {
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    @EventListener(ApplicationReadyEvent.class)
    public void registerListeners() {
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(this::listen);
        circuitBreakerRegistry.getEventPublisher().onEntryAdded(event -> listen(event.getAddedEntry()));
        retryRegistry.getAllRetries().forEach(this::listen);
        retryRegistry.getEventPublisher().onEntryAdded(event -> listen(event.getAddedEntry()));
        log.info("[CB] Listeners registrados para: {}", circuitBreakerRegistry.getAllCircuitBreakers().stream()
                .map(CircuitBreaker::getName).sorted().toList());
    }

    private void listen(CircuitBreaker breaker) {
        breaker.getEventPublisher().onStateTransition(event -> log.warn("[CB] {}: {} -> {}",
                event.getCircuitBreakerName(),
                event.getStateTransition().getFromState(),
                event.getStateTransition().getToState()));
    }

    private void listen(Retry retry) {
        retry.getEventPublisher().onRetry(event -> log.warn(
                "[COMPENSACION] Retry '{}' intento {} falló: {}. Reintentando en {} ms",
                event.getName(), event.getNumberOfRetryAttempts(),
                event.getLastThrowable() != null ? event.getLastThrowable().getMessage() : "-",
                event.getWaitInterval().toMillis()));
    }
}
