package com.guruge.hardware.service;

import com.guruge.hardware.dto.response.ProductResponse;
import com.guruge.hardware.entity.Product;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicProductService {

    private final ProductRepository productRepository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String keyword, Long categoryId, Long brandId,
                                        BigDecimal minPrice, BigDecimal maxPrice,
                                        String sort, Pageable pageable) {
        Pageable effective = applySort(pageable, sort);
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "ACTIVE"));
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
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return productRepository.findAll(spec, effective).map(productService::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> featured() {
        Pageable pageable = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Product> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), "ACTIVE"),
                cb.greaterThan(root.get("currentStock"), 0));
        return productRepository.findAll(spec, pageable).getContent().stream()
                .map(productService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> popular() {
        Pageable pageable = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "currentStock"));
        Specification<Product> spec = (root, query, cb) ->
                cb.equal(root.get("status"), "ACTIVE");
        return productRepository.findAll(spec, pageable).getContent().stream()
                .map(productService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        return productService.toResponse(product);
    }

    private Pageable applySort(Pageable pageable, String sort) {
        Sort s = pageable.getSort();
        if (StringUtils.hasText(sort)) {
            String normalized = sort.trim().toLowerCase().replace("_", "");
            switch (normalized) {
                case "priceasc" -> s = Sort.by(Sort.Direction.ASC, "sellingPrice");
                case "pricedesc" -> s = Sort.by(Sort.Direction.DESC, "sellingPrice");
                case "nameasc" -> s = Sort.by(Sort.Direction.ASC, "name");
                case "namedesc" -> s = Sort.by(Sort.Direction.DESC, "name");
                case "newest" -> s = Sort.by(Sort.Direction.DESC, "createdAt");
                default -> {
                }
            }
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), s);
    }
}
