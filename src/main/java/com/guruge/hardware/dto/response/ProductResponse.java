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
public class ProductResponse {

    private Long id;
    private String sku;
    private String barcode;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Long unitId;
    private String unitName;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private BigDecimal discountPercent;
    private Integer currentStock;
    private Integer minStockLevel;
    private String status;
    private String imageUrl;
    private String availabilityStatus;
}
