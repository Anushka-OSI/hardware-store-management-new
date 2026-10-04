package com.guruge.hardware.controller.admin;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.dto.response.UserResponse;
import com.guruge.hardware.security.UserPrincipal;
import com.guruge.hardware.service.UserService;
import jakarta.validation.constraints.Email;
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

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(userService.list(keyword, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('user:create','user:update','user:delete','user:role:assign')")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @RequestBody CreateUserRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        UserResponse created = userService.create(
                req.getUsername(), req.getEmail(), req.getPassword(),
                req.getFullName(), req.getPhone(), req.getAddress(),
                req.getRoleName(), currentUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("User created", created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('user:create','user:update','user:delete','user:role:assign')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        UserResponse updated = userService.update(
                id, req.getFullName(), req.getPhone(), req.getAddress(),
                req.getEmail(), currentUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("User updated", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('user:create','user:update','user:delete','user:role:assign')")
    public ResponseEntity<ApiResponse<UserResponse>> changeStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        UserResponse updated = userService.changeStatus(id, req.getStatus(), currentUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("User status updated", updated));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAnyAuthority('user:create','user:update','user:delete','user:role:assign')")
    public ResponseEntity<ApiResponse<UserResponse>> changeRole(
            @PathVariable Long id,
            @RequestBody RoleRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        UserResponse updated = userService.changeRole(id, req.getRoleName(), currentUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("User role updated", updated));
    }

    private Long currentUserId(UserPrincipal principal) {
        return principal != null ? principal.getId() : null;
    }

    @Data
    public static class CreateUserRequest {
        @NotBlank private String username;
        @NotBlank @Email private String email;
        @NotBlank private String password;
        private String fullName;
        private String phone;
        private String address;
        @NotBlank private String roleName;
    }

    @Data
    public static class UpdateUserRequest {
        private String fullName;
        private String phone;
        private String address;
        private String email;
    }

    @Data
    public static class StatusRequest {
        @NotBlank private String status;
    }

    @Data
    public static class RoleRequest {
        @NotBlank private String roleName;
    }
}
