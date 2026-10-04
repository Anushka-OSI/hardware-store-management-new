package com.guruge.hardware.repository;

import com.guruge.hardware.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {

    Optional<Sale> findByReceiptNumber(String receiptNumber);

    List<Sale> findByCustomerId(Long customerId);

    List<Sale> findByCashierId(Long cashierId);

    List<Sale> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Sale> findByPaymentStatus(String paymentStatus);

    List<Sale> findBySaleStatus(String saleStatus);

    boolean existsByReceiptNumber(String receiptNumber);
}
