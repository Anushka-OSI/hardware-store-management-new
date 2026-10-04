package com.guruge.hardware.controller.customer;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.dto.request.CustomerRequestRespond;
import com.guruge.hardware.dto.request.CustomerRequestSubmit;
import com.guruge.hardware.entity.CustomerRequest;
import com.guruge.hardware.security.UserPrincipal;
import com.guruge.hardware.service.CustomerRequestService;
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

@RestController
@RequiredArgsConstructor
public class CustomerRequestController {

    private final CustomerRequestService customerRequestService;

    // ---- Public endpoints ----

    @PostMapping("/api/public/requests")
    public ResponseEntity<ApiResponse<CustomerRequest>> submit(
            @Valid @RequestBody CustomerRequestSubmit req) {
        return ResponseEntity.ok(ApiResponse.ok("Request submitted", customerRequestService.submit(req)));
    }

    @GetMapping("/api/public/requests/check")
    public ResponseEntity<ApiResponse<CustomerRequest>> check(
            @RequestParam String requestId,
            @RequestParam String phone) {
        return ResponseEntity.ok(ApiResponse.ok(customerRequestService.checkStatus(requestId, phone)));
    }

    // ---- Admin endpoints ----

    @GetMapping("/api/requests")
    @PreAuthorize("hasAuthority('request:read')")
    public ResponseEntity<ApiResponse<Page<CustomerRequest>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(customerRequestService.list(status, keyword, pageable)));
    }

    @GetMapping("/api/requests/{id}")
    @PreAuthorize("hasAuthority('request:read')")
    public ResponseEntity<ApiResponse<CustomerRequest>> getById(@PathVariable Long id) {
        return customerRequestService.list(null, null,
                        org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                .stream().filter(r -> r.getId().equals(id)).findFirst()
                .map(r -> ResponseEntity.ok(ApiResponse.ok(r)))
                .orElseThrow(() -> new com.guruge.hardware.exception.ResourceNotFoundException("CustomerRequest", "id", id));
    }

    @PatchMapping("/api/requests/{id}/respond")
    @PreAuthorize("hasAnyAuthority('request:respond','request:close')")
    public ResponseEntity<ApiResponse<CustomerRequest>> respond(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestRespond req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long respondedBy = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok("Response recorded",
                customerRequestService.respond(id, req, respondedBy)));
    }

    @PatchMapping("/api/requests/{id}/status")
    @PreAuthorize("hasAnyAuthority('request:respond','request:close')")
    public ResponseEntity<ApiResponse<CustomerRequest>> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long actorId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                customerRequestService.updateStatus(id, req.getStatus(), actorId)));
    }

    @Data
    public static class StatusRequest {
        private String status;
    }
}
