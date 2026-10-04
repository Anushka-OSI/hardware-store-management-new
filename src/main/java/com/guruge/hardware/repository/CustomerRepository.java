package com.guruge.hardware.repository;

import com.guruge.hardware.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    List<Customer> findByPhone(String phone);

    Optional<Customer> findByEmail(String email);

    List<Customer> findByNameContainingIgnoreCase(String name);
}
