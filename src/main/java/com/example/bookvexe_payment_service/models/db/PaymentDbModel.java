package com.example.bookvexe_payment_service.models.db;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentDbModel extends BaseModel {

    @Column(name = "bookingId")
    private UUID bookingId;

    @ManyToOne
    @JoinColumn(name = "methodId")
    private PaymentMethodDbModel method;

    @Column(precision = 10, scale = 2, name = "amount")
    private BigDecimal amount;

    @Column(length = 20, name = "status")
    private String status;

    @Column(length = 100, name = "transactionCode")
    private String transactionCode;

    @Column(name = "paidAt")
    private LocalDateTime paidAt;
}
