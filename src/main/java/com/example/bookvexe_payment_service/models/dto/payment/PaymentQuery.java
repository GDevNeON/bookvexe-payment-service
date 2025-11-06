package com.example.bookvexe_payment_service.models.dto.payment;

import com.example.bookvexe_payment_service.models.dto.base.BasePageableQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentQuery extends BasePageableQuery {
    private UUID bookingId;
    private UUID methodId;
    private String status;
    private String transactionCode;
}
