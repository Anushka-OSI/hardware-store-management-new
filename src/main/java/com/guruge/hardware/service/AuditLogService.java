package com.guruge.hardware.service;

import com.guruge.hardware.entity.AuditLog;
import com.guruge.hardware.repository.AuditLogRepository;
import com.guruge.hardware.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public AuditLog log(String action, String entityType, String entityId,
                        String oldVal, String newVal, Long userId,
                        String ip, String userAgent) {
        String username = null;
        if (userId != null) {
            username = userRepository.findById(userId)
                    .map(u -> u.getUsername())
                    .orElse(null);
        }
        AuditLog entry = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValues(oldVal)
                .newValues(newVal)
                .ipAddress(ip)
                .userAgent(userAgent)
                .build();
        return auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> list(String action, String entityType, Long userId, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(action)) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (StringUtils.hasText(entityType)) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return auditLogRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getRecent(int limit) {
        Page<AuditLog> page = auditLogRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, Math.max(1, Math.min(limit, 100)),
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")));
        return page.getContent();
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getByEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
}
