package com.guruge.hardware.service;

import com.guruge.hardware.entity.Payment;
import com.guruge.hardware.entity.Sale;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.PaymentRepository;
import com.guruge.hardware.repository.SaleRepository;
import com.guruge.hardware.util.MoneyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SaleRepository saleRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public Payment record(Long saleId, String paymentMethod, BigDecimal amountPaid,
                          String transactionRef, Long actorId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", saleId));
        BigDecimal balance = MoneyUtils.sub(amountPaid, sale.getTotalAmount());
        Payment payment = Payment.builder()
                .sale(sale)
                .paymentMethod(paymentMethod != null ? paymentMethod.toUpperCase() : "CASH")
                .amountPaid(MoneyUtils.round(amountPaid))
                .balance(MoneyUtils.round(balance))
                .transactionRef(transactionRef)
                .status("PAID")
                .createdBy(actorId)
                .build();
        Payment saved = paymentRepository.save(payment);
        auditLogService.log("PAYMENT_RECORD", "Payment", String.valueOf(saved.getId()),
                null, sale.getReceiptNumber(), actorId, null, null);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Payment> listBySale(Long saleId) {
        return paymentRepository.findBySaleId(saleId);
    }
}
