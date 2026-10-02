package com.poc.practica5.shipping.repository;

import com.poc.practica5.shipping.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}
