package com.example.bookvexe_payment_service.services.payment.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.bookvexe_payment_service.exceptions.ResourceNotFoundException;
import com.example.bookvexe_payment_service.mappers.PaymentMapper;
import com.example.bookvexe_payment_service.models.db.PaymentDbModel;
import com.example.bookvexe_payment_service.models.db.PaymentMethodDbModel;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentCreate;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentQuery;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentResponse;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentSelectResponse;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentUpdate;
import com.example.bookvexe_payment_service.repositories.payment.PaymentMethodRepository;
import com.example.bookvexe_payment_service.repositories.payment.PaymentRepository;
import com.example.bookvexe_payment_service.services.notification.NotificationService;
import com.example.bookvexe_payment_service.services.payment.PaymentService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final NotificationService notificationService;
    private final PaymentMapper paymentMapper;

    @Override
    public List<PaymentResponse> findAll() {
        List<PaymentDbModel> entities = paymentRepository.findAllNotDeleted();
        return entities.stream().map(paymentMapper::toResponse).toList();
    }

    @Override
    public Page<PaymentResponse> findAll(PaymentQuery query) {
        Specification<PaymentDbModel> spec = buildSpecification(query);
        Pageable pageable = buildPageable(query);
        Page<PaymentDbModel> entities = paymentRepository.findAll(spec, pageable);
        return entities.map(paymentMapper::toResponse);
    }

    @Override
    public PaymentResponse findById(UUID id) {
        PaymentDbModel entity = paymentRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException(PaymentDbModel.class, id));
        return paymentMapper.toResponse(entity);
    }

    @Override
    public PaymentResponse create(PaymentCreate createDto) {
        PaymentDbModel entity = new PaymentDbModel();
        entity.setAmount(createDto.getAmount());
        entity.setStatus(createDto.getStatus());
        entity.setTransactionCode(createDto.getTransactionCode());
        entity.setPaidAt(createDto.getPaidAt());
        entity.setBookingId(createDto.getBookingId());

        PaymentMethodDbModel method = paymentMethodRepository.findById(createDto.getMethodId())
                .orElseThrow(() -> new ResourceNotFoundException(PaymentMethodDbModel.class, createDto.getMethodId()));
        entity.setMethod(method);

        PaymentDbModel saved = paymentRepository.save(entity);

        // ADD NOTIFICATION: Payment Completed
        if ("SUCCESS".equalsIgnoreCase(saved.getStatus())) {
            try {
                // Use the booking-based notification method which handles both registered and
                // guest users
                notificationService.sendNotificationByBookingId(
                        "TYPE_PAYMENT_SUCCESS",
                        "Thanh toán thành công",
                        "Thanh toán của bạn đã được xử lý thành công. Mã giao dịch: " + saved.getTransactionCode() +
                                ". Số tiền: " + saved.getAmount() + " VND.",
                        saved.getBookingId(),
                        "APP",
                        true, // sendEmail
                        true // shouldSave (will be skipped for guests automatically)
                );
            } catch (Exception e) {
                log.error("Failed to send payment completion notification: {}", e.getMessage(), e);
                // Don't throw - notification failure shouldn't break payment creation
            }
        }

        return paymentMapper.toResponse(saved);
    }

    @Override
    public PaymentResponse update(UUID id, PaymentUpdate updateDto) {
        PaymentDbModel entity = paymentRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException(PaymentDbModel.class, id));

        // Capture old status for comparison
        String oldStatus = entity.getStatus();

        Optional.ofNullable(updateDto.getAmount()).ifPresent(entity::setAmount);
        Optional.ofNullable(updateDto.getStatus()).ifPresent(entity::setStatus);
        Optional.ofNullable(updateDto.getTransactionCode()).ifPresent(entity::setTransactionCode);
        Optional.ofNullable(updateDto.getPaidAt()).ifPresent(entity::setPaidAt);
        Optional.ofNullable(updateDto.getBookingId()).ifPresent(entity::setBookingId);

        if (updateDto.getMethodId() != null) {
            PaymentMethodDbModel method = paymentMethodRepository.findById(updateDto.getMethodId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(PaymentMethodDbModel.class, updateDto.getMethodId()));
            entity.setMethod(method);
        }

        PaymentDbModel updated = paymentRepository.save(entity);

        // ADD NOTIFICATION: Payment Status Changed
        if (!oldStatus.equals(updated.getStatus())) {
            try {
                String statusMessage = getStatusMessage(updated.getStatus());
                notificationService.sendNotificationByBookingId(
                        "TYPE_PAYMENT_STATUS_CHANGED",
                        "Trạng thái thanh toán thay đổi",
                        "Trạng thái thanh toán của bạn đã thay đổi: " + statusMessage +
                                ". Mã giao dịch: " + updated.getTransactionCode(),
                        updated.getBookingId(),
                        "APP",
                        true, // sendEmail
                        true // shouldSave
                );
            } catch (Exception e) {
                log.error("Failed to send payment status change notification: {}", e.getMessage(), e);
            }
        }

        return paymentMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        paymentRepository.softDeleteById(id);
    }

    @Override
    public void activate(UUID id) {
        PaymentDbModel entity = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PaymentDbModel.class, id));
        entity.setIsDeleted(false);
        paymentRepository.save(entity);
    }

    @Override
    public void deactivate(UUID id) {
        PaymentDbModel entity = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PaymentDbModel.class, id));
        entity.setIsDeleted(true);
        paymentRepository.save(entity);
    }

    @Override
    public List<PaymentSelectResponse> findAllForSelect() {
        List<PaymentDbModel> entities = paymentRepository.findAllNotDeleted();
        return entities.stream().map(paymentMapper::toSelectResponse).toList();
    }

    @Override
    public Page<PaymentSelectResponse> findAllForSelect(PaymentQuery query) {
        Specification<PaymentDbModel> spec = buildSpecification(query);
        Pageable pageable = buildPageable(query);
        Page<PaymentDbModel> entities = paymentRepository.findAll(spec, pageable);
        return entities.map(paymentMapper::toSelectResponse);
    }

    @Override
    public PaymentResponse updateStatusByTransactionCode(String transactionCode, String status, LocalDateTime paidAt) {
        PaymentDbModel entity = paymentRepository.findByTransactionCodeAndIsDeletedFalse(transactionCode)
                .orElseThrow(() -> new ResourceNotFoundException(PaymentDbModel.class, transactionCode));

        PaymentUpdate updateDto = new PaymentUpdate();
        updateDto.setStatus(status);
        updateDto.setPaidAt(paidAt);

        return update(entity.getId(), updateDto);
    }

    @Override
    public void deleteByTransactionCode(String transactionCode) {
        paymentRepository.findByTransactionCodeAndIsDeletedFalse(transactionCode)
                .ifPresent(paymentRepository::softDelete);
    }

    @Override
    public PaymentResponse findByTransactionCode(String transactionCode) {
        PaymentDbModel entity = paymentRepository.findByTransactionCodeAndIsDeletedFalse(transactionCode)
                .orElse(null);
        return entity != null ? paymentMapper.toResponse(entity) : null;
    }

    private Specification<PaymentDbModel> buildSpecification(PaymentQuery query) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.getBookingId() != null) {
                predicates.add(cb.equal(root.get("bookingId"), query.getBookingId()));
            }
            if (query.getMethodId() != null) {
                predicates.add(cb.equal(root.get("method").get("id"), query.getMethodId()));
            }
            if (query.getStatus() != null && !query.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }
            if (query.getTransactionCode() != null && !query.getTransactionCode().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("transactionCode")),
                        "%" + query.getTransactionCode().toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable buildPageable(PaymentQuery query) {
        Sort.Direction direction = Sort.Direction.fromString(query.getSortDirection());
        Sort sort = Sort.by(direction, query.getSortBy());
        return PageRequest.of(query.getPage(), query.getSize(), sort);
    }

    private String getStatusMessage(String status) {
        return switch (status.toUpperCase()) {
            case "SUCCESS" -> "Thành công";
            case "FAILED" -> "Thất bại";
            case "PENDING" -> "Đang chờ xử lý";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }
}
