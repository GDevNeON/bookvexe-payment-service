package com.example.bookvexe_payment_service.services.payment;

import com.example.bookvexe_payment_service.models.dto.payment.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodService {
    List<PaymentMethodResponse> findAll();

    Page<PaymentMethodResponse> findAll(PaymentMethodQuery query);

    PaymentMethodResponse findById(UUID id);

    PaymentMethodResponse create(PaymentMethodCreate createDto);

    PaymentMethodResponse update(UUID id, PaymentMethodUpdate updateDto);

    void delete(UUID id);

    void activate(UUID id);

    void deactivate(UUID id);

    List<PaymentMethodSelectResponse> findAllForSelect();

    Page<PaymentMethodSelectResponse> findAllForSelect(PaymentMethodQuery query);
}
