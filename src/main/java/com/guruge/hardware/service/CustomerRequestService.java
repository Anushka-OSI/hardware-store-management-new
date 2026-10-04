package com.guruge.hardware.service;

import com.guruge.hardware.dto.request.CustomerRequestRespond;
import com.guruge.hardware.dto.request.CustomerRequestSubmit;
import com.guruge.hardware.entity.CustomerRequest;
import com.guruge.hardware.exception.BusinessException;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.CustomerRequestRepository;
import com.guruge.hardware.util.NumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerRequestService {

    private final CustomerRequestRepository customerRequestRepository;
    private final NumberGenerator numberGenerator;
    private final AuditLogService auditLogService;

    @Transactional
    public CustomerRequest submit(CustomerRequestSubmit req) {
        CustomerRequest cr = CustomerRequest.builder()
                .requestId(numberGenerator.generateRequestId())
                .customerName(req.getCustomerName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .message(req.getMessage())
                .status("NEW")
                .build();
        return customerRequestRepository.save(cr);
    }

    @Transactional(readOnly = true)
    public CustomerRequest checkStatus(String requestId, String phone) {
        return customerRequestRepository.findByPhoneAndRequestId(phone, requestId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerRequest", "requestId", requestId));
    }

    @Transactional(readOnly = true)
    public Page<CustomerRequest> list(String status, String keyword, Pageable pageable) {
        Specification<CustomerRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status.toUpperCase()));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("customerName")), like),
                        cb.like(cb.lower(root.get("requestId")), like),
                        cb.like(cb.lower(root.get("phone")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return customerRequestRepository.findAll(spec, pageable);
    }

    @Transactional
    public CustomerRequest respond(Long id, CustomerRequestRespond req, Long respondedBy) {
        CustomerRequest cr = customerRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerRequest", "id", id));
        cr.setAdminResponse(req.getAdminResponse());
        cr.setStatus(StringUtils.hasText(req.getStatus()) ? req.getStatus().toUpperCase() : "RESPONDED");
        cr.setRespondedBy(respondedBy);
        cr.setRespondedAt(LocalDateTime.now());
        CustomerRequest saved = customerRequestRepository.save(cr);
        auditLogService.log("REQUEST_RESPOND", "CustomerRequest", String.valueOf(id),
                null, saved.getStatus(), respondedBy, null, null);
        return saved;
    }

    @Transactional
    public CustomerRequest updateStatus(Long id, String status, Long actorId) {
        CustomerRequest cr = customerRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerRequest", "id", id));
        if (!StringUtils.hasText(status)) {
            throw new BusinessException("Status is required");
        }
        String old = cr.getStatus();
        cr.setStatus(status.toUpperCase());
        CustomerRequest saved = customerRequestRepository.save(cr);
        auditLogService.log("REQUEST_STATUS", "CustomerRequest", String.valueOf(id), old, status, actorId, null, null);
        return saved;
    }
}
