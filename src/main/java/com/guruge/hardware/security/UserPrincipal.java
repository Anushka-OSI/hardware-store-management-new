package com.guruge.hardware.security;

import com.guruge.hardware.entity.Permission;
import com.guruge.hardware.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String roleName;
    private List<String> permissions = new ArrayList<>();
    private String status;

    public static UserPrincipal fromUser(User user) {
        UserPrincipal principal = new UserPrincipal();
        principal.setId(user.getId());
        principal.setUsername(user.getUsername());
        principal.setPassword(user.getPasswordHash());
        principal.setFullName(user.getFullName());
        principal.setStatus(user.getStatus());
        if (user.getRole() != null) {
            principal.setRoleName(user.getRole().getName());
            List<String> perms = new ArrayList<>();
            if (user.getRole().getPermissions() != null) {
                for (Permission p : user.getRole().getPermissions()) {
                    if (p != null && p.getCode() != null) {
                        perms.add(p.getCode());
                    }
                }
            }
            principal.setPermissions(perms);
        }
        return principal;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (roleName != null && !roleName.isBlank()) {
            String normalized = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            authorities.add(new SimpleGrantedAuthority(normalized));
        }
        if (permissions != null) {
            for (String perm : permissions) {
                if (perm != null && !perm.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(perm));
                }
            }
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"LOCKED".equalsIgnoreCase(status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
