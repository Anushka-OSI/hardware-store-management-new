package com.guruge.hardware.repository;

import com.guruge.hardware.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByAction(String action);
}
