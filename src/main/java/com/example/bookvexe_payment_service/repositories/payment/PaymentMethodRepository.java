package com.example.bookvexe_payment_service.repositories.payment;

import com.example.bookvexe_payment_service.models.db.PaymentMethodDbModel;
import com.example.bookvexe_payment_service.repositories.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends BaseRepository<PaymentMethodDbModel> {
    Optional<PaymentMethodDbModel> findByCode(String code);

    Optional<PaymentMethodDbModel> findByCodeIgnoreCase(String code);
}
