package com.example.bookvexe_payment_service.controllers.payment;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import com.example.bookvexe_payment_service.models.db.PaymentMethodDbModel;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentCreate;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentResponse;
import com.example.bookvexe_payment_service.repositories.payment.PaymentMethodRepository;
import com.example.bookvexe_payment_service.services.payment.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/user/payments")
@RequiredArgsConstructor
public class PaymentUserController {

    private final PaymentService paymentService;
    private final PaymentMethodRepository paymentMethodRepository;

    @PostMapping
    public ResponseEntity<PaymentResponse> createGuest(@RequestBody PaymentGuestCreate req) {
        PaymentCreate create = new PaymentCreate();
        create.setBookingId(req.getBookingId());
        create.setAmount(req.getAmount());
        create.setStatus(req.getStatus());
        create.setTransactionCode(req.getTransactionCode());
        create.setPaidAt(req.getPaidAt());

        if (req.getMethodId() != null) {
            create.setMethodId(req.getMethodId());
        } else if (req.getMethodCode() != null) {
            Optional<PaymentMethodDbModel> methodOpt = paymentMethodRepository.findByCode(req.getMethodCode());
            if (methodOpt.isPresent()) {
                create.setMethodId(methodOpt.get().getId());
            } else {
                PaymentMethodDbModel method = new PaymentMethodDbModel();
                method.setCode(req.getMethodCode());
                method.setName(req.getMethodCode());
                method.setIsDeleted(false);
                PaymentMethodDbModel saved = paymentMethodRepository.save(method);
                create.setMethodId(saved.getId());
            }
        } else {
            throw new IllegalArgumentException("Either methodId or methodCode must be provided");
        }

        return ResponseEntity.ok(paymentService.create(create));
    }

    @Data
    public static class PaymentGuestCreate {
        private UUID bookingId;
        private UUID methodId;
        private String methodCode;
        private BigDecimal amount;
        private String status;
        private String transactionCode;
        private LocalDateTime paidAt;
    }
}
