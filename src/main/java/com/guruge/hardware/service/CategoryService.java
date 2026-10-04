package com.guruge.hardware.service;

import com.guruge.hardware.dto.request.CategoryRequest;
import com.guruge.hardware.entity.Category;
import com.guruge.hardware.exception.BusinessException;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.CategoryRepository;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<Category> list(String keyword, Pageable pageable) {
        Specification<Category> spec = (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), like);
        };
        return categoryRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<Category> listActive() {
        return categoryRepository.findByIsActive(true);
    }

    @Transactional
    public Category create(CategoryRequest req, Long actorId) {
        String slug = slugify(req.getName());
        if (categoryRepository.existsBySlug(slug)) {
            throw new BusinessException("Category slug already exists: " + slug);
        }
        Category category = Category.builder()
                .name(req.getName())
                .slug(slug)
                .description(req.getDescription())
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .build();
        if (req.getParentId() != null) {
            Category parent = categoryRepository.findById(req.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", req.getParentId()));
            category.setParent(parent);
        }
        Category saved = categoryRepository.save(category);
        auditLogService.log("CATEGORY_CREATE", "Category", String.valueOf(saved.getId()),
                null, saved.getName(), actorId, null, null);
        return saved;
    }

    @Transactional
    public Category update(Long id, CategoryRequest req, Long actorId) {
        Category category = getEntity(id);
        String old = category.getName();
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        if (req.getIsActive() != null) {
            category.setIsActive(req.getIsActive());
        }
        if (req.getSortOrder() != null) {
            category.setSortOrder(req.getSortOrder());
        }
        if (req.getParentId() != null) {
            Category parent = categoryRepository.findById(req.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", req.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        Category saved = categoryRepository.save(category);
        auditLogService.log("CATEGORY_UPDATE", "Category", String.valueOf(id), old, saved.getName(), actorId, null, null);
        return saved;
    }

    @Transactional
    public void delete(Long id, Long actorId) {
        // Soft delete: products reference categories via FK
        Category category = getEntity(id);
        category.setIsActive(false);
        categoryRepository.save(category);
        auditLogService.log("CATEGORY_DEACTIVATE", "Category", String.valueOf(id), category.getName(), "INACTIVE", actorId, null, null);
    }

    @Transactional
    public Category setActive(Long id, boolean active, Long actorId) {
        Category category = getEntity(id);
        category.setIsActive(active);
        Category saved = categoryRepository.save(category);
        auditLogService.log(active ? "CATEGORY_ACTIVATE" : "CATEGORY_DEACTIVATE",
                "Category", String.valueOf(id), String.valueOf(!active), String.valueOf(active), actorId, null, null);
        return saved;
    }

    private Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    private String slugify(String name) {
        if (name == null) {
            return "category";
        }
        return name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
