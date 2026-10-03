package com.poc.practica5.order.exception;

import com.poc.practica5.order.dto.ApiError;
import com.poc.practica5.order.dto.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SagaFailedException.class)
    public ResponseEntity<ApiError> handleSagaFailed(SagaFailedException ex) {
        Throwable cause = ex.getCause();
        HttpStatus status;
        String error;
        if (cause instanceof BusinessException business) {
            status = HttpStatus.CONFLICT;
            error = business.getErrorCode();
        } else if (cause instanceof ServiceUnavailableException || cause instanceof RemoteServiceException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            error = "SERVICE_UNAVAILABLE";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            error = "INTERNAL_ERROR";
        }
        log.warn("[SAGA] Respondiendo {} para la orden {}: {}", status.value(), ex.getOrder().getId(), ex.getMessage());
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), error, ex.getMessage(), OrderResponse.from(ex.getOrder())));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(OrderNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Petición inválida: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatusCode status = errorResponse.getStatusCode();
            return ResponseEntity.status(status)
                    .body(ApiError.of(status.value(), "HTTP_" + status.value(), ex.getMessage()));
        }
        log.error("Error inesperado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), error, message));
    }
}
