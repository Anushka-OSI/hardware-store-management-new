package com.guruge.hardware.service;

import com.guruge.hardware.dto.request.SaleRequest;
import com.guruge.hardware.dto.response.SaleResponse;
import com.guruge.hardware.entity.Customer;
import com.guruge.hardware.entity.Payment;
import com.guruge.hardware.entity.Product;
import com.guruge.hardware.entity.Sale;
import com.guruge.hardware.entity.SaleItem;
import com.guruge.hardware.exception.BusinessException;
import com.guruge.hardware.exception.InsufficientStockException;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.CustomerRepository;
import com.guruge.hardware.repository.PaymentRepository;
import com.guruge.hardware.repository.ProductRepository;
import com.guruge.hardware.repository.SaleRepository;
import com.guruge.hardware.util.MoneyUtils;
import com.guruge.hardware.util.NumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final StockMovementService stockMovementService;
    private final NumberGenerator numberGenerator;
    private final AuditLogService auditLogService;

    @Transactional
    public SaleResponse completeSale(SaleRequest req, Long cashierId) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BusinessException("Sale must contain at least one item");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<SaleItem> items = new ArrayList<>();

        // lock + validate products first
        for (SaleRequest.SaleItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findByIdForUpdate(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));
            if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
                throw new BusinessException("Product is not ACTIVE: " + product.getName());
            }
            int available = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
            if (available < itemReq.getQuantity()) {
                throw new InsufficientStockException(product.getName(), available, itemReq.getQuantity());
            }
            BigDecimal unitPrice = itemReq.getUnitPriceOverride() != null
                    ? itemReq.getUnitPriceOverride()
                    : product.getSellingPrice();
            BigDecimal lineDiscount = itemReq.getDiscountAmount() != null ? itemReq.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal lineTotal = MoneyUtils.sub(MoneyUtils.mul(unitPrice, itemReq.getQuantity()), lineDiscount);

            SaleItem item = SaleItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(MoneyUtils.round(unitPrice))
                    .discountAmount(MoneyUtils.round(lineDiscount))
                    .discountType(itemReq.getDiscountType())
                    .lineTotal(lineTotal)
                    .costPriceAtSale(product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO)
                    .build();
            items.add(item);
            subtotal = MoneyUtils.add(subtotal, lineTotal);
        }

        String discountType = req.getDiscountType() != null ? req.getDiscountType().toUpperCase() : "NONE";
        BigDecimal discountInput = req.getDiscountAmount() != null ? req.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount;
        if ("PERCENT".equals(discountType)) {
            if (discountInput.compareTo(BigDecimal.ZERO) < 0 || discountInput.compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException("Discount percent must be between 0 and 100");
            }
            discountAmount = MoneyUtils.percentage(subtotal, discountInput);
        } else if ("FIXED".equals(discountType)) {
            if (discountInput.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Discount amount must be >= 0");
            }
            if (discountInput.compareTo(subtotal) > 0) {
                throw new BusinessException("Discount amount cannot exceed subtotal");
            }
            discountAmount = discountInput;
        } else {
            discountAmount = BigDecimal.ZERO;
        }
        BigDecimal total = MoneyUtils.sub(subtotal, discountAmount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Total cannot be negative");
        }
        if (req.getAmountPaid() == null || req.getAmountPaid().compareTo(total) < 0) {
            throw new BusinessException("Payment amount (" + req.getAmountPaid() + ") is less than total (" + total + ")");
        }
        BigDecimal balance = MoneyUtils.sub(req.getAmountPaid(), total);

        Customer customer = null;
        if (req.getCustomerId() != null) {
            customer = customerRepository.findById(req.getCustomerId()).orElse(null);
        }

        Sale sale = Sale.builder()
                .receiptNumber(numberGenerator.generateReceiptNumber())
                .customer(customer)
                .customerName(req.getCustomerName() != null ? req.getCustomerName()
                        : (customer != null ? customer.getName() : null))
                .customerPhone(req.getCustomerPhone() != null ? req.getCustomerPhone()
                        : (customer != null ? customer.getPhone() : null))
                .cashierId(cashierId)
                .subtotal(subtotal)
                .discountAmount(MoneyUtils.round(discountAmount))
                .discountType(req.getDiscountType())
                .totalAmount(total)
                .paymentStatus("PAID")
                .saleStatus("COMPLETED")
                .notes(req.getNotes())
                .build();
        for (SaleItem item : items) {
            item.setSale(sale);
        }
        sale.getItems().addAll(items);
        Sale saved = saleRepository.save(sale);

        Payment payment = Payment.builder()
                .sale(saved)
                .paymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod().toUpperCase() : "CASH")
                .amountPaid(MoneyUtils.round(req.getAmountPaid()))
                .balance(MoneyUtils.round(balance))
                .transactionRef(req.getTransactionRef())
                .status("PAID")
                .createdBy(cashierId)
                .build();
        paymentRepository.save(payment);
        saved.getPayments().add(payment);

        // decrease stock + txn SALE
        for (SaleItem item : saved.getItems()) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", item.getProduct().getId()));
            int prev = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
            int next = prev - item.getQuantity();
            if (next < 0) {
                throw new InsufficientStockException(product.getName(), prev, item.getQuantity());
            }
            product.setCurrentStock(next);
            productRepository.save(product);
            stockMovementService.record(product, -item.getQuantity(), prev, next,
                    "SALE", "SALE", saved.getId(), "Sale " + saved.getReceiptNumber(), cashierId);
        }

        auditLogService.log("SALE_COMPLETE", "Sale", String.valueOf(saved.getId()),
                null, saved.getReceiptNumber(), cashierId, null, null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SaleResponse getSaleDetail(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));
        return toResponse(sale);
    }

    @Transactional(readOnly = true)
    public Page<SaleResponse> searchSales(String receipt, LocalDate dateFrom, LocalDate dateTo,
                                          Long cashierId, String paymentMethod, Pageable pageable) {
        Specification<Sale> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(receipt)) {
                predicates.add(cb.like(cb.lower(root.get("receiptNumber")), "%" + receipt.toLowerCase() + "%"));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom.atStartOfDay()));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo.plusDays(1).atStartOfDay()));
            }
            if (cashierId != null) {
                predicates.add(cb.equal(root.get("cashierId"), cashierId));
            }
            predicates.add(cb.notEqual(root.get("saleStatus"), "CANCELLED"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Sale> page = saleRepository.findAll(spec, pageable);
        if (StringUtils.hasText(paymentMethod)) {
            // filter in-memory by payment method since payments is a collection
            List<SaleResponse> filtered = page.getContent().stream()
                    .filter(s -> s.getPayments() != null && s.getPayments().stream()
                            .anyMatch(p -> paymentMethod.equalsIgnoreCase(p.getPaymentMethod())))
                    .map(this::toResponse)
                    .toList();
            return new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
        }
        return page.map(this::toResponse);
    }

    @Transactional
    public SaleResponse cancelSale(Long id, Long actorId) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));
        if ("CANCELLED".equalsIgnoreCase(sale.getSaleStatus())) {
            throw new BusinessException("Sale is already cancelled");
        }
        sale.setSaleStatus("CANCELLED");
        Sale saved = saleRepository.save(sale);
        auditLogService.log("SALE_CANCEL", "Sale", String.valueOf(id), "COMPLETED", "CANCELLED", actorId, null, null);
        return toResponse(saved);
    }

    public SaleResponse toResponse(Sale sale) {
        List<SaleResponse.SaleItemResponse> itemResponses = sale.getItems() != null
                ? sale.getItems().stream().map(i -> SaleResponse.SaleItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .discountAmount(i.getDiscountAmount())
                        .lineTotal(i.getLineTotal())
                        .build()).toList()
                : List.of();
        List<SaleResponse.PaymentResponse> paymentResponses = sale.getPayments() != null
                ? sale.getPayments().stream().map(p -> SaleResponse.PaymentResponse.builder()
                        .id(p.getId())
                        .paymentMethod(p.getPaymentMethod())
                        .amountPaid(p.getAmountPaid())
                        .balance(p.getBalance())
                        .status(p.getStatus())
                        .build()).toList()
                : List.of();
        return SaleResponse.builder()
                .id(sale.getId())
                .receiptNumber(sale.getReceiptNumber())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getId() : null)
                .customerName(sale.getCustomerName())
                .customerPhone(sale.getCustomerPhone())
                .subtotal(sale.getSubtotal())
                .discountAmount(sale.getDiscountAmount())
                .discountType(sale.getDiscountType())
                .totalAmount(sale.getTotalAmount())
                .paymentStatus(sale.getPaymentStatus())
                .saleStatus(sale.getSaleStatus())
                .createdAt(sale.getCreatedAt())
                .items(itemResponses)
                .payments(paymentResponses)
                .build();
    }
}
