package com.poc.practica5.inventory.service;

import com.poc.practica5.inventory.dto.ProductResponse;
import com.poc.practica5.inventory.dto.ReleaseResponse;
import com.poc.practica5.inventory.dto.ReservationResponse;
import com.poc.practica5.inventory.dto.ReserveRequest;
import com.poc.practica5.inventory.entity.Product;
import com.poc.practica5.inventory.entity.Reservation;
import com.poc.practica5.inventory.entity.ReservationStatus;
import com.poc.practica5.inventory.exception.NotFoundException;
import com.poc.practica5.inventory.exception.OutOfStockException;
import com.poc.practica5.inventory.repository.ProductRepository;
import com.poc.practica5.inventory.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationResponse reserve(ReserveRequest request) {
        Product product = productRepository.findByIdForUpdate(request.productId())
                .orElseThrow(() -> new NotFoundException("Producto " + request.productId() + " no encontrado"));
        if (product.getStock() < request.quantity()) {
            log.warn("Sin stock para el producto {} (stock={}, solicitado={}, orden={})",
                    product.getId(), product.getStock(), request.quantity(), request.orderId());
            throw new OutOfStockException(product.getId());
        }
        product.setStock(product.getStock() - request.quantity());
        Reservation reservation = reservationRepository.save(Reservation.builder()
                .orderId(request.orderId())
                .productId(product.getId())
                .quantity(request.quantity())
                .status(ReservationStatus.RESERVED)
                .build());
        log.info("Reserva {} RESERVED: orden={} producto={} cantidad={} stock restante={}",
                reservation.getId(), request.orderId(), product.getId(), request.quantity(), product.getStock());
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReleaseResponse release(Long orderId) {
        List<Reservation> active = reservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.RESERVED);
        if (active.isEmpty()) {
            log.info("[COMPENSACION] La orden {} no tiene reservas activas, nada que liberar (idempotente)", orderId);
            return new ReleaseResponse(orderId, 0, "No hay reservas activas para la orden");
        }
        for (Reservation reservation : active) {
            Product product = productRepository.findByIdForUpdate(reservation.getProductId())
                    .orElseThrow(() -> new NotFoundException("Producto " + reservation.getProductId() + " no encontrado"));
            product.setStock(product.getStock() + reservation.getQuantity());
            reservation.setStatus(ReservationStatus.RELEASED);
            log.info("[COMPENSACION] Reserva {} RELEASED: producto={} +{} unidades, stock={}",
                    reservation.getId(), product.getId(), reservation.getQuantity(), product.getStock());
        }
        return new ReleaseResponse(orderId, active.size(), "Stock liberado");
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findProducts() {
        return productRepository.findAllByOrderByIdAsc().stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findReservations() {
        return reservationRepository.findAll().stream().map(ReservationResponse::from).toList();
    }
}
