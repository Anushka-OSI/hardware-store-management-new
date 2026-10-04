package com.guruge.hardware.repository;

import com.guruge.hardware.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findBySaleId(Long saleId);

    List<Payment> findByStatus(String status);

    List<Payment> findByPaymentMethod(String paymentMethod);
}
