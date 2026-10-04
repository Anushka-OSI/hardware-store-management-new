package com.guruge.hardware.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    private BigDecimal todaySales;
    private long todayOrders;
    private BigDecimal monthRevenue;
    private long lowStockCount;
    private long outOfStockCount;
    private long totalProducts;
    private long pendingPurchaseOrders;
    private long newCustomerRequests;
}
