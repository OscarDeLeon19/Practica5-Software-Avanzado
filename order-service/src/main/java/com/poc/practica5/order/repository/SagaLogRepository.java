package com.poc.practica5.order.repository;

import com.poc.practica5.order.entity.SagaLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SagaLogRepository extends JpaRepository<SagaLog, Long> {
    List<SagaLog> findByOrderIdOrderByTimestampAscIdAsc(Long orderId);
}
