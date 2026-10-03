package com.poc.practica5.order.exception;

public class PaymentRejectedException extends BusinessException {
    public PaymentRejectedException(String message) {
        super("PAYMENT_REJECTED", message);
    }
}
