package com.guruge.hardware.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {

    private Long id;
    private String receiptNumber;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private String discountType;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String saleStatus;
    private LocalDateTime createdAt;
    private List<SaleItemResponse> items;
    private List<PaymentResponse> payments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountAmount;
        private BigDecimal lineTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResponse {
        private Long id;
        private String paymentMethod;
        private BigDecimal amountPaid;
        private BigDecimal balance;
        private String status;
    }
}
