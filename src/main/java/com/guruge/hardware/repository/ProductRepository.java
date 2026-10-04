package com.guruge.hardware.repository;

import com.guruge.hardware.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findByBarcode(String barcode);

    List<Product> findByStatus(String status);

    Page<Product> findByStatus(String status, Pageable pageable);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByBrandId(Long brandId);

    boolean existsBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("select p from Product p where " +
            "lower(p.name) like lower(concat('%', :keyword, '%')) or " +
            "lower(p.sku) like lower(concat('%', :keyword, '%')) or " +
            "lower(p.barcode) like lower(concat('%', :keyword, '%'))")
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("select p from Product p where p.currentStock <= p.minStockLevel and p.status = 'ACTIVE'")
    List<Product> findLowStockProducts();

    @Query("select p from Product p where p.currentStock <= :threshold and p.status = 'ACTIVE'")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
}
