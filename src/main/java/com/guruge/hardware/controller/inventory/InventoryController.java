package com.guruge.hardware.controller.inventory;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.entity.InventoryTransaction;
import com.guruge.hardware.entity.Product;
import com.guruge.hardware.security.UserPrincipal;
import com.guruge.hardware.service.InventoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<Page<InventoryTransaction>>> transactions(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getMovements(null, pageable)));
    }

    @GetMapping("/movements/{productId}")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<Page<InventoryTransaction>>> movements(
            @PathVariable Long productId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getMovements(productId, pageable)));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyAuthority('inventory:adjust','inventory:transfer')")
    public ResponseEntity<ApiResponse<Product>> adjust(
            @RequestBody AdjustRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        Product product = inventoryService.adjustStock(req.getProductId(), req.getQtyChange(), req.getReason(), userId);
        return ResponseEntity.ok(ApiResponse.ok("Stock adjusted", product));
    }

    @GetMapping("/valuation")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<BigDecimal>> valuation() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getValuation()));
    }

    @Data
    public static class AdjustRequest {
        private Long productId;
        private int qtyChange;
        private String reason;
    }
}
