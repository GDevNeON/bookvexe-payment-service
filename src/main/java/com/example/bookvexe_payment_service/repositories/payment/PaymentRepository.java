package com.example.bookvexe_payment_service.repositories.payment;

import com.example.bookvexe_payment_service.models.db.PaymentDbModel;
import com.example.bookvexe_payment_service.repositories.base.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends BaseRepository<PaymentDbModel> {
}
