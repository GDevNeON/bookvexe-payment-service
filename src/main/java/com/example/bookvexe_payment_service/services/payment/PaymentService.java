package com.example.bookvexe_payment_service.services.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.example.bookvexe_payment_service.models.dto.payment.PaymentCreate;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentQuery;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentResponse;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentSelectResponse;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentUpdate;

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

    PaymentResponse findByTransactionCode(String transactionCode);
}
