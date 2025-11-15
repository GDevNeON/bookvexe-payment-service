package com.example.bookvexe_payment_service.repositories.payment;

import com.example.bookvexe_payment_service.models.db.PaymentDbModel;
import com.example.bookvexe_payment_service.repositories.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends BaseRepository<PaymentDbModel> {
    Optional<PaymentDbModel> findByTransactionCodeAndIsDeletedFalse(String transactionCode);
}
