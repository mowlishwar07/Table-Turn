package com.example.tableturn.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tableturn.model.Bill;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findAllByOrderByGeneratedAtDesc();
    long countByTableId(Long tableId);
}
