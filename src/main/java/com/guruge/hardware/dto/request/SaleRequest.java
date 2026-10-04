package com.guruge.hardware.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleRequest {

    private Long customerId;
    private String customerName;
    private String customerPhone;

    @Builder.Default
    private String discountType = "NONE";

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    private String paymentMethod = "CASH";

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.0", message = "Amount paid must be >= 0")
    private BigDecimal amountPaid;

    private String transactionRef;
    private String notes;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<SaleItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleItemRequest {

        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull
        @Min(value = 1, message = "Quantity must be >= 1")
        private Integer quantity;

        private BigDecimal unitPriceOverride;

        @Builder.Default
        private BigDecimal discountAmount = BigDecimal.ZERO;

        @Builder.Default
        private String discountType = "NONE";
    }
}
