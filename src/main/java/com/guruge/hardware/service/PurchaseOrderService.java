package com.guruge.hardware.service;

import com.guruge.hardware.dto.request.PurchaseOrderRequest;
import com.guruge.hardware.entity.Product;
import com.guruge.hardware.entity.PurchaseOrder;
import com.guruge.hardware.entity.PurchaseOrderItem;
import com.guruge.hardware.entity.Supplier;
import com.guruge.hardware.exception.BusinessException;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.ProductRepository;
import com.guruge.hardware.repository.PurchaseOrderRepository;
import com.guruge.hardware.repository.SupplierRepository;
import com.guruge.hardware.util.MoneyUtils;
import com.guruge.hardware.util.NumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final NumberGenerator numberGenerator;
    private final AuditLogService auditLogService;
    @Lazy
    private final GoodsReceiptService goodsReceiptService;

    @Transactional(readOnly = true)
    public Page<PurchaseOrder> list(Long supplierId, String status, Pageable pageable) {
        return list(supplierId, status, false, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrder> list(Long supplierId, String status, boolean excludeDraft, Pageable pageable) {
        Specification<PurchaseOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (supplierId != null) {
                predicates.add(cb.equal(root.get("supplier").get("id"), supplierId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status.toUpperCase()));
            }
            if (excludeDraft) {
                // Supplier portal: DRAFT orders are internal and must never be visible
                predicates.add(cb.notEqual(root.get("status"), "DRAFT"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return purchaseOrderRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getDetail(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrderRequest req, Long actorId) {
        Supplier supplier = supplierRepository.findById(req.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", req.getSupplierId()));
        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(numberGenerator.generatePoNumber())
                .supplier(supplier)
                .expectedDeliveryDate(req.getExpectedDeliveryDate())
                .notes(req.getNotes())
                .status("DRAFT")
                .createdBy(actorId)
                .build();
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderRequest.PurchaseOrderItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));
            BigDecimal lineTotal = MoneyUtils.mul(itemReq.getUnitCost(), itemReq.getOrderedQty());
            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .orderedQty(itemReq.getOrderedQty())
                    .unitCost(MoneyUtils.round(itemReq.getUnitCost()))
                    .totalCost(lineTotal)
                    .receivedQty(0)
                    .damagedQty(0)
                    .notes(itemReq.getNotes())
                    .build();
            po.getItems().add(item);
            total = MoneyUtils.add(total, lineTotal);
        }
        po.setTotalAmount(total);
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditLogService.log("PO_CREATE", "PurchaseOrder", String.valueOf(saved.getId()),
                null, saved.getPoNumber(), actorId, null, null);
        return saved;
    }

    @Transactional
    public PurchaseOrder updateDraft(Long id, PurchaseOrderRequest req, Long actorId) {
        PurchaseOrder po = getDetail(id);
        if (!"DRAFT".equalsIgnoreCase(po.getStatus())) {
            throw new BusinessException("Only DRAFT orders can be updated");
        }
        Supplier supplier = supplierRepository.findById(req.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", req.getSupplierId()));
        po.setSupplier(supplier);
        po.setExpectedDeliveryDate(req.getExpectedDeliveryDate());
        po.setNotes(req.getNotes());
        po.getItems().clear();
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderRequest.PurchaseOrderItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));
            BigDecimal lineTotal = MoneyUtils.mul(itemReq.getUnitCost(), itemReq.getOrderedQty());
            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .orderedQty(itemReq.getOrderedQty())
                    .unitCost(MoneyUtils.round(itemReq.getUnitCost()))
                    .totalCost(lineTotal)
                    .receivedQty(0)
                    .damagedQty(0)
                    .notes(itemReq.getNotes())
                    .build();
            po.getItems().add(item);
            total = MoneyUtils.add(total, lineTotal);
        }
        po.setTotalAmount(total);
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditLogService.log("PO_UPDATE", "PurchaseOrder", String.valueOf(id), null, saved.getPoNumber(), actorId, null, null);
        return saved;
    }

    @Transactional
    public PurchaseOrder send(Long id, Long actorId) {
        PurchaseOrder po = getDetail(id);
        if (!"DRAFT".equalsIgnoreCase(po.getStatus())) {
            throw new BusinessException("Only DRAFT orders can be sent");
        }
        po.setStatus("SENT");
        po.setApprovedBy(actorId);
        return purchaseOrderRepository.save(po);
    }

    @Transactional
    public PurchaseOrder confirm(Long id, Long actorId) {
        return confirm(id, actorId, null);
    }

    @Transactional
    public PurchaseOrder confirm(Long id, Long actorId, Long supplierScope) {
        PurchaseOrder po = getDetail(id);
        if (supplierScope != null) {
            // Supplier portal: may confirm only its own SENT orders (availability check)
            if (po.getSupplier() == null || !supplierScope.equals(po.getSupplier().getId())) {
                throw new com.guruge.hardware.exception.UnauthorizedException(
                        "You can only confirm purchase orders assigned to your company.");
            }
        }
        if (!"SENT".equalsIgnoreCase(po.getStatus())) {
            throw new BusinessException("Only SENT orders can be confirmed");
        }
        po.setStatus("CONFIRMED");
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditLogService.log("PO_CONFIRM", "PurchaseOrder", String.valueOf(id), "SENT", "CONFIRMED", actorId, null, null);
        return saved;
    }

    @Transactional
    public PurchaseOrder reject(Long id, Long actorId, Long supplierScope, String reason) {
        PurchaseOrder po = getDetail(id);
        if (supplierScope != null) {
            // Supplier portal: may reject only its own SENT orders
            if (po.getSupplier() == null || !supplierScope.equals(po.getSupplier().getId())) {
                throw new com.guruge.hardware.exception.UnauthorizedException(
                        "You can only reject purchase orders assigned to your company.");
            }
        }
        if (!"SENT".equalsIgnoreCase(po.getStatus())) {
            throw new BusinessException("Only SENT orders can be rejected");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("A rejection reason is required");
        }
        po.setStatus("CANCELLED");
        String note = "Rejected by supplier: " + reason.strip();
        po.setNotes(po.getNotes() == null || po.getNotes().isBlank() ? note : po.getNotes() + "\n" + note);
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditLogService.log("PO_REJECT", "PurchaseOrder", String.valueOf(id), "SENT", "CANCELLED", actorId, null, null);
        return saved;
    }

    @Transactional
    public PurchaseOrder cancel(Long id, Long actorId) {
        PurchaseOrder po = getDetail(id);
        if ("RECEIVED".equalsIgnoreCase(po.getStatus()) || "CANCELLED".equalsIgnoreCase(po.getStatus())) {
            throw new BusinessException("Order cannot be cancelled in status: " + po.getStatus());
        }
        po.setStatus("CANCELLED");
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditLogService.log("PO_CANCEL", "PurchaseOrder", String.valueOf(id), null, "CANCELLED", actorId, null, null);
        return saved;
    }

    @Transactional
    public Object receive(Long poId, List<com.guruge.hardware.dto.request.ReceiveRequest.ReceiveItemRequest> items,
                          String notes, Long receivedBy) {
        com.guruge.hardware.dto.request.ReceiveRequest req =
                com.guruge.hardware.dto.request.ReceiveRequest.builder()
                        .purchaseOrderId(poId)
                        .notes(notes)
                        .items(items)
                        .build();
        return goodsReceiptService.receiveGoods(req, receivedBy);
    }
}
