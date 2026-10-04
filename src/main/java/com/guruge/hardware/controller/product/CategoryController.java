package com.guruge.hardware.controller.product;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.dto.request.CategoryRequest;
import com.guruge.hardware.entity.Category;
import com.guruge.hardware.security.UserPrincipal;
import com.guruge.hardware.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ApiResponse<Page<Category>>> list(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.list(keyword, pageable)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Category>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.listActive()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ApiResponse<Category>> getById(@PathVariable Long id) {
        return categoryService.list(null, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                .stream().filter(c -> c.getId().equals(id)).findFirst()
                .map(c -> ResponseEntity.ok(ApiResponse.ok(c)))
                .orElseThrow(() -> new com.guruge.hardware.exception.ResourceNotFoundException("Category", "id", id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<Category>> create(
            @Valid @RequestBody CategoryRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Category created",
                categoryService.create(req, currentUserId(principal))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<Category>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Category updated",
                categoryService.update(id, req, currentUserId(principal))));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<Category>> setActive(
            @PathVariable Long id,
            @RequestBody ActiveRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Category status updated",
                categoryService.setActive(id, req.isActive(), currentUserId(principal))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        categoryService.delete(id, currentUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("Category deleted", null));
    }

    private Long currentUserId(UserPrincipal principal) {
        return principal != null ? principal.getId() : null;
    }

    @Data
    public static class ActiveRequest {
        private boolean active;
    }
}
