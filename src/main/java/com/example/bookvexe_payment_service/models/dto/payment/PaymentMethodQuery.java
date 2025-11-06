package com.example.bookvexe_payment_service.models.dto.payment;

import com.example.bookvexe_payment_service.models.dto.base.BasePageableQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentMethodQuery extends BasePageableQuery {
    private String code;
    private String name;
    private Boolean isDeleted;
}
