package com.poc.practica5.shipping.exception;

import org.springframework.http.HttpStatus;

public class ChaosException extends ApiException {
    public ChaosException(String serviceName) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "SIMULATED_FAILURE", "Fallo simulado en " + serviceName);
    }
}
