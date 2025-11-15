package com.example.bookvexe_payment_service.services.payment;

import com.example.bookvexe_payment_service.models.dto.payment.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentService {
    List<PaymentResponse> findAll();

    Page<PaymentResponse> findAll(PaymentQuery query);

    PaymentResponse findById(UUID id);

    PaymentResponse create(PaymentCreate createDto);

    PaymentResponse update(UUID id, PaymentUpdate updateDto);

    void delete(UUID id);

    void activate(UUID id);

    void deactivate(UUID id);

    List<PaymentSelectResponse> findAllForSelect();

    Page<PaymentSelectResponse> findAllForSelect(PaymentQuery query);

    PaymentResponse updateStatusByTransactionCode(String transactionCode, String status, LocalDateTime paidAt);

    void deleteByTransactionCode(String transactionCode);
}
