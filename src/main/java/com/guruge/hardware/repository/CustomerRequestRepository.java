package com.guruge.hardware.repository;

import com.guruge.hardware.entity.CustomerRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRequestRepository extends JpaRepository<CustomerRequest, Long>, JpaSpecificationExecutor<CustomerRequest> {

    Optional<CustomerRequest> findByRequestId(String requestId);

    Optional<CustomerRequest> findByPhoneAndRequestId(String phone, String requestId);

    List<CustomerRequest> findByStatus(String status);

    List<CustomerRequest> findByPhone(String phone);
}
