package com.guruge.hardware.repository;

import com.guruge.hardware.entity.GoodsReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, Long> {

    List<GoodsReceiptItem> findByGoodsReceiptId(Long goodsReceiptId);

    List<GoodsReceiptItem> findByPurchaseOrderItemId(Long purchaseOrderItemId);
}
