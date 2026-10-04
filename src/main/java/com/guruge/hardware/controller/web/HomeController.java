package com.guruge.hardware.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("page", "home");
        return "public/home";
    }

    @GetMapping("/catalogue")
    public String catalogue(Model model) {
        model.addAttribute("page", "catalogue");
        return "public/catalogue";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("productId", id);
        return "public/product-detail";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        model.addAttribute("page", "cart");
        return "public/cart";
    }

    @GetMapping("/request")
    public String request(Model model) {
        model.addAttribute("page", "request");
        return "public/request";
    }

    @GetMapping("/request/status")
    public String requestStatus(Model model) {
        model.addAttribute("page", "request-status");
        return "public/request-status";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                String normalized = role != null && role.startsWith("ROLE_") ? role.substring(5) : role;
                if (normalized == null) {
                    continue;
                }
                switch (normalized) {
                    case "ADMIN":
                        return "redirect:/admin/dashboard";
                    case "INVENTORY_MANAGER":
                        return "redirect:/inventory/dashboard";
                    case "CASHIER":
                        return "redirect:/cashier/dashboard";
                    case "SUPPLIER":
                        return "redirect:/supplier/dashboard";
                    default:
                        break;
                }
            }
        }
        return "redirect:/";
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        model.addAttribute("page", "admin-dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/inventory/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public String inventoryDashboard(Model model) {
        model.addAttribute("page", "inventory-dashboard");
        return "inventory/dashboard";
    }

    @GetMapping("/cashier/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public String cashierDashboard(Model model) {
        model.addAttribute("page", "cashier-dashboard");
        return "cashier/dashboard";
    }

    @GetMapping("/cashier/pos")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public String pos(Model model) {
        model.addAttribute("page", "pos");
        return "cashier/pos";
    }

    @GetMapping("/supplier/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','SUPPLIER')")
    public String supplierDashboard(Model model) {
        model.addAttribute("page", "supplier-dashboard");
        return "supplier/dashboard";
    }
}
