package com.guruge.hardware.repository;

import com.guruge.hardware.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long>, JpaSpecificationExecutor<InventoryTransaction> {

    List<InventoryTransaction> findByProductId(Long productId);

    List<InventoryTransaction> findByMovementType(String movementType);

    List<InventoryTransaction> findByProductIdAndMovementType(Long productId, String movementType);

    List<InventoryTransaction> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}
