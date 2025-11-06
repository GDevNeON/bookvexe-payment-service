package com.example.bookvexe_payment_service.repositories.base;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import com.example.bookvexe_payment_service.models.db.BaseModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BaseRepositoryImpl<T extends BaseModel> extends SimpleJpaRepository<T, UUID> implements BaseRepositoryCustom<T> {

    private final EntityManager entityManager;
    private final Class<T> domainClass;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.domainClass = entityInformation.getJavaType();
    }

    private Predicate getNotDeletedPredicate(CriteriaBuilder cb, Root<T> root) {
        return cb.or(cb.isNull(root.get("isDeleted")), cb.isFalse(root.get("isDeleted")));
    }

    @Override
    public Optional<T> findById(String id) {
        return findById(UUID.fromString(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findByIdAndNotDeleted(UUID id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(domainClass);
        Root<T> root = query.from(domainClass);
        Predicate notDeleted = getNotDeletedPredicate(cb, root);
        query.where(cb.and(cb.equal(root.get("id"), id), notDeleted));
        try {
            return Optional.of(entityManager.createQuery(query).getSingleResult());
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAllNotDeleted() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(domainClass);
        Root<T> root = query.from(domainClass);
        query.where(getNotDeletedPredicate(cb, root));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<T> findAllNotDeleted(Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(domainClass);
        countQuery.select(cb.count(countRoot)).where(getNotDeletedPredicate(cb, countRoot));
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        if (total == 0) return new PageImpl<>(List.of(), pageable, 0);

        CriteriaQuery<T> resultQuery = cb.createQuery(domainClass);
        Root<T> resultRoot = resultQuery.from(domainClass);
        resultQuery.where(getNotDeletedPredicate(cb, resultRoot));

        List<Order> orders = new ArrayList<>();
        for (Sort.Order sortOrder : pageable.getSort()) {
            String property = sortOrder.getProperty();
            orders.add(sortOrder.getDirection() == Sort.Direction.ASC ? cb.asc(resultRoot.get(property)) : cb.desc(resultRoot.get(property)));
        }
        resultQuery.orderBy(orders);

        List<T> result = entityManager.createQuery(resultQuery)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(result, pageable, total);
    }
}
