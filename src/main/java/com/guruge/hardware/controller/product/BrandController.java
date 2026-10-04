package com.guruge.hardware.controller.product;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.entity.Brand;
import com.guruge.hardware.security.UserPrincipal;
import com.guruge.hardware.service.BrandService;
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

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ApiResponse<Page<Brand>>> list(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(brandService.list(keyword, pageable)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Brand>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(brandService.listActive()));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<Brand>> create(
            @RequestBody BrandRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Brand created",
                brandService.create(req.getName(), req.getDescription(), req.getLogoUrl(), currentUserId(principal))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ApiResponse<Brand>> getById(@PathVariable Long id) {
        return brandService.list(null, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                .stream().filter(b -> b.getId().equals(id)).findFirst()
                .map(b -> ResponseEntity.ok(ApiResponse.ok(b)))
                .orElseThrow(() -> new com.guruge.hardware.exception.ResourceNotFoundException("Brand", "id", id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<Brand>> update(
            @PathVariable Long id,
            @RequestBody BrandRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Brand updated",
                brandService.update(id, req.getName(), req.getDescription(), req.getLogoUrl(), currentUserId(principal))));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<Brand>> setActive(
            @PathVariable Long id,
            @RequestBody ActiveRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Brand status updated",
                brandService.setActive(id, req.isActive(), currentUserId(principal))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        brandService.delete(id, currentUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("Brand deleted", null));
    }

    private Long currentUserId(UserPrincipal principal) {
        return principal != null ? principal.getId() : null;
    }

    @Data
    public static class BrandRequest {
        @NotBlank private String name;
        private String description;
        private String logoUrl;
    }

    @Data
    public static class ActiveRequest {
        private boolean active;
    }
}
