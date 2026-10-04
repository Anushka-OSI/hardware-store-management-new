package com.guruge.hardware.controller.sales;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.dto.request.ReturnRequest;
import com.guruge.hardware.entity.ReturnEntity;
import com.guruge.hardware.repository.ReturnRepository;
import com.guruge.hardware.security.UserPrincipal;
import com.guruge.hardware.service.ReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;
    private final ReturnRepository returnRepository;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('sale:return','sale:refund')")
    public ResponseEntity<ApiResponse<ReturnEntity>> create(
            @Valid @RequestBody ReturnRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long processedBy = principal != null ? principal.getId() : null;
        ReturnEntity created = returnService.processReturn(
                req.getSaleId(), req.getItems(), req.getReason(), processedBy);
        return ResponseEntity.ok(ApiResponse.ok("Return processed", created));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('sale:read')")
    public ResponseEntity<ApiResponse<Page<ReturnEntity>>> list(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ReturnEntity> page;
        if (status != null && !status.isBlank()) {
            java.util.List<ReturnEntity> filtered = returnRepository.findByStatus(status.toUpperCase());
            int start = (int) Math.min(pageable.getOffset(), filtered.size());
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            page = new org.springframework.data.domain.PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
        } else {
            page = returnRepository.findAll(pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sale:read')")
    public ResponseEntity<ApiResponse<ReturnEntity>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(returnService.getById(id)));
    }
}
