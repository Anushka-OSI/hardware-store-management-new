package com.guruge.hardware.controller.auth;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.dto.request.AuthLoginRequest;
import com.guruge.hardware.dto.response.UserResponse;
import com.guruge.hardware.security.UserPrincipal;
import com.guruge.hardware.service.AuthService;
import com.guruge.hardware.service.UserService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody AuthLoginRequest req) {
        AuthService.LoginResult result = authService.login(req.getUsername(), req.getPassword());
        Map<String, Object> data = new HashMap<>();
        data.put("token", result.token());
        data.put("refreshToken", result.refreshToken());
        data.put("user", result.user());
        return ResponseEntity.ok(ApiResponse.ok("Login successful", data));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Stateless JWT: client discards the token. Provided for API symmetry.
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse user = userService.findById(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(principal.getId(), req.getOldPassword(), req.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @Data
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
}
