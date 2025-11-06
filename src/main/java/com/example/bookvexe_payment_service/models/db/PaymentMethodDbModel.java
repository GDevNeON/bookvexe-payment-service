package com.example.bookvexe_payment_service.models.db;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentMethodDbModel extends BaseModel {
    @Column(length = 255, unique = true, name = "code")
    private String code;

    @Column(length = 50, unique = true, name = "name")
    private String name;

    @Column(length = 255, name = "description")
    private String description;

    @OneToMany(mappedBy = "method")
    private List<PaymentDbModel> payments;
}
