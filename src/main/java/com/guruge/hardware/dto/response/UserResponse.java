package com.guruge.hardware.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String employeeId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String roleName;
    private List<String> permissions;
    private String status;
    private LocalDateTime lastLoginAt;
}
