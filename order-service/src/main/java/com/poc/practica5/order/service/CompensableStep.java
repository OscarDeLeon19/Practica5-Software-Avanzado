package com.poc.practica5.order.service;

import com.poc.practica5.order.entity.SagaStep;

public record CompensableStep(SagaStep step, String referenceId, Runnable compensation) {
}
