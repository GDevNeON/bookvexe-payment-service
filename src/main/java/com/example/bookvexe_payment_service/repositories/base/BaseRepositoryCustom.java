package com.example.bookvexe_payment_service.repositories.base;

import com.example.bookvexe_payment_service.models.db.BaseModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BaseRepositoryCustom<T extends BaseModel> {
    Optional<T> findById(String id);
    Optional<T> findByIdAndNotDeleted(UUID id);
    List<T> findAllNotDeleted();
    Page<T> findAllNotDeleted(Pageable pageable);
}
