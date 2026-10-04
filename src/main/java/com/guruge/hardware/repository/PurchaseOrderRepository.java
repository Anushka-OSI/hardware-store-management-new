package com.guruge.hardware.repository;

import com.guruge.hardware.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    List<PurchaseOrder> findBySupplierId(Long supplierId);

    List<PurchaseOrder> findByStatus(String status);

    boolean existsByPoNumber(String poNumber);
}
