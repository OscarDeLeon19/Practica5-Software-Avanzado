package com.poc.practica5.order.exception;

public class OutOfStockException extends BusinessException {
    public OutOfStockException(String message) {
        super("OUT_OF_STOCK", message);
    }
}
