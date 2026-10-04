package com.guruge.hardware.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cashier")
public class CashierWebController {

    @GetMapping("/sales")
    public String sales(Model model) {
        model.addAttribute("page", "cashier-sales");
        return "cashier/sales";
    }

    @GetMapping("/returns")
    public String returns(Model model) {
        model.addAttribute("page", "cashier-returns");
        return "cashier/returns";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("page", "cashier-reports");
        return "cashier/reports";
    }
}
