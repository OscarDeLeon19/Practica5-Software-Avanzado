package com.poc.practica5.inventory.repository;

import com.poc.practica5.inventory.entity.Reservation;
import com.poc.practica5.inventory.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByOrderIdAndStatus(Long orderId, ReservationStatus status);
}
