package com.poc.practica5.inventory.dto;

import com.poc.practica5.inventory.entity.Reservation;
import com.poc.practica5.inventory.entity.ReservationStatus;

public record ReservationResponse(
        Long reservationId,
        Long orderId,
        String productId,
        int quantity,
        ReservationStatus status) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getOrderId(), reservation.getProductId(),
                reservation.getQuantity(), reservation.getStatus());
    }
}
