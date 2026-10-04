package com.guruge.hardware.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class RoleRedirectHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String target = resolveTargetUrl(authentication);
        response.sendRedirect(request.getContextPath() + target);
    }

    private String resolveTargetUrl(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role == null) {
                continue;
            }
            String normalized = role.startsWith("ROLE_") ? role.substring(5) : role;
            switch (normalized) {
                case "ADMIN":
                    return "/admin/dashboard";
                case "INVENTORY_MANAGER":
                    return "/inventory/dashboard";
                case "CASHIER":
                    return "/cashier/dashboard";
                case "SUPPLIER":
                    return "/supplier/dashboard";
                default:
                    break;
            }
        }
        return "/dashboard";
    }
}
