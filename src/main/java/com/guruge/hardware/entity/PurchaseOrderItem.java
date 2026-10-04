package com.guruge.hardware.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @JsonIgnore

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "ordered_qty", nullable = false)
    private Integer orderedQty;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "total_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Builder.Default
    @Column(name = "received_qty", nullable = false)
    private Integer receivedQty = 0;

    @Builder.Default
    @Column(name = "damaged_qty", nullable = false)
    private Integer damagedQty = 0;

    @Column(name = "notes", length = 500)
    private String notes;
    @JsonIgnore

    @OneToMany(mappedBy = "purchaseOrderItem", fetch = FetchType.LAZY)
    @Builder.Default
    private List<GoodsReceiptItem> goodsReceiptItems = new ArrayList<>();
}
