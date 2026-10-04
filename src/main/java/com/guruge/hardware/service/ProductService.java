package com.guruge.hardware.service;

import com.guruge.hardware.dto.request.ProductRequest;
import com.guruge.hardware.dto.response.ProductResponse;
import com.guruge.hardware.entity.Brand;
import com.guruge.hardware.entity.Category;
import com.guruge.hardware.entity.Product;
import com.guruge.hardware.entity.Unit;
import com.guruge.hardware.exception.BusinessException;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.BrandRepository;
import com.guruge.hardware.repository.CategoryRepository;
import com.guruge.hardware.repository.ProductRepository;
import com.guruge.hardware.repository.UnitRepository;
import com.guruge.hardware.util.FileUploadUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UnitRepository unitRepository;
    private final FileUploadUtils fileUploadUtils;
    private final AuditLogService auditLogService;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String keyword, Long categoryId, Long brandId,
                                       String status, BigDecimal minPrice, BigDecimal maxPrice,
                                       String availability, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("sku")), like),
                        cb.like(cb.lower(root.get("barcode")), like)));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (brandId != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), brandId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status.toUpperCase()));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
            }
            if (StringUtils.hasText(availability)) {
                switch (availability.toUpperCase()) {
                    case "OUT_OF_STOCK" -> predicates.add(cb.lessThanOrEqualTo(root.get("currentStock"), 0));
                    case "LOW_STOCK" -> predicates.add(cb.and(
                            cb.greaterThan(root.get("currentStock"), 0),
                            cb.lessThanOrEqualTo(root.get("currentStock"), root.get("minStockLevel"))));
                    case "IN_STOCK" -> predicates.add(cb.greaterThan(root.get("currentStock"), root.get("minStockLevel")));
                    default -> {
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return productRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public String generateSku(Long categoryId) {
        String prefix = "PRD";
        if (categoryId != null) {
            String catName = categoryRepository.findById(categoryId)
                    .map(c -> c.getName() != null ? c.getName() : "")
                    .orElse("");
            String clean = catName.replaceAll("[^A-Za-z]", "").toUpperCase();
            if (clean.length() >= 3) {
                prefix = clean.substring(0, 3);
            } else if (!clean.isEmpty()) {
                prefix = (clean + "XXX").substring(0, 3);
            }
        }
        long base = productRepository.count() + 1;
        for (int attempt = 0; attempt < 500; attempt++) {
            String candidate = prefix + "-" + String.format("%06d", base + attempt);
            if (!productRepository.existsBySku(candidate)) {
                return candidate;
            }
        }
        // Fallback: timestamp-based, guaranteed unique in practice
        String candidate = prefix + "-" + System.currentTimeMillis() % 1000000;
        if (!productRepository.existsBySku(candidate)) {
            return candidate;
        }
        return prefix + "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Transactional
    public ProductResponse create(ProductRequest req, MultipartFile imageFile, Long actorId) {
        if (!StringUtils.hasText(req.getSku())) {
            req.setSku(generateSku(req.getCategoryId()));
        }
        if (productRepository.existsBySku(req.getSku())) {
            throw new BusinessException("SKU already exists: " + req.getSku());
        }
        if (StringUtils.hasText(req.getBarcode())) {
            productRepository.findByBarcode(req.getBarcode()).ifPresent(p -> {
                throw new BusinessException("Barcode already exists: " + req.getBarcode());
            });
        }
        if (req.getCategoryId() == null) {
            throw new BusinessException("Category is required");
        }
        if (req.getBrandId() == null) {
            throw new BusinessException("Brand is required");
        }
        if (imageFile == null || imageFile.isEmpty()) {
            throw new BusinessException("Product image is required");
        }
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", req.getCategoryId()));
        Unit unit = unitRepository.findById(req.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", req.getUnitId()));
        Brand brand = brandRepository.findById(req.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", req.getBrandId()));
        String imageUrl;
        try {
            imageUrl = fileUploadUtils.save(imageFile, "product-images");
        } catch (IOException ex) {
            throw new BusinessException("Failed to upload image: " + ex.getMessage(), ex);
        }
        Product product = Product.builder()
                .sku(req.getSku())
                .barcode(req.getBarcode())
                .name(req.getName())
                .shortDesc(req.getShortDesc())
                .fullDesc(req.getFullDesc())
                .category(category)
                .brand(brand)
                .unit(unit)
                .sellingPrice(req.getSellingPrice() != null ? req.getSellingPrice() : BigDecimal.ZERO)
                .costPrice(req.getCostPrice() != null ? req.getCostPrice() : BigDecimal.ZERO)
                .discountPercent(req.getDiscountPercent() != null ? req.getDiscountPercent() : BigDecimal.ZERO)
                .currentStock(req.getCurrentStock() != null ? req.getCurrentStock() : 0)
                .minStockLevel(req.getMinStockLevel() != null ? req.getMinStockLevel() : 10)
                .maxStockLevel(req.getMaxStockLevel())
                .status(StringUtils.hasText(req.getStatus()) ? req.getStatus().toUpperCase() : "ACTIVE")
                .imageUrl(imageUrl)
                .createdBy(actorId)
                .updatedBy(actorId)
                .build();
        Product saved = productRepository.save(product);
        auditLogService.log("PRODUCT_CREATE", "Product", String.valueOf(saved.getId()),
                null, saved.getSku(), actorId, null, null);
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest req, Long actorId) {
        Product product = getEntity(id);
        if (!StringUtils.hasText(req.getSku())) {
            req.setSku(product.getSku());
        }
        if (!product.getSku().equals(req.getSku()) && productRepository.existsBySku(req.getSku())) {
            throw new BusinessException("SKU already exists: " + req.getSku());
        }
        if (StringUtils.hasText(req.getBarcode())
                && !req.getBarcode().equals(product.getBarcode())) {
            productRepository.findByBarcode(req.getBarcode()).ifPresent(p -> {
                throw new BusinessException("Barcode already exists: " + req.getBarcode());
            });
        }
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", req.getCategoryId()));
        Unit unit = unitRepository.findById(req.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", req.getUnitId()));
        Brand brand = null;
        if (req.getBrandId() != null) {
            brand = brandRepository.findById(req.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", req.getBrandId()));
        }
        String old = product.getName() + "|" + product.getSellingPrice();
        product.setSku(req.getSku());
        product.setBarcode(req.getBarcode());
        product.setName(req.getName());
        product.setShortDesc(req.getShortDesc());
        product.setFullDesc(req.getFullDesc());
        product.setCategory(category);
        product.setBrand(brand);
        product.setUnit(unit);
        product.setSellingPrice(req.getSellingPrice());
        if (req.getCostPrice() != null) {
            product.setCostPrice(req.getCostPrice());
        }
        if (req.getDiscountPercent() != null) {
            product.setDiscountPercent(req.getDiscountPercent());
        }
        if (req.getCurrentStock() != null) {
            product.setCurrentStock(req.getCurrentStock());
        }
        if (req.getMinStockLevel() != null) {
            product.setMinStockLevel(req.getMinStockLevel());
        }
        product.setMaxStockLevel(req.getMaxStockLevel());
        if (StringUtils.hasText(req.getStatus())) {
            product.setStatus(req.getStatus().toUpperCase());
        }
        product.setUpdatedBy(actorId);
        Product saved = productRepository.save(product);
        auditLogService.log("PRODUCT_UPDATE", "Product", String.valueOf(id), old,
                saved.getName() + "|" + saved.getSellingPrice(), actorId, null, null);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id, Long actorId) {
        // Soft delete: keep historical sales/purchases intact
        Product product = getEntity(id);
        String old = product.getStatus();
        product.setStatus("INACTIVE");
        product.setUpdatedBy(actorId);
        productRepository.save(product);
        auditLogService.log("PRODUCT_DEACTIVATE", "Product", String.valueOf(id), old, "INACTIVE", actorId, null, null);
    }

    @Transactional
    public ProductResponse updatePrice(Long id, BigDecimal sellingPrice, BigDecimal costPrice, Long actorId) {
        Product product = getEntity(id);
        String old = String.valueOf(product.getSellingPrice());
        product.setSellingPrice(sellingPrice);
        if (costPrice != null) {
            product.setCostPrice(costPrice);
        }
        product.setUpdatedBy(actorId);
        Product saved = productRepository.save(product);
        auditLogService.log("PRODUCT_PRICE", "Product", String.valueOf(id), old, String.valueOf(sellingPrice), actorId, null, null);
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse adjustStock(Long productId, int qtyChange, String reason, Long userId) {
        inventoryService.adjustStock(productId, qtyChange, reason, userId);
        return toResponse(getEntity(productId));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> lowStock() {
        return productRepository.findLowStockProducts().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> outOfStock() {
        Specification<Product> spec = (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("currentStock"), 0);
        return productRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional
    public String uploadImage(Long productId, MultipartFile file, Long actorId) {
        Product product = getEntity(id(productId));
        try {
            String path = fileUploadUtils.save(file, "product-images");
            if (product.getImageUrl() != null) {
                fileUploadUtils.delete(product.getImageUrl());
            }
            product.setImageUrl(path);
            product.setUpdatedBy(actorId);
            productRepository.save(product);
            return path;
        } catch (IOException ex) {
            throw new BusinessException("Failed to upload image: " + ex.getMessage(), ex);
        }
    }

    private Long id(Long productId) {
        return productId;
    }

    private Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    public ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .sku(p.getSku())
                .barcode(p.getBarcode())
                .name(p.getName())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .brandId(p.getBrand() != null ? p.getBrand().getId() : null)
                .brandName(p.getBrand() != null ? p.getBrand().getName() : null)
                .unitId(p.getUnit() != null ? p.getUnit().getId() : null)
                .unitName(p.getUnit() != null ? p.getUnit().getName() : null)
                .sellingPrice(p.getSellingPrice())
                .costPrice(p.getCostPrice())
                .discountPercent(p.getDiscountPercent())
                .currentStock(p.getCurrentStock())
                .minStockLevel(p.getMinStockLevel())
                .status(p.getStatus())
                .imageUrl(p.getImageUrl())
                .availabilityStatus(p.getAvailabilityStatus())
                .build();
    }
}
