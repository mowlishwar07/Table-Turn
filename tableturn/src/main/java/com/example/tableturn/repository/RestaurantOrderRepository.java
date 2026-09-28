package com.example.tableturn.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tableturn.model.RestaurantOrder;

public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, Long> {
    List<RestaurantOrder> findAllByOrderByCreatedAtDesc();
    List<RestaurantOrder> findAllByReservationIdAndBillIsNullOrderByCreatedAtAsc(Long reservationId);
    List<RestaurantOrder> findAllByBillIdOrderByCreatedAtAsc(Long billId);
    long countByTableId(Long tableId);
    long countByReservationId(Long reservationId);
}
