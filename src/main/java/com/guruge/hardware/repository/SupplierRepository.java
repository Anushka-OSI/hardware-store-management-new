package com.guruge.hardware.repository;

import com.guruge.hardware.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {

    List<Supplier> findByStatus(String status);

    List<Supplier> findByCompanyNameContainingIgnoreCase(String companyName);
}
