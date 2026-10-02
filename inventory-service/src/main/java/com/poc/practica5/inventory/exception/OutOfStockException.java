package com.poc.practica5.inventory.exception;

import org.springframework.http.HttpStatus;

public class OutOfStockException extends ApiException {
    public OutOfStockException(String productId) {
        super(HttpStatus.CONFLICT, "OUT_OF_STOCK", "Sin stock para el producto " + productId);
    }
}
