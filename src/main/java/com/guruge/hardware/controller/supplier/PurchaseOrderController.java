package com.guruge.hardware.controller.supplier;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.dto.request.PurchaseOrderRequest;
import com.guruge.hardware.dto.request.ReceiveRequest;
import com.guruge.hardware.entity.PurchaseOrder;
import com.guruge.hardware.security.UserPrincipal;
import com.guruge.hardware.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    @PreAuthorize("hasAuthority('po:read')")
    public ResponseEntity<ApiResponse<Page<PurchaseOrder>>> list(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(purchaseOrderService.list(supplierId, status, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('po:create','po:update','po:send','po:receive','po:cancel')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> create(
            @Valid @RequestBody PurchaseOrderRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order created",
                purchaseOrderService.create(req, currentUserId(principal))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('po:read')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(purchaseOrderService.getDetail(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('po:create','po:update','po:send','po:receive','po:cancel')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> update(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order updated",
                purchaseOrderService.updateDraft(id, req, currentUserId(principal))));
    }

    @PatchMapping("/{id}/send")
    @PreAuthorize("hasAnyAuthority('po:create','po:update','po:send','po:receive','po:cancel')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> send(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order sent",
                purchaseOrderService.send(id, currentUserId(principal))));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('po:create','po:update','po:send','po:receive','po:cancel')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> confirm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order confirmed",
                purchaseOrderService.confirm(id, currentUserId(principal))));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyAuthority('po:create','po:update','po:send','po:receive','po:cancel')")
    public ResponseEntity<ApiResponse<Object>> receive(
            @PathVariable Long id,
            @Valid @RequestBody ReceiveBody body,
            @AuthenticationPrincipal UserPrincipal principal) {
        Object receipt = purchaseOrderService.receive(id, body.getItems(), body.getNotes(), currentUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("Goods received", receipt));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('po:create','po:update','po:send','po:receive','po:cancel')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order cancelled",
                purchaseOrderService.cancel(id, currentUserId(principal))));
    }

    private Long currentUserId(UserPrincipal principal) {
        return principal != null ? principal.getId() : null;
    }

    @Data
    public static class ReceiveBody {
        private List<ReceiveRequest.ReceiveItemRequest> items;
        private String notes;
    }
}
