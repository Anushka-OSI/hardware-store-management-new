package com.guruge.hardware.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/supplier")
@PreAuthorize("hasAnyRole('ADMIN','SUPPLIER')")
public class SupplierWebController {

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("page", "supplier-orders");
        return "supplier/orders";
    }

    @GetMapping("/deliveries")
    public String deliveries(Model model) {
        model.addAttribute("page", "supplier-deliveries");
        return "supplier/deliveries";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("page", "supplier-reports");
        return "supplier/reports";
    }
}
