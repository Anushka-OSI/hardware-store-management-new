package com.guruge.hardware.controller.product;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.dto.request.ProductRequest;
import com.guruge.hardware.dto.response.ProductResponse;
import com.guruge.hardware.security.UserPrincipal;
import com.guruge.hardware.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String availability,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                productService.search(keyword, categoryId, brandId, status, minPrice, maxPrice, availability, pageable)));
    }

    @GetMapping("/next-sku")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<String>> nextSku(
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(ApiResponse.ok(productService.generateSku(categoryId)));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> lowStock() {
        return ResponseEntity.ok(ApiResponse.ok(productService.lowStock()));
    }

    @GetMapping("/out-of-stock")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> outOfStock() {
        return ResponseEntity.ok(ApiResponse.ok(productService.outOfStock()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProductResponse created = productService.create(req, currentUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("Product created", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Product updated", productService.update(id, req, currentUserId(principal))));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProductResponse current = productService.getById(id);
        ProductRequest update = ProductRequest.builder()
                .sku(current.getSku())
                .name(current.getName())
                .categoryId(current.getCategoryId())
                .unitId(current.getUnitId())
                .brandId(current.getBrandId())
                .sellingPrice(current.getSellingPrice())
                .status(req.getStatus())
                .build();
        // Preserve required fields for validation-safe update
        return ResponseEntity.ok(ApiResponse.ok("Product status updated",
                productService.update(id, update, currentUserId(principal))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        productService.delete(id, currentUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("Product deleted", null));
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        String path = productService.uploadImage(id, file, currentUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("Image uploaded", path));
    }

    private Long currentUserId(UserPrincipal principal) {
        return principal != null ? principal.getId() : null;
    }

    @Data
    public static class StatusRequest {
        @NotBlank private String status;
    }
}
