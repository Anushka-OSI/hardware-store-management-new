package com.guruge.hardware.service;

import com.guruge.hardware.dto.response.DashboardStats;
import com.guruge.hardware.entity.Payment;
import com.guruge.hardware.entity.Product;
import com.guruge.hardware.entity.PurchaseOrder;
import com.guruge.hardware.entity.Sale;
import com.guruge.hardware.repository.CustomerRequestRepository;
import com.guruge.hardware.repository.PaymentRepository;
import com.guruge.hardware.repository.ProductRepository;
import com.guruge.hardware.repository.PurchaseOrderRepository;
import com.guruge.hardware.repository.SaleRepository;
import com.guruge.hardware.repository.UserRepository;
import com.guruge.hardware.util.MoneyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final CustomerRequestRepository customerRequestRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminDashboard() {
        long totalProducts = productRepository.count();
        BigDecimal stockValue = productRepository.findAll().stream()
                .map(p -> {
                    BigDecimal cost = p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO;
                    int qty = p.getCurrentStock() != null ? p.getCurrentStock() : 0;
                    return cost.multiply(BigDecimal.valueOf(qty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long lowStockCount = productRepository.findLowStockProducts().size();
        long outOfStock = productRepository.findAll().stream()
                .filter(p -> p.getCurrentStock() == null || p.getCurrentStock() <= 0).count();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        List<Sale> todaySalesList = saleRepository.findByCreatedAtBetween(startOfDay, endOfDay).stream()
                .filter(s -> !"CANCELLED".equalsIgnoreCase(s.getSaleStatus())).toList();
        BigDecimal todayRevenue = todaySalesList.stream()
                .map(Sale::getTotalAmount).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        List<Sale> monthSales = saleRepository.findByCreatedAtBetween(startOfMonth, LocalDateTime.now()).stream()
                .filter(s -> !"CANCELLED".equalsIgnoreCase(s.getSaleStatus())).toList();
        BigDecimal monthlyRevenue = monthSales.stream()
                .map(Sale::getTotalAmount).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingPOs = purchaseOrderRepository.findByStatus("DRAFT").size()
                + purchaseOrderRepository.findByStatus("SENT").size()
                + purchaseOrderRepository.findByStatus("CONFIRMED").size();
        long pendingRequests = customerRequestRepository.findByStatus("NEW").size()
                + customerRequestRepository.findByStatus("IN_PROGRESS").size();
        long activeStaff = userRepository.findByStatus("ACTIVE").size();

        // daily sales last 7 days
        List<Map<String, Object>> dailySales = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            List<Sale> daySales = saleRepository.findByCreatedAtBetween(d.atStartOfDay(), d.plusDays(1).atStartOfDay());
            BigDecimal rev = daySales.stream()
                    .filter(s -> !"CANCELLED".equalsIgnoreCase(s.getSaleStatus()))
                    .map(Sale::getTotalAmount).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> row = new HashMap<>();
            row.put("date", d.format(fmt));
            row.put("count", daySales.size());
            row.put("revenue", rev);
            dailySales.add(row);
        }

        // payment breakdown
        Map<String, BigDecimal> paymentBreakdown = new HashMap<>();
        for (Sale s : monthSales) {
            List<Payment> payments = paymentRepository.findBySaleId(s.getId());
            for (Payment p : payments) {
                paymentBreakdown.merge(p.getPaymentMethod(), p.getAmountPaid(), BigDecimal::add);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalProducts", totalProducts);
        result.put("stockValue", stockValue);
        result.put("lowStockCount", lowStockCount);
        result.put("outOfStock", outOfStock);
        result.put("todaySales", todaySalesList.size());
        result.put("todayRevenue", todayRevenue);
        result.put("monthlySales", monthSales.size());
        result.put("monthlyRevenue", monthlyRevenue);
        result.put("pendingPOs", pendingPOs);
        result.put("pendingRequests", pendingRequests);
        result.put("activeStaff", activeStaff);
        result.put("dailySales", dailySales);
        result.put("paymentBreakdown", paymentBreakdown);
        result.put("stats", DashboardStats.builder()
                .todaySales(todayRevenue)
                .todayOrders(todaySalesList.size())
                .monthRevenue(monthlyRevenue)
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStock)
                .totalProducts(totalProducts)
                .pendingPurchaseOrders(pendingPOs)
                .newCustomerRequests(pendingRequests)
                .build());
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> inventoryDashboard() {
        List<Product> all = productRepository.findAll();
        BigDecimal valuation = all.stream()
                .map(p -> MoneyUtils.mul(p.getCostPrice(), p.getCurrentStock() != null ? p.getCurrentStock() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> result = new HashMap<>();
        result.put("totalProducts", all.size());
        result.put("valuation", valuation);
        result.put("lowStock", productRepository.findLowStockProducts());
        result.put("outOfStockCount", all.stream().filter(p -> p.getCurrentStock() == null || p.getCurrentStock() <= 0).count());
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> cashierDashboard(Long cashierId) {
        LocalDate today = LocalDate.now();
        List<Sale> sales = saleRepository.findByCashierId(cashierId).stream()
                .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().toLocalDate().equals(today))
                .filter(s -> !"CANCELLED".equalsIgnoreCase(s.getSaleStatus()))
                .toList();
        BigDecimal revenue = sales.stream().map(Sale::getTotalAmount)
                .filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> result = new HashMap<>();
        result.put("todayCount", sales.size());
        result.put("todayRevenue", revenue);
        result.put("sales", sales);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> supplierDashboard(Long supplierId) {
        List<PurchaseOrder> orders = purchaseOrderRepository.findBySupplierId(supplierId);
        Map<String, Long> byStatus = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus() != null ? o.getStatus() : "UNKNOWN", Collectors.counting()));
        Map<String, Object> result = new HashMap<>();
        result.put("total", orders.size());
        result.put("pending", byStatus.getOrDefault("DRAFT", 0L) + byStatus.getOrDefault("SENT", 0L) + byStatus.getOrDefault("CONFIRMED", 0L));
        result.put("delivered", byStatus.getOrDefault("RECEIVED", 0L));
        result.put("byStatus", byStatus);
        result.put("history", orders);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> salesReport(LocalDate dateFrom, LocalDate dateTo, String groupBy) {
        LocalDateTime from = (dateFrom != null ? dateFrom : LocalDate.now().minusDays(30)).atStartOfDay();
        LocalDateTime to = (dateTo != null ? dateTo.plusDays(1) : LocalDate.now().plusDays(1)).atStartOfDay();
        List<Sale> sales = saleRepository.findByCreatedAtBetween(from, to).stream()
                .filter(s -> !"CANCELLED".equalsIgnoreCase(s.getSaleStatus())).toList();
        BigDecimal revenue = sales.stream().map(Sale::getTotalAmount)
                .filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, List<Sale>> grouped = new LinkedHashMap<>();
        DateTimeFormatter fmt = "month".equalsIgnoreCase(groupBy)
                ? DateTimeFormatter.ofPattern("yyyy-MM")
                : DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Sale s : sales) {
            String key = s.getCreatedAt() != null ? s.getCreatedAt().format(fmt) : "unknown";
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("count", sales.size());
        result.put("revenue", revenue);
        result.put("grouped", grouped);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> inventoryReport() {
        return inventoryDashboard();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> purchaseReport(LocalDate dateFrom, LocalDate dateTo) {
        LocalDateTime from = (dateFrom != null ? dateFrom : LocalDate.now().minusDays(30)).atStartOfDay();
        LocalDateTime to = (dateTo != null ? dateTo.plusDays(1) : LocalDate.now().plusDays(1)).atStartOfDay();
        List<PurchaseOrder> all = purchaseOrderRepository.findAll().stream()
                .filter(po -> po.getOrderDate() != null && !po.getOrderDate().isBefore(from) && po.getOrderDate().isBefore(to))
                .toList();
        BigDecimal total = all.stream().map(PurchaseOrder::getTotalAmount)
                .filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> result = new HashMap<>();
        result.put("count", all.size());
        result.put("total", total);
        result.put("orders", all);
        return result;
    }
}
