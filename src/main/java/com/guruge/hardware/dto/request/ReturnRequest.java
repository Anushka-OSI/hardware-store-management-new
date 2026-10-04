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
public class ReturnRequest {

    @NotNull(message = "Sale ID is required")
    private Long saleId;

    private String reason;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<ReturnItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnItemRequest {

        @NotNull(message = "Sale item ID is required")
        private Long saleItemId;

        @NotNull
        @Min(value = 1, message = "Returned quantity must be >= 1")
        private Integer returnedQty;

        private String reason;
        private String itemCondition;

        @Builder.Default
        private Boolean restocked = true;
    }
}
