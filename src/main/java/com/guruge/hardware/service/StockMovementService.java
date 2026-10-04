package com.guruge.hardware.service;

import com.guruge.hardware.entity.InventoryTransaction;
import com.guruge.hardware.entity.Product;
import com.guruge.hardware.repository.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final InventoryTransactionRepository inventoryTransactionRepository;

    @Transactional
    public InventoryTransaction record(Product product, Integer qty, Integer prevStock, Integer newStock,
                                       String movementType, String refType, Long refId,
                                       String reason, Long userId) {
        InventoryTransaction txn = InventoryTransaction.builder()
                .product(product)
                .quantity(qty)
                .previousStock(prevStock)
                .newStock(newStock)
                .movementType(movementType)
                .referenceType(refType)
                .referenceId(refId)
                .reason(reason)
                .userId(userId)
                .build();
        return inventoryTransactionRepository.save(txn);
    }
}
