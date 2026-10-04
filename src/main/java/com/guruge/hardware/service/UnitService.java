package com.guruge.hardware.service;

import com.guruge.hardware.entity.Unit;
import com.guruge.hardware.exception.BusinessException;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.UnitRepository;
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
public class UnitService {

    private final UnitRepository unitRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<Unit> list(String keyword, Pageable pageable) {
        Specification<Unit> spec = (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("code")), like));
        };
        return unitRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<Unit> listActive() {
        Specification<Unit> spec = (root, query, cb) -> cb.equal(root.get("isActive"), true);
        return unitRepository.findAll(spec);
    }

    @Transactional
    public Unit create(String name, String code, String description, Long actorId) {
        if (unitRepository.findByName(name).isPresent() || unitRepository.findByCode(code).isPresent()) {
            throw new BusinessException("Unit name/code already exists");
        }
        Unit unit = Unit.builder().name(name).code(code).description(description).isActive(true).build();
        Unit saved = unitRepository.save(unit);
        auditLogService.log("UNIT_CREATE", "Unit", String.valueOf(saved.getId()), null, name, actorId, null, null);
        return saved;
    }

    @Transactional
    public Unit update(Long id, String name, String code, String description, Long actorId) {
        Unit unit = getEntity(id);
        unit.setName(name);
        unit.setCode(code);
        unit.setDescription(description);
        return unitRepository.save(unit);
    }

    @Transactional
    public void delete(Long id, Long actorId) {
        // Hard delete: only succeeds when NO products use this unit (FK).
        Unit unit = getEntity(id);
        unitRepository.delete(unit);
        auditLogService.log("UNIT_DELETE", "Unit", String.valueOf(id), unit.getName(), null, actorId, null, null);
    }

    @Transactional
    public Unit setActive(Long id, boolean active, Long actorId) {
        Unit unit = getEntity(id);
        unit.setIsActive(active);
        return unitRepository.save(unit);
    }

    private Unit getEntity(Long id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", id));
    }
}
