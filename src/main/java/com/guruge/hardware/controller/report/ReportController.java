package com.guruge.hardware.controller.report;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.security.UserPrincipal;
import com.guruge.hardware.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard/admin")
    @PreAuthorize("hasAnyAuthority('report:sales','report:inventory','report:purchase','report:payment')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getAdminDashboard()));
    }

    @GetMapping("/dashboard/inventory")
    @PreAuthorize("hasAnyAuthority('report:sales','report:inventory','report:purchase','report:payment')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> inventoryDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.inventoryDashboard()));
    }

    @GetMapping("/dashboard/cashier")
    @PreAuthorize("hasAnyAuthority('report:sales','report:inventory','report:purchase','report:payment')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cashierDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long cashierId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok(reportService.cashierDashboard(cashierId)));
    }

    @GetMapping("/dashboard/supplier")
    @PreAuthorize("hasAnyAuthority('report:sales','report:inventory','report:purchase','report:payment')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> supplierDashboard(
            @RequestParam(required = false) Long supplierId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long effectiveId;
        if (principal != null && "SUPPLIER".equalsIgnoreCase(principal.getRoleName())) {
            if (principal.getSupplierId() == null) {
                throw new com.guruge.hardware.exception.BusinessException(
                        "Your login is not linked to a supplier account. Contact the administrator.");
            }
            effectiveId = principal.getSupplierId();
        } else {
            effectiveId = supplierId;
        }
        return ResponseEntity.ok(ApiResponse.ok(reportService.supplierDashboard(effectiveId)));
    }

    @GetMapping("/sales/daily")
    @PreAuthorize("hasAnyAuthority('report:sales','report:inventory','report:purchase','report:payment')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> salesDaily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.salesReport(dateFrom, dateTo, groupBy)));
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyAuthority('report:sales','report:inventory','report:purchase','report:payment')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.salesReport(dateFrom, dateTo, groupBy)));
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyAuthority('report:sales','report:inventory','report:purchase','report:payment')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> inventory() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.inventoryReport()));
    }

    @GetMapping("/purchases")
    @PreAuthorize("hasAnyAuthority('report:sales','report:inventory','report:purchase','report:payment')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> purchases(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.purchaseReport(dateFrom, dateTo)));
    }
}
