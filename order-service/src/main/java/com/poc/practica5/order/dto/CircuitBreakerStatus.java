package com.poc.practica5.order.dto;

public record CircuitBreakerStatus(
        String name,
        String state,
        float failureRate,
        float slowCallRate,
        int bufferedCalls,
        int failedCalls,
        int slowCalls,
        long notPermittedCalls) {
}
