package com.guruge.hardware.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String users(Model model) {
        model.addAttribute("page", "admin-users");
        return "admin/users";
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public String products(Model model) {
        model.addAttribute("page", "admin-products");
        return "admin/products";
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public String categories(Model model) {
        model.addAttribute("page", "admin-categories");
        return "admin/categories";
    }

    @GetMapping("/suppliers")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public String suppliers(Model model) {
        model.addAttribute("page", "admin-suppliers");
        return "admin/suppliers";
    }

    @GetMapping("/purchases")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public String purchases(Model model) {
        model.addAttribute("page", "admin-purchases");
        return "admin/purchases";
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public String sales(Model model) {
        model.addAttribute("page", "admin-sales");
        return "admin/sales";
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public String reports(Model model) {
        model.addAttribute("page", "admin-reports");
        return "admin/reports";
    }

    @GetMapping("/requests")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public String requests(Model model) {
        model.addAttribute("page", "admin-requests");
        return "admin/requests";
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public String auditLogs(Model model) {
        model.addAttribute("page", "admin-audit-logs");
        return "admin/audit-logs";
    }
}
