package com.guruge.hardware.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private LocalDate expectedDeliveryDate;
    private String notes;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<PurchaseOrderItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseOrderItemRequest {

        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull
        @Min(value = 1, message = "Ordered quantity must be >= 1")
        private Integer orderedQty;

        @NotNull(message = "Unit cost is required")
        private BigDecimal unitCost;

        private String notes;
    }
}
