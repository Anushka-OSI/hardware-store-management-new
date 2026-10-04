package com.guruge.hardware.controller.product;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.entity.Unit;
import com.guruge.hardware.security.UserPrincipal;
import com.guruge.hardware.service.UnitService;
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
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @GetMapping
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ApiResponse<Page<Unit>>> list(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(unitService.list(keyword, pageable)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Unit>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(unitService.listActive()));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<Unit>> create(
            @RequestBody UnitRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Unit created",
                unitService.create(req.getName(), req.getCode(), req.getDescription(), currentUserId(principal))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ApiResponse<Unit>> getById(@PathVariable Long id) {
        return unitService.list(null, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                .stream().filter(u -> u.getId().equals(id)).findFirst()
                .map(u -> ResponseEntity.ok(ApiResponse.ok(u)))
                .orElseThrow(() -> new com.guruge.hardware.exception.ResourceNotFoundException("Unit", "id", id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<Unit>> update(
            @PathVariable Long id,
            @RequestBody UnitRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Unit updated",
                unitService.update(id, req.getName(), req.getCode(), req.getDescription(), currentUserId(principal))));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<Unit>> setActive(
            @PathVariable Long id,
            @RequestBody ActiveRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Unit status updated",
                unitService.setActive(id, req.isActive(), currentUserId(principal))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('product:create','product:update','product:delete','product:price:update','category:manage','brand:manage','unit:manage')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        unitService.delete(id, currentUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("Unit deleted", null));
    }

    private Long currentUserId(UserPrincipal principal) {
        return principal != null ? principal.getId() : null;
    }

    @Data
    public static class UnitRequest {
        @NotBlank private String name;
        @NotBlank private String code;
        private String description;
    }

    @Data
    public static class ActiveRequest {
        private boolean active;
    }
}
