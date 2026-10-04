package com.guruge.hardware.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    private String sku;

    private String barcode;

    @NotBlank(message = "Product name is required")
    private String name;

    private String shortDesc;
    private String fullDesc;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Long brandId;

    @NotNull(message = "Unit ID is required")
    private Long unitId;

    @NotNull
    @DecimalMin(value = "0.0", message = "Selling price must be >= 0")
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.0", message = "Cost price must be >= 0")
    private BigDecimal costPrice;

    @DecimalMin(value = "0.0", message = "Discount percent must be >= 0")
    private BigDecimal discountPercent;

    @Min(value = 0, message = "Stock must be >= 0")
    private Integer currentStock;

    @Min(value = 0, message = "Min stock level must be >= 0")
    private Integer minStockLevel;

    private Integer maxStockLevel;
    private String status;
}
