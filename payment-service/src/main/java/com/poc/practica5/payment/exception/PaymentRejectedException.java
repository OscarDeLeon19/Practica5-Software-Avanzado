package com.poc.practica5.payment.exception;

import org.springframework.http.HttpStatus;

public class PaymentRejectedException extends ApiException {
    public PaymentRejectedException() {
        super(HttpStatus.PAYMENT_REQUIRED, "PAYMENT_REJECTED", "Pago rechazado: fondos insuficientes");
    }
}
