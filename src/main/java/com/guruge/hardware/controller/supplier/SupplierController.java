package com.guruge.hardware.controller.supplier;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.dto.request.SupplierRequest;
import com.guruge.hardware.entity.Supplier;
import com.guruge.hardware.security.UserPrincipal;
import com.guruge.hardware.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasAuthority('supplier:read')")
    public ResponseEntity<ApiResponse<Page<Supplier>>> search(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(supplierService.search(keyword, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('supplier:create','supplier:update','supplier:delete')")
    public ResponseEntity<ApiResponse<Supplier>> create(
            @Valid @RequestBody SupplierRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier created",
                supplierService.create(req, currentUserId(principal))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier:read')")
    public ResponseEntity<ApiResponse<Supplier>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(supplierService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('supplier:create','supplier:update','supplier:delete')")
    public ResponseEntity<ApiResponse<Supplier>> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier updated",
                supplierService.update(id, req, currentUserId(principal))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('supplier:create','supplier:update','supplier:delete')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        supplierService.delete(id, currentUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("Supplier deactivated", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('supplier:create','supplier:update','supplier:delete')")
    public ResponseEntity<ApiResponse<Supplier>> changeStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier status updated",
                supplierService.changeStatus(id, req.getStatus(), currentUserId(principal))));
    }

    @GetMapping("/{id}/purchase-history")
    @PreAuthorize("hasAuthority('supplier:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> purchaseHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(supplierService.purchaseHistory(id)));
    }

    private Long currentUserId(UserPrincipal principal) {
        return principal != null ? principal.getId() : null;
    }

    @lombok.Data
    public static class StatusRequest {
        private String status;
    }
}
