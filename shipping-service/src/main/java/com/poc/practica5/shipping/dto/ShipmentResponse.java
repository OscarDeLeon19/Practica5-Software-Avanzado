package com.poc.practica5.shipping.dto;

import com.poc.practica5.shipping.entity.Shipment;
import com.poc.practica5.shipping.entity.ShipmentStatus;

import java.time.LocalDate;

public record ShipmentResponse(
        Long shipmentId,
        Long orderId,
        String address,
        ShipmentStatus status,
        LocalDate scheduledDate) {
    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(shipment.getId(), shipment.getOrderId(), shipment.getAddress(),
                shipment.getStatus(), shipment.getScheduledDate());
    }
}
