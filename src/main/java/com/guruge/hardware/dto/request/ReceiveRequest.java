package com.guruge.hardware.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveRequest {

    @NotNull(message = "Purchase order ID is required")
    private Long purchaseOrderId;

    private String notes;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<ReceiveItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiveItemRequest {

        @NotNull(message = "Purchase order item ID is required")
        private Long purchaseOrderItemId;

        @NotNull
        @Min(value = 0, message = "Received quantity must be >= 0")
        private Integer receivedQty;

        @Min(value = 0, message = "Accepted quantity must be >= 0")
        private Integer acceptedQty;

        @Min(value = 0, message = "Damaged quantity must be >= 0")
        private Integer damagedQty;

        private String notes;
    }
}
