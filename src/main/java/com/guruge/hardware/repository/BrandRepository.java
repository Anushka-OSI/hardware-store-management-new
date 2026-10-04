package com.guruge.hardware.repository;

import com.guruge.hardware.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long>, JpaSpecificationExecutor<Brand> {

    Optional<Brand> findBySlug(String slug);

    Optional<Brand> findByName(String name);

    List<Brand> findByIsActive(Boolean isActive);

    boolean existsBySlug(String slug);
}
