package com.poc.practica5.shipping.service;

import com.poc.practica5.shipping.dto.ScheduleRequest;
import com.poc.practica5.shipping.dto.ShipmentResponse;
import com.poc.practica5.shipping.entity.Shipment;
import com.poc.practica5.shipping.entity.ShipmentStatus;
import com.poc.practica5.shipping.exception.NotFoundException;
import com.poc.practica5.shipping.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {
    private static final int DELIVERY_DAYS = 3;

    private final ShipmentRepository shipmentRepository;

    @Transactional
    public ShipmentResponse schedule(ScheduleRequest request) {
        Shipment shipment = shipmentRepository.save(Shipment.builder()
                .orderId(request.orderId())
                .address(request.address())
                .status(ShipmentStatus.SCHEDULED)
                .scheduledDate(LocalDate.now().plusDays(DELIVERY_DAYS))
                .build());
        log.info("Envío {} SCHEDULED para la orden {} el {}", shipment.getId(), shipment.getOrderId(),
                shipment.getScheduledDate());
        return ShipmentResponse.from(shipment);
    }

    @Transactional
    public ShipmentResponse cancel(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new NotFoundException("Envío " + shipmentId + " no encontrado"));
        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            log.info("[COMPENSACION] Envío {} ya estaba CANCELLED, no se hace nada (idempotente)", shipmentId);
            return ShipmentResponse.from(shipment);
        }
        shipment.setStatus(ShipmentStatus.CANCELLED);
        log.info("[COMPENSACION] Envío {} de la orden {} CANCELLED", shipmentId, shipment.getOrderId());
        return ShipmentResponse.from(shipment);
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> findAll() {
        return shipmentRepository.findAll().stream().map(ShipmentResponse::from).toList();
    }
}
