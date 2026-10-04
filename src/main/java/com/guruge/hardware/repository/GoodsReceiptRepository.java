package com.guruge.hardware.repository;

import com.guruge.hardware.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long>, JpaSpecificationExecutor<GoodsReceipt> {

    Optional<GoodsReceipt> findByReceiptNumber(String receiptNumber);

    List<GoodsReceipt> findByPurchaseOrderId(Long purchaseOrderId);

    List<GoodsReceipt> findBySupplierId(Long supplierId);

    boolean existsByReceiptNumber(String receiptNumber);
}
