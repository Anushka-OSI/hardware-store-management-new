package com.guruge.hardware.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "barcode", unique = true, length = 80)
    private String barcode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "short_desc", length = 500)
    private String shortDesc;

    @Column(name = "full_desc", columnDefinition = "TEXT")
    private String fullDesc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Builder.Default
    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;

    @Builder.Default
    @Column(name = "min_stock_level", nullable = false)
    private Integer minStockLevel = 10;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
    @JsonIgnore

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();
    @JsonIgnore

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private List<InventoryTransaction> inventoryTransactions = new ArrayList<>();

    @Transient
    public String getAvailabilityStatus() {
        if (status != null && !"ACTIVE".equalsIgnoreCase(status)) {
            return "INACTIVE";
        }
        int stock = currentStock != null ? currentStock : 0;
        int min = minStockLevel != null ? minStockLevel : 0;
        if (stock <= 0) {
            return "OUT_OF_STOCK";
        }
        if (stock <= min) {
            return "LOW_STOCK";
        }
        return "IN_STOCK";
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (currentStock == null) {
            currentStock = 0;
        }
        if (minStockLevel == null) {
            minStockLevel = 10;
        }
        if (sellingPrice == null) {
            sellingPrice = BigDecimal.ZERO;
        }
        if (costPrice == null) {
            costPrice = BigDecimal.ZERO;
        }
        if (discountPercent == null) {
            discountPercent = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
