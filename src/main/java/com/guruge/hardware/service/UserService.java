package com.guruge.hardware.service;

import com.guruge.hardware.dto.response.UserResponse;
import com.guruge.hardware.entity.Role;
import com.guruge.hardware.entity.User;
import com.guruge.hardware.exception.BusinessException;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.RoleRepository;
import com.guruge.hardware.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<UserResponse> list(String keyword, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("username")), like),
                    cb.like(cb.lower(root.get("fullName")), like),
                    cb.like(cb.lower(root.get("email")), like));
        };
        return userRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public UserResponse create(String username, String email, String rawPassword,
                               String fullName, String phone, String address,
                               String roleName, Long actorId) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Username already exists: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email already exists: " + email);
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
        String employeeId = "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        while (userRepository.existsByEmployeeId(employeeId)) {
            employeeId = "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        User user = User.builder()
                .employeeId(employeeId)
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .phone(phone)
                .address(address)
                .role(role)
                .status("ACTIVE")
                .build();
        User saved = userRepository.save(user);
        auditLogService.log("USER_CREATE", "User", String.valueOf(saved.getId()),
                null, saved.getUsername(), actorId, null, null);
        return toResponse(saved);
    }

    @Transactional
    public UserResponse update(Long id, String fullName, String phone, String address, String email, Long actorId) {
        User user = getEntity(id);
        String oldVal = user.getFullName() + "|" + user.getEmail();
        if (StringUtils.hasText(email) && !email.equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new BusinessException("Email already exists: " + email);
            }
            user.setEmail(email);
        }
        if (StringUtils.hasText(fullName)) {
            user.setFullName(fullName);
        }
        user.setPhone(phone);
        user.setAddress(address);
        User saved = userRepository.save(user);
        auditLogService.log("USER_UPDATE", "User", String.valueOf(id),
                oldVal, saved.getFullName() + "|" + saved.getEmail(), actorId, null, null);
        return toResponse(saved);
    }

    @Transactional
    public UserResponse changeStatus(Long id, String status, Long actorId) {
        User user = getEntity(id);
        if (actorId != null && actorId.equals(id) && !"ACTIVE".equalsIgnoreCase(status)) {
            throw new BusinessException("You cannot deactivate or lock your own account");
        }
        if (!"ACTIVE".equalsIgnoreCase(status) && isLastActiveAdmin(user)) {
            throw new BusinessException("Cannot deactivate the last active admin user");
        }
        String old = user.getStatus();
        user.setStatus(status);
        User saved = userRepository.save(user);
        auditLogService.log("USER_STATUS", "User", String.valueOf(id), old, status, actorId, null, null);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id, Long actorId) {
        // Hard delete: only succeeds when the user has NO history
        // (sales, receipts, adjustments, audit entries reference users via FK).
        // Otherwise a 409 is returned and the caller should deactivate instead.
        User user = getEntity(id);
        if (actorId != null && actorId.equals(id)) {
            throw new BusinessException("You cannot delete your own account");
        }
        if (isLastActiveAdmin(user)) {
            throw new BusinessException("Cannot delete the last active admin user");
        }
        userRepository.delete(user);
        auditLogService.log("USER_DELETE", "User", String.valueOf(id), user.getUsername(), null, actorId, null, null);
    }

    @Transactional
    public UserResponse changeRole(Long id, String roleName, Long actorId) {
        User user = getEntity(id);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
        String oldRole = user.getRole() != null ? user.getRole().getName() : null;
        user.setRole(role);
        User saved = userRepository.save(user);
        auditLogService.log("USER_ROLE", "User", String.valueOf(id), oldRole, roleName, actorId, null, null);
        return toResponse(saved);
    }

    private boolean isLastActiveAdmin(User user) {
        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            return false;
        }
        List<User> admins = userRepository.findByRoleId(user.getRole().getId());
        long activeAdmins = admins.stream()
                .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                .count();
        return activeAdmins <= 1;
    }

    private User getEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .permissions(user.getRole() != null && user.getRole().getPermissions() != null
                        ? user.getRole().getPermissions().stream()
                                .filter(p -> p != null && p.getCode() != null)
                                .map(p -> p.getCode())
                                .collect(Collectors.toList())
                        : List.of())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
