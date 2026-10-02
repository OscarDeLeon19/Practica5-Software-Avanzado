package com.poc.practica5.inventory.dto;

public record ReleaseResponse(Long orderId, int releasedReservations, String message) {
}
