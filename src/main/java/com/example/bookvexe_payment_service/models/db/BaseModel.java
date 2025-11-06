package com.example.bookvexe_payment_service.models.db;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EntityListeners(AuditingEntityListener.class)
public class BaseModel {
    @Id
    @Column(length = 255, name = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "isDeleted")
    private Boolean isDeleted = false;

    @CreatedDate
    @Column(updatable = false, name = "createdDate")
    private LocalDateTime createdDate;

    @Column(length = 255, name = "createdBy")
    @CreatedBy
    private UUID createdBy;

    @LastModifiedDate
    @Column(name = "updatedDate")
    private LocalDateTime updatedDate;

    @Column(length = 255, name = "updatedBy")
    @LastModifiedBy
    private UUID updatedBy;
}
