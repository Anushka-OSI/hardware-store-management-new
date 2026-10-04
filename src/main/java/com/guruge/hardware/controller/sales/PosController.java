package com.guruge.hardware.controller.sales;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.dto.request.SaleRequest;
import com.guruge.hardware.dto.response.ReceiptResponse;
import com.guruge.hardware.dto.response.SaleResponse;
import com.guruge.hardware.security.UserPrincipal;
import com.guruge.hardware.service.ReceiptService;
import com.guruge.hardware.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class PosController {

    private final SaleService saleService;
    private final ReceiptService receiptService;

    @PostMapping
    @PreAuthorize("hasAuthority('sale:create')")
    public ResponseEntity<ApiResponse<SaleResponse>> completeSale(
            @Valid @RequestBody SaleRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long cashierId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok("Sale completed", saleService.completeSale(req, cashierId)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('sale:read')")
    public ResponseEntity<ApiResponse<Page<SaleResponse>>> list(
            @RequestParam(required = false) String receipt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long cashierId,
            @RequestParam(required = false) String paymentMethod,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                saleService.searchSales(receipt, dateFrom, dateTo, cashierId, paymentMethod, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sale:read')")
    public ResponseEntity<ApiResponse<SaleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(saleService.getSaleDetail(id)));
    }

    @GetMapping("/{id}/receipt")
    @PreAuthorize("hasAuthority('sale:read')")
    public ResponseEntity<ApiResponse<ReceiptResponse>> receipt(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(receiptService.receiptData(id)));
    }

    @GetMapping("/{id}/receipt.pdf")
    @PreAuthorize("hasAuthority('sale:read')")
    public ResponseEntity<byte[]> receiptPdf(@PathVariable Long id) {
        byte[] pdf = receiptService.receiptPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
