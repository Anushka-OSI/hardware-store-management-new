package com.guruge.hardware.controller.customer;

import com.guruge.hardware.dto.common.ApiResponse;
import com.guruge.hardware.dto.response.ProductResponse;
import com.guruge.hardware.entity.Brand;
import com.guruge.hardware.entity.Category;
import com.guruge.hardware.service.BrandService;
import com.guruge.hardware.service.CategoryService;
import com.guruge.hardware.service.PublicProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicProductController {

    private final PublicProductService publicProductService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                publicProductService.search(keyword, categoryId, brandId, minPrice, maxPrice, sort, pageable)));
    }

    @GetMapping("/products/featured")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> featured() {
        return ResponseEntity.ok(ApiResponse.ok(publicProductService.featured()));
    }

    @GetMapping("/products/popular")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> popular() {
        return ResponseEntity.ok(ApiResponse.ok(publicProductService.popular()));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> productDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(publicProductService.getDetail(id)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> categories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.listActive()));
    }

    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<List<Brand>>> brands() {
        return ResponseEntity.ok(ApiResponse.ok(brandService.listActive()));
    }
}
