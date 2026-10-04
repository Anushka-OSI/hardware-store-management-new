package com.guruge.hardware.service;

import com.guruge.hardware.dto.response.UserResponse;
import com.guruge.hardware.entity.User;
import com.guruge.hardware.exception.BusinessException;
import com.guruge.hardware.repository.UserRepository;
import com.guruge.hardware.security.JwtService;
import com.guruge.hardware.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public record LoginResult(String token, String refreshToken, UserResponse user) {
    }

    @Transactional
    public LoginResult login(String username, String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        User user = userRepository.findByUsername(principal.getUsername())
                .or(() -> userRepository.findByEmail(principal.getUsername()))
                .orElseThrow(() -> new BusinessException("User not found after authentication"));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);
        return new LoginResult(token, refreshToken, toResponse(user));
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found: " + userId));
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        com.guruge.hardware.util.PasswordPolicy.validate(user.getUsername(), newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserResponse toResponse(User user) {
        String roleName = user.getRole() != null ? user.getRole().getName() : null;
        return UserResponse.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roleName(roleName)
                .permissions(user.getRole() != null && user.getRole().getPermissions() != null
                        ? user.getRole().getPermissions().stream()
                                .filter(p -> p != null && p.getCode() != null)
                                .map(p -> p.getCode())
                                .collect(Collectors.toList())
                        : java.util.List.of())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
