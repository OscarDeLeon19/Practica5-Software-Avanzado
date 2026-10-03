package com.poc.practica5.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long orderId) {
        super("Orden " + orderId + " no encontrada");
    }
}
