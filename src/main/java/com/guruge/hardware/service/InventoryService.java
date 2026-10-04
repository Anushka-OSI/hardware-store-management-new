package com.guruge.hardware.service;

import com.guruge.hardware.entity.InventoryTransaction;
import com.guruge.hardware.entity.Product;
import com.guruge.hardware.exception.BusinessException;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.InventoryTransactionRepository;
import com.guruge.hardware.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final StockMovementService stockMovementService;

    @Transactional(readOnly = true)
    public Page<InventoryTransaction> getMovements(Long productId, Pageable pageable) {
        if (productId != null) {
            List<InventoryTransaction> list = inventoryTransactionRepository.findByProductId(productId);
            int start = (int) Math.min(pageable.getOffset(), list.size());
            int end = Math.min(start + pageable.getPageSize(), list.size());
            List<InventoryTransaction> sub = list.subList(start, end);
            return new org.springframework.data.domain.PageImpl<>(sub, pageable, list.size());
        }
        return inventoryTransactionRepository.findAll(pageable);
    }

    @Transactional
    public Product adjustStock(Long productId, int qtyChange, String reason, Long userId) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        int prev = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
        int next = prev + qtyChange;
        if (next < 0) {
            throw new BusinessException("Stock adjustment would result in negative stock for: " + product.getName());
        }
        product.setCurrentStock(next);
        Product saved = productRepository.save(product);
        String movementType = qtyChange >= 0 ? "ADJUSTMENT" : "ADJUSTMENT";
        stockMovementService.record(saved, qtyChange, prev, next, movementType, "MANUAL", null, reason, userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public BigDecimal getValuation() {
        return productRepository.findAll().stream()
                .map(p -> {
                    BigDecimal cost = p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO;
                    int qty = p.getCurrentStock() != null ? p.getCurrentStock() : 0;
                    return cost.multiply(BigDecimal.valueOf(qty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<Product> getLowStock() {
        return productRepository.findLowStockProducts();
    }

    @Transactional(readOnly = true)
    public List<Product> getOutOfStock() {
        return productRepository.findAll().stream()
                .filter(p -> p.getCurrentStock() == null || p.getCurrentStock() <= 0)
                .toList();
    }
}
