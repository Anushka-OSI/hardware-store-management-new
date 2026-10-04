package com.guruge.hardware.service;

import com.guruge.hardware.entity.Brand;
import com.guruge.hardware.exception.BusinessException;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<Brand> list(String keyword, Pageable pageable) {
        Specification<Brand> spec = (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
        };
        return brandRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<Brand> listActive() {
        return brandRepository.findByIsActive(true);
    }

    @Transactional
    public Brand create(String name, String description, String logoUrl, Long actorId) {
        if (brandRepository.findByName(name).isPresent()) {
            throw new BusinessException("Brand already exists: " + name);
        }
        String slug = slugify(name);
        if (brandRepository.existsBySlug(slug)) {
            throw new BusinessException("Brand slug already exists: " + slug);
        }
        Brand brand = Brand.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .logoUrl(logoUrl)
                .isActive(true)
                .build();
        Brand saved = brandRepository.save(brand);
        auditLogService.log("BRAND_CREATE", "Brand", String.valueOf(saved.getId()), null, name, actorId, null, null);
        return saved;
    }

    @Transactional
    public Brand update(Long id, String name, String description, String logoUrl, Long actorId) {
        Brand brand = getEntity(id);
        String old = brand.getName();
        brand.setName(name);
        brand.setSlug(slugify(name));
        brand.setDescription(description);
        brand.setLogoUrl(logoUrl);
        Brand saved = brandRepository.save(brand);
        auditLogService.log("BRAND_UPDATE", "Brand", String.valueOf(id), old, name, actorId, null, null);
        return saved;
    }

    @Transactional
    public void delete(Long id, Long actorId) {
        // Soft delete: products reference brands via FK
        Brand brand = getEntity(id);
        brand.setIsActive(false);
        brandRepository.save(brand);
        auditLogService.log("BRAND_DEACTIVATE", "Brand", String.valueOf(id), brand.getName(), "INACTIVE", actorId, null, null);
    }

    @Transactional
    public Brand setActive(Long id, boolean active, Long actorId) {
        Brand brand = getEntity(id);
        brand.setIsActive(active);
        return brandRepository.save(brand);
    }

    private Brand getEntity(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
    }

    private String slugify(String name) {
        if (name == null) {
            return "brand";
        }
        return name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
