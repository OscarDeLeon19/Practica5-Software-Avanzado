package com.poc.practica5.order.exception;

import com.poc.practica5.order.entity.Order;
import lombok.Getter;

@Getter
public class SagaFailedException extends RuntimeException {
    private final transient Order order;

    public SagaFailedException(Order order, RuntimeException cause) {
        super(cause.getMessage(), cause);
        this.order = order;
    }
}
