package com.poc.practica5.order.dto.client;

public record RemoteError(String timestamp, int status, String error, String message) {
}
