package com.poc.practica5.order.repository;

import com.poc.practica5.order.entity.PendingCompensation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PendingCompensationRepository extends JpaRepository<PendingCompensation, Long> {
    List<PendingCompensation> findAllByOrderByCreatedAtAscIdAsc();

    boolean existsByOrderId(Long orderId);
}
