package com.guruge.hardware.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("page", "admin-users");
        return "admin/users";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("page", "admin-products");
        return "admin/products";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("page", "admin-categories");
        return "admin/categories";
    }

    @GetMapping("/suppliers")
    public String suppliers(Model model) {
        model.addAttribute("page", "admin-suppliers");
        return "admin/suppliers";
    }

    @GetMapping("/purchases")
    public String purchases(Model model) {
        model.addAttribute("page", "admin-purchases");
        return "admin/purchases";
    }

    @GetMapping("/sales")
    public String sales(Model model) {
        model.addAttribute("page", "admin-sales");
        return "admin/sales";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("page", "admin-reports");
        return "admin/reports";
    }

    @GetMapping("/requests")
    public String requests(Model model) {
        model.addAttribute("page", "admin-requests");
        return "admin/requests";
    }

    @GetMapping("/audit-logs")
    public String auditLogs(Model model) {
        model.addAttribute("page", "admin-audit-logs");
        return "admin/audit-logs";
    }
}
