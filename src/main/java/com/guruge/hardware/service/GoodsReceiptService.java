package com.guruge.hardware.service;

import com.guruge.hardware.dto.request.ReceiveRequest;
import com.guruge.hardware.entity.GoodsReceipt;
import com.guruge.hardware.entity.GoodsReceiptItem;
import com.guruge.hardware.entity.Product;
import com.guruge.hardware.entity.PurchaseOrder;
import com.guruge.hardware.entity.PurchaseOrderItem;
import com.guruge.hardware.exception.BusinessException;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.GoodsReceiptRepository;
import com.guruge.hardware.repository.ProductRepository;
import com.guruge.hardware.repository.PurchaseOrderItemRepository;
import com.guruge.hardware.repository.PurchaseOrderRepository;
import com.guruge.hardware.util.NumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final ProductRepository productRepository;
    private final StockMovementService stockMovementService;
    private final NumberGenerator numberGenerator;
    private final AuditLogService auditLogService;

    @Transactional
    public GoodsReceipt receiveGoods(ReceiveRequest req, Long receivedBy) {
        PurchaseOrder po = purchaseOrderRepository.findById(req.getPurchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", req.getPurchaseOrderId()));
        String status = po.getStatus();
        if (!("CONFIRMED".equalsIgnoreCase(status) || "SENT".equalsIgnoreCase(status)
                || "PARTIALLY_RECEIVED".equalsIgnoreCase(status))) {
            throw new BusinessException("PO status does not allow receiving: " + status);
        }

        Map<Long, PurchaseOrderItem> poiMap = new HashMap<>();
        for (PurchaseOrderItem poi : po.getItems()) {
            poiMap.put(poi.getId(), poi);
        }

        GoodsReceipt gr = GoodsReceipt.builder()
                .receiptNumber(numberGenerator.generateGrNumber())
                .purchaseOrder(po)
                .supplier(po.getSupplier())
                .receivedBy(receivedBy)
                .notes(req.getNotes())
                .status("COMPLETED")
                .build();

        for (ReceiveRequest.ReceiveItemRequest itemReq : req.getItems()) {
            PurchaseOrderItem poi = purchaseOrderItemRepository.findById(itemReq.getPurchaseOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrderItem", "id", itemReq.getPurchaseOrderItemId()));
            if (!poi.getPurchaseOrder().getId().equals(po.getId())) {
                throw new BusinessException("PO item does not belong to PO: " + po.getPoNumber());
            }
            int receivedQty = itemReq.getReceivedQty() != null ? itemReq.getReceivedQty() : 0;
            int damagedQty = itemReq.getDamagedQty() != null ? itemReq.getDamagedQty() : 0;
            if (damagedQty > receivedQty) {
                throw new BusinessException("Damaged quantity cannot exceed received quantity for item: " + poi.getId());
            }
            int remaining = poi.getOrderedQty() - poi.getReceivedQty();
            if (receivedQty > remaining) {
                throw new BusinessException("Received quantity exceeds remaining (" + remaining + ") for item: " + poi.getId());
            }
            int accepted = receivedQty - damagedQty;

            poi.setReceivedQty(poi.getReceivedQty() + receivedQty);
            poi.setDamagedQty(poi.getDamagedQty() + damagedQty);
            purchaseOrderItemRepository.save(poi);

            if (accepted > 0) {
                Product product = productRepository.findByIdForUpdate(poi.getProduct().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", poi.getProduct().getId()));
                int prev = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
                int next = prev + accepted;
                product.setCurrentStock(next);
                productRepository.save(product);
                stockMovementService.record(product, accepted, prev, next,
                        "PURCHASE", "GOODS_RECEIPT", null, "PO " + po.getPoNumber(), receivedBy);
            }

            GoodsReceiptItem gri = GoodsReceiptItem.builder()
                    .goodsReceipt(gr)
                    .purchaseOrderItem(poi)
                    .receivedQty(receivedQty)
                    .acceptedQty(accepted)
                    .damagedQty(damagedQty)
                    .notes(itemReq.getNotes())
                    .build();
            gr.getItems().add(gri);
        }

        GoodsReceipt saved = goodsReceiptRepository.save(gr);

        // update PO status
        boolean allReceived = po.getItems().stream()
                .allMatch(i -> i.getReceivedQty() >= i.getOrderedQty());
        // re-read updated qtys from managed entities
        boolean complete = true;
        for (PurchaseOrderItem i : po.getItems()) {
            if (i.getReceivedQty() < i.getOrderedQty()) {
                complete = false;
                break;
            }
        }
        po.setStatus(complete && allReceived ? "RECEIVED" : "PARTIALLY_RECEIVED");
        purchaseOrderRepository.save(po);

        // backfill referenceId on txns is optional; skip

        auditLogService.log("GR_RECEIVE", "GoodsReceipt", String.valueOf(saved.getId()),
                po.getPoNumber(), saved.getReceiptNumber(), receivedBy, null, null);
        return saved;
    }

    @Transactional(readOnly = true)
    public GoodsReceipt getById(Long id) {
        return goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoodsReceipt", "id", id));
    }
}
