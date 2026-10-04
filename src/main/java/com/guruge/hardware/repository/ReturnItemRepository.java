package com.guruge.hardware.repository;

import com.guruge.hardware.entity.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {

    List<ReturnItem> findByReturnEntityId(Long returnEntityId);

    List<ReturnItem> findByProductId(Long productId);

    List<ReturnItem> findBySaleItemId(Long saleItemId);
}
