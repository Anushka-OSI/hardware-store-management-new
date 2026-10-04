package com.guruge.hardware.service;

import com.guruge.hardware.dto.request.SupplierRequest;
import com.guruge.hardware.entity.PurchaseOrder;
import com.guruge.hardware.entity.Supplier;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.PurchaseOrderRepository;
import com.guruge.hardware.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<Supplier> search(String keyword, Pageable pageable) {
        Specification<Supplier> spec = (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("companyName")), like),
                    cb.like(cb.lower(root.get("contactPerson")), like),
                    cb.like(cb.lower(root.get("phone")), like));
        };
        return supplierRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Supplier getById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
    }

    @Transactional
    public Supplier create(SupplierRequest req, Long actorId) {
        Supplier supplier = Supplier.builder()
                .companyName(req.getCompanyName())
                .contactPerson(req.getContactPerson())
                .phone(req.getPhone())
                .email(req.getEmail())
                .address(req.getAddress())
                .taxNumber(req.getTaxNumber())
                .status(StringUtils.hasText(req.getStatus()) ? req.getStatus().toUpperCase() : "ACTIVE")
                .notes(req.getNotes())
                .build();
        Supplier saved = supplierRepository.save(supplier);
        auditLogService.log("SUPPLIER_CREATE", "Supplier", String.valueOf(saved.getId()),
                null, saved.getCompanyName(), actorId, null, null);
        return saved;
    }

    @Transactional
    public Supplier update(Long id, SupplierRequest req, Long actorId) {
        Supplier supplier = getById(id);
        String old = supplier.getCompanyName();
        supplier.setCompanyName(req.getCompanyName());
        supplier.setContactPerson(req.getContactPerson());
        supplier.setPhone(req.getPhone());
        supplier.setEmail(req.getEmail());
        supplier.setAddress(req.getAddress());
        supplier.setTaxNumber(req.getTaxNumber());
        if (StringUtils.hasText(req.getStatus())) {
            supplier.setStatus(req.getStatus().toUpperCase());
        }
        supplier.setNotes(req.getNotes());
        Supplier saved = supplierRepository.save(supplier);
        auditLogService.log("SUPPLIER_UPDATE", "Supplier", String.valueOf(id), old, saved.getCompanyName(), actorId, null, null);
        return saved;
    }

    @Transactional
    public void delete(Long id, Long actorId) {
        // Soft delete: keep historical purchase orders intact
        changeStatus(id, "INACTIVE", actorId);
    }

    @Transactional
    public Supplier changeStatus(Long id, String status, Long actorId) {
        Supplier supplier = getById(id);
        String old = supplier.getStatus();
        String next = StringUtils.hasText(status) ? status.toUpperCase() : "ACTIVE";
        supplier.setStatus(next);
        Supplier saved = supplierRepository.save(supplier);
        auditLogService.log("SUPPLIER_STATUS", "Supplier", String.valueOf(id), old, next, actorId, null, null);
        return saved;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> purchaseHistory(Long supplierId) {
        Supplier supplier = getById(supplierId);
        var orders = purchaseOrderRepository.findBySupplierId(supplier.getId());
        BigDecimal total = orders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Object lastDate = orders.stream()
                .map(PurchaseOrder::getOrderDate)
                .filter(d -> d != null)
                .max(java.time.LocalDateTime::compareTo)
                .orElse(null);
        Map<String, Object> result = new HashMap<>();
        result.put("supplier", supplier);
        result.put("orders", orders);
        result.put("total", total);
        result.put("count", orders.size());
        result.put("lastOrderDate", lastDate);
        return result;
    }
}
