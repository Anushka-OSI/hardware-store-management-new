package com.guruge.hardware.repository;

import com.guruge.hardware.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long>, JpaSpecificationExecutor<Unit> {

    Optional<Unit> findByCode(String code);

    Optional<Unit> findByName(String name);

    boolean existsByCode(String code);
}
