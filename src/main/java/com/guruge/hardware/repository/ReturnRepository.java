package com.guruge.hardware.repository;

import com.guruge.hardware.entity.ReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRepository extends JpaRepository<ReturnEntity, Long>, JpaSpecificationExecutor<ReturnEntity> {

    Optional<ReturnEntity> findByReturnNumber(String returnNumber);

    List<ReturnEntity> findBySaleId(Long saleId);

    List<ReturnEntity> findByStatus(String status);

    boolean existsByReturnNumber(String returnNumber);
}
