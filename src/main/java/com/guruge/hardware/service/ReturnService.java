package com.guruge.hardware.service;

import com.guruge.hardware.dto.request.ReturnRequest;
import com.guruge.hardware.entity.Product;
import com.guruge.hardware.entity.ReturnEntity;
import com.guruge.hardware.entity.ReturnItem;
import com.guruge.hardware.entity.Sale;
import com.guruge.hardware.entity.SaleItem;
import com.guruge.hardware.exception.BusinessException;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.ProductRepository;
import com.guruge.hardware.repository.ReturnItemRepository;
import com.guruge.hardware.repository.ReturnRepository;
import com.guruge.hardware.repository.SaleItemRepository;
import com.guruge.hardware.repository.SaleRepository;
import com.guruge.hardware.util.MoneyUtils;
import com.guruge.hardware.util.NumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnRepository returnRepository;
    private final ReturnItemRepository returnItemRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final StockMovementService stockMovementService;
    private final NumberGenerator numberGenerator;
    private final AuditLogService auditLogService;

    @Transactional
    public ReturnEntity processReturn(Long saleId, List<ReturnRequest.ReturnItemRequest> items,
                                      String reason, Long processedBy) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", saleId));
        if ("CANCELLED".equalsIgnoreCase(sale.getSaleStatus())) {
            throw new BusinessException("Cannot return items for a cancelled sale");
        }

        Map<Long, SaleItem> saleItemMap = new HashMap<>();
        for (SaleItem si : sale.getItems()) {
            saleItemMap.put(si.getId(), si);
        }
        // already-returned quantities per sale item
        Map<Long, Integer> alreadyReturned = new HashMap<>();
        for (ReturnItem ri : returnItemRepository.findBySaleItemId(null) == null ? List.<ReturnItem>of() : List.<ReturnItem>of()) {
            // placeholder (kept simple below with full scan)
        }
        List<ReturnEntity> priorReturns = returnRepository.findBySaleId(saleId);
        for (ReturnEntity re : priorReturns) {
            List<ReturnItem> priorItems = returnItemRepository.findByReturnEntityId(re.getId());
            for (ReturnItem pi : priorItems) {
                Long sid = pi.getSaleItem() != null ? pi.getSaleItem().getId() : null;
                if (sid != null) {
                    alreadyReturned.merge(sid, pi.getReturnedQty(), Integer::sum);
                }
            }
        }

        ReturnEntity ret = ReturnEntity.builder()
                .returnNumber(numberGenerator.generateReturnNumber())
                .sale(sale)
                .customer(sale.getCustomer())
                .processedBy(processedBy)
                .totalRefund(BigDecimal.ZERO)
                .status("COMPLETED")
                .reason(reason)
                .build();

        BigDecimal totalRefund = BigDecimal.ZERO;
        for (ReturnRequest.ReturnItemRequest itemReq : items) {
            SaleItem saleItem = saleItemMap.get(itemReq.getSaleItemId());
            if (saleItem == null) {
                // fallback: load directly and verify it belongs to sale
                saleItem = saleItemRepository.findById(itemReq.getSaleItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("SaleItem", "id", itemReq.getSaleItemId()));
                if (!saleItem.getSale().getId().equals(saleId)) {
                    throw new BusinessException("Sale item does not belong to sale: " + saleId);
                }
            }
            int restored = alreadyReturned.getOrDefault(saleItem.getId(), 0);
            int maxReturnable = saleItem.getQuantity() - restored;
            if (itemReq.getReturnedQty() > maxReturnable) {
                throw new BusinessException("Return quantity exceeds returnable quantity (" + maxReturnable
                        + ") for item: " + saleItem.getProductName());
            }
            BigDecimal unitRefund = saleItem.getQuantity() > 0
                    ? MoneyUtils.div(saleItem.getLineTotal(), BigDecimal.valueOf(saleItem.getQuantity()))
                    : BigDecimal.ZERO;
            BigDecimal refund = MoneyUtils.mul(unitRefund, itemReq.getReturnedQty());
            boolean restocked = itemReq.getRestocked() == null || itemReq.getRestocked();

            ReturnItem ri = ReturnItem.builder()
                    .returnEntity(ret)
                    .saleItem(saleItem)
                    .product(saleItem.getProduct())
                    .returnedQty(itemReq.getReturnedQty())
                    .refundAmount(refund)
                    .reason(itemReq.getReason())
                    .itemCondition(itemReq.getItemCondition())
                    .restocked(restocked)
                    .build();
            ret.getItems().add(ri);
            totalRefund = MoneyUtils.add(totalRefund, refund);

            if (restocked) {
                final Long productId = saleItem.getProduct().getId();
                Product product = productRepository.findByIdForUpdate(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
                int prev = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
                int next = prev + itemReq.getReturnedQty();
                product.setCurrentStock(next);
                productRepository.save(product);
                stockMovementService.record(product, itemReq.getReturnedQty(), prev, next,
                        "RETURN", "RETURN", null, "Return for sale " + sale.getReceiptNumber(), processedBy);
            }
        }

        ret.setTotalRefund(totalRefund);
        ReturnEntity saved = returnRepository.save(ret);

        // update sale status if fully returned
        int totalSold = sale.getItems().stream().mapToInt(SaleItem::getQuantity).sum();
        int totalReturned = alreadyReturned.values().stream().mapToInt(Integer::intValue).sum()
                + items.stream().mapToInt(ReturnRequest.ReturnItemRequest::getReturnedQty).sum();
        if (totalReturned >= totalSold) {
            sale.setSaleStatus("REFUNDED");
        } else if (totalReturned > 0) {
            sale.setSaleStatus("PARTIALLY_REFUNDED");
        }
        saleRepository.save(sale);

        auditLogService.log("RETURN_PROCESS", "Return", String.valueOf(saved.getId()),
                sale.getReceiptNumber(), saved.getReturnNumber(), processedBy, null, null);
        return saved;
    }

    @Transactional(readOnly = true)
    public ReturnEntity getById(Long id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return", "id", id));
    }
}
