package com.example.bookvexe_payment_service.repositories.base;

import com.example.bookvexe_payment_service.models.db.BaseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

@NoRepositoryBean
public interface BaseRepository<T extends BaseModel> extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T>, BaseRepositoryCustom<T> {

    default void softDelete(T entity) {
        if (entity != null) {
            entity.setIsDeleted(true);
            save(entity);
        }
    }

    default void softDeleteById(UUID id) {
        findById(id).ifPresent(this::softDelete);
    }
}
