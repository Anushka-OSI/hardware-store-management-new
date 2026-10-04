package com.guruge.hardware.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventory")
public class InventoryWebController {

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("page", "inventory-products");
        return "inventory/products";
    }

    @GetMapping("/stock")
    public String stock(Model model) {
        model.addAttribute("page", "inventory-stock");
        return "inventory/stock";
    }

    @GetMapping("/transactions")
    public String transactions(Model model) {
        model.addAttribute("page", "inventory-transactions");
        return "inventory/transactions";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("page", "inventory-reports");
        return "inventory/reports";
    }
}
