package com.guruge.hardware.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {

    private String receiptNumber;
    private String storeName;
    private String customerName;
    private String customerPhone;
    private String cashierName;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balance;
    private String paymentMethod;
    private LocalDateTime createdAt;
}
