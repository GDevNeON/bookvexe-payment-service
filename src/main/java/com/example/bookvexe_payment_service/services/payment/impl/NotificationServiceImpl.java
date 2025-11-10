package org.example.bookvexebej2e.services.notification;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.example.bookvexebej2e.exceptions.ForbiddenException;
import org.example.bookvexebej2e.exceptions.ResourceNotFoundException;
import org.example.bookvexebej2e.mappers.NotificationMapper;
import org.example.bookvexebej2e.models.db.*;
import org.example.bookvexebej2e.models.dto.notification.*;
import org.example.bookvexebej2e.repositories.booking.BookingRepository;
import org.example.bookvexebej2e.repositories.notification.NotificationRepository;
import org.example.bookvexebej2e.repositories.notification.NotificationTypeRepository;
import org.example.bookvexebej2e.repositories.trip.TripRepository;
import org.example.bookvexebej2e.repositories.user.UserRepository;
import org.example.bookvexebej2e.services.external.MailingService;
import org.example.bookvexebej2e.services.external.WebSocketService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final UserRepository userRepository;
    private final MailingService mailingService;
    private final WebSocketService webSocketService;
    private final NotificationMapper notificationMapper;

    @Override
    public List<NotificationResponse> findAll() {
        List<NotificationDbModel> entities = notificationRepository.findAllNotDeleted();
        return entities.stream()
            .map(notificationMapper::toResponse)
            .toList();
    }

    @Override
    public Page<NotificationResponse> findAll(NotificationQuery query) {
        Specification<NotificationDbModel> spec = buildSpecification(query, null);
        Pageable pageable = buildPageable(query);
        Page<NotificationDbModel> entities = notificationRepository.findAll(spec, pageable);
        return entities.map(notificationMapper::toResponse);
    }

    @Override
    public NotificationResponse findById(UUID id) {
        NotificationDbModel entity = notificationRepository.findByIdAndNotDeleted(id)
            .orElseThrow(() -> new ResourceNotFoundException(NotificationDbModel.class, id));
        return notificationMapper.toResponse(entity);
    }

    @Override
    public NotificationResponse create(NotificationCreate createDto) {
        NotificationDbModel entity = new NotificationDbModel();
        entity.setChannel(createDto.getChannel());
        entity.setTitle(createDto.getTitle());
        entity.setMessage(createDto.getMessage());
        entity.setIsSent(createDto.getIsSent());
        entity.setSentAt(createDto.getSentAt());

        UserDbModel user = userRepository.findById(createDto.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException(UserDbModel.class, createDto.getUserId()));
        entity.setUser(user);

        if (createDto.getBookingId() != null) {
            BookingDbModel booking = bookingRepository.findById(createDto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException(BookingDbModel.class, createDto.getBookingId()));
            entity.setBooking(booking);
        }

        if (createDto.getTripId() != null) {
            TripDbModel trip = tripRepository.findById(createDto.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException(TripDbModel.class, createDto.getTripId()));
            entity.setTrip(trip);
        }

        NotificationTypeDbModel type = notificationTypeRepository.findById(createDto.getTypeId())
            .orElseThrow(() -> new ResourceNotFoundException(NotificationTypeDbModel.class, createDto.getTypeId()));
        entity.setType(type);

        NotificationDbModel savedEntity = notificationRepository.save(entity);
        return notificationMapper.toResponse(savedEntity);
    }

    @Override
    public NotificationResponse update(UUID id, NotificationUpdate updateDto) {
        NotificationDbModel entity = notificationRepository.findByIdAndNotDeleted(id)
            .orElseThrow(() -> new ResourceNotFoundException(NotificationDbModel.class, id));

        entity.setChannel(updateDto.getChannel());
        entity.setTitle(updateDto.getTitle());
        entity.setMessage(updateDto.getMessage());
        entity.setIsSent(updateDto.getIsSent());
        entity.setSentAt(updateDto.getSentAt());

        if (updateDto.getUserId() != null) {
            UserDbModel user = userRepository.findById(updateDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(UserDbModel.class, updateDto.getUserId()));
            entity.setUser(user);
        }

        if (updateDto.getBookingId() != null) {
            BookingDbModel booking = bookingRepository.findById(updateDto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException(BookingDbModel.class, updateDto.getBookingId()));
            entity.setBooking(booking);
        }

        if (updateDto.getTripId() != null) {
            TripDbModel trip = tripRepository.findById(updateDto.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException(TripDbModel.class, updateDto.getTripId()));
            entity.setTrip(trip);
        }

        if (updateDto.getTypeId() != null) {
            NotificationTypeDbModel type = notificationTypeRepository.findById(updateDto.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(NotificationTypeDbModel.class, updateDto.getTypeId()));
            entity.setType(type);
        }

        NotificationDbModel updatedEntity = notificationRepository.save(entity);
        return notificationMapper.toResponse(updatedEntity);
    }

    @Override
    public void delete(UUID id) {
        notificationRepository.softDeleteById(id);
    }

    @Override
    public void activate(UUID id) {
        NotificationDbModel entity = notificationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(NotificationDbModel.class, id));
        entity.setIsDeleted(false);
        notificationRepository.save(entity);
    }

    @Override
    public void deactivate(UUID id) {
        NotificationDbModel entity = notificationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(NotificationDbModel.class, id));
        entity.setIsDeleted(true);
        notificationRepository.save(entity);
    }

    @Override
    public List<NotificationSelectResponse> findAllForSelect() {
        List<NotificationDbModel> entities = notificationRepository.findAllNotDeleted();
        return entities.stream()
            .map(notificationMapper::toSelectResponse)
            .toList();
    }

    @Override
    public Page<NotificationSelectResponse> findAllForSelect(NotificationQuery query) {
        Specification<NotificationDbModel> spec = buildSpecification(query, null);
        Pageable pageable = buildPageable(query);
        Page<NotificationDbModel> entities = notificationRepository.findAll(spec, pageable);
        return entities.map(notificationMapper::toSelectResponse);
    }

    /**
     * High-level service method to send a notification, optionally saving it to the database
     * and always pushing it via WebSocket and email (if requested), resolved from userId.
     */
    @Transactional
    public NotificationResponse sendNotification(UUID userId, String typeCode, String title, String message,
        UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave) {

        return sendNotificationInternal(userId, null, typeCode, title, message, bookingId, tripId, channel, sendEmail,
            shouldSave);
    }

    /**
     * Overload method that allows specifying an explicit email address for sending the email
     * notification, bypassing the lookup from the userId.
     */
    @Transactional
    public NotificationResponse sendNotification(UUID userId, String toEmail, String typeCode, String title,
        String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave) {

        return sendNotificationInternal(userId, toEmail, typeCode, title, message, bookingId, tripId, channel,
            sendEmail, shouldSave);
    }

    // -------------------------------------------------------------
    // Internal Method to Avoid Code Repetition
    // -------------------------------------------------------------

    private NotificationResponse sendNotificationInternal(UUID userId, String toEmail, String typeCode, String title,
        String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave) {

        NotificationDbModel entity;

        if (Boolean.TRUE.equals(shouldSave)) {
            // 1. Save Notification (Persistence)
            entity = saveNotification(userId, typeCode, title, message, bookingId, tripId, channel);
            log.info("Notification saved and sent to user {}. ID: {}", userId, entity.getId());
        } else {
            // 1b. Create unsaved entity for response and logging (No Persistence)
            entity = createUnsavedNotification(userId, typeCode, title, message, bookingId, tripId, channel);
            log.info("DEBUG Notification created (unsaved) and sent to user {}.", userId);
        }

        // 2. Send Email if requested
        if (Boolean.TRUE.equals(sendEmail)) {
            if (toEmail != null && !toEmail.isBlank()) {
                // Use explicit email if provided
                mailingService.sendEmail(toEmail, title, message);
            } else {
                // Fallback to user ID lookup
                mailingService.sendEmailToUser(userId, title, message);
            }
        }

        // 3. Ping Frontend via WebSocket
        webSocketService.notifyUser(userId, "NEW_NOTIFICATION");

        return notificationMapper.toResponse(entity);
    }

    // Helper for unsaved response
    private NotificationDbModel createUnsavedNotification(UUID userId, String typeCode, String title, String message,
        UUID bookingId, UUID tripId, String channel) {

        NotificationDbModel entity = new NotificationDbModel();
        entity.setId(UUID.randomUUID()); // Give it a temporary ID
        entity.setChannel(channel);
        entity.setTitle(title);
        entity.setMessage(message);
        entity.setIsRead(false);
        entity.setIsSent(true);
        entity.setSentAt(LocalDateTime.now());
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);

        if (typeCode != null && !typeCode.trim()
            .isEmpty()) {
            NotificationTypeDbModel type = notificationTypeRepository.findByCode(typeCode)
                .orElseThrow(() -> new ResourceNotFoundException(NotificationTypeDbModel.class, typeCode));
            entity.setType(type);
        }

        if (bookingId != null) {
            BookingDbModel booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(BookingDbModel.class, bookingId));
            entity.setBooking(booking);
        }

        if (tripId != null) {
            TripDbModel trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException(TripDbModel.class, tripId));
            entity.setTrip(trip);
        }

        return entity;
    }

    /**
     * Saves the notification entity to the database.
     */
    private NotificationDbModel saveNotification(UUID userId, String typeCode, String title, String message,
        UUID bookingId, UUID tripId, String channel) {

        UserDbModel user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(UserDbModel.class, userId));

        if (Strings.CI.contains(typeCode, "DEPARTURE") && Strings.CI.contains(typeCode, "REMINDER")) {
            Optional<NotificationDbModel> existing = notificationRepository.findExistingReminder(userId, bookingId,
                tripId, typeCode);

            if (existing.isPresent()) {
                log.info("Notification already sent for user={}, booking={}, trip={}", userId, bookingId, tripId);
                return existing.get();
            }
        }

        NotificationDbModel entity = new NotificationDbModel();
        entity.setUser(user);
        entity.setChannel(channel != null ? channel : "APP");
        entity.setTitle(title);
        entity.setMessage(message);
        entity.setIsRead(false);
        entity.setIsSent(true);
        entity.setSentAt(LocalDateTime.now());

        if (typeCode != null && !typeCode.trim()
            .isEmpty()) {
            NotificationTypeDbModel type = notificationTypeRepository.findByCode(typeCode)
                .orElseThrow(() -> new ResourceNotFoundException(NotificationTypeDbModel.class, typeCode));
            entity.setType(type);
        }

        if (bookingId != null) {
            BookingDbModel booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(BookingDbModel.class, bookingId));
            entity.setBooking(booking);
        }

        if (tripId != null) {
            TripDbModel trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException(TripDbModel.class, tripId));
            entity.setTrip(trip);
        }

        return notificationRepository.save(entity);
    }

    @Override
    public Page<NotificationResponse> getMyNotifications(UUID userId, NotificationQuery query) {
        Specification<NotificationDbModel> spec = buildSpecification(query, false);
        Pageable pageable = buildPageable(query);
        Page<NotificationDbModel> entities = notificationRepository.findAll(spec, pageable);
        return entities.map(notificationMapper::toResponse);
    }

    @Override
    @Transactional
    public void markNotificationAsRead(UUID notificationId, UUID userId) {
        NotificationDbModel entity = notificationRepository.findByIdAndNotDeleted(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException(NotificationDbModel.class, notificationId));

        // Security check: only the owner can mark as read
        if (!entity.getUser()
            .getId()
            .equals(userId)) {
            throw new ForbiddenException("You do not have permission to modify this notification.");
        }

        entity.setIsRead(true);
        notificationRepository.save(entity);

        // Ping frontend to update unread count
        webSocketService.notifyUser(userId, "READ_NOTIFICATION");
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        NotificationDbModel entity = notificationRepository.findByIdAndNotDeleted(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException(NotificationDbModel.class, notificationId));

        // Security check: only the owner can delete
        if (!entity.getUser()
            .getId()
            .equals(userId)) {
            throw new ForbiddenException("You do not have permission to delete this notification.");
        }

        notificationRepository.softDeleteById(notificationId);

        // Ping frontend to remove notification from list
        webSocketService.notifyUser(userId, "DELETED_NOTIFICATION");
    }

    @Override
    public int countUnreadNotifications(UUID userId) {
        return (int) notificationRepository.count((root, cq, cb) -> cb.and(cb.equal(root.get("user")
            .get("id"), userId), cb.isFalse(root.get("isRead")), cb.isFalse(root.get("isDeleted"))));
    }

    private Specification<NotificationDbModel> buildSpecification(NotificationQuery query, Boolean isDeletedFilter) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isDeletedFilter != null) {
                // If isDeletedFilter is provided (true or false), apply it directly.
                predicates.add(cb.equal(root.get("isDeleted"), isDeletedFilter));
            }

            if (query.getUserId() != null) {
                predicates.add(cb.equal(root.get("user")
                    .get("id"), query.getUserId()));
            }
            if (query.getBookingId() != null) {
                predicates.add(cb.equal(root.get("booking")
                    .get("id"), query.getBookingId()));
            }
            if (query.getTripId() != null) {
                predicates.add(cb.equal(root.get("trip")
                    .get("id"), query.getTripId()));
            }
            if (query.getTypeId() != null) {
                predicates.add(cb.equal(root.get("type")
                    .get("id"), query.getTypeId()));
            }
            if (query.getChannel() != null && !query.getChannel()
                .isEmpty()) {
                predicates.add(cb.equal(root.get("channel"), query.getChannel()));
            }
            if (query.getIsSent() != null) {
                predicates.add(cb.equal(root.get("isSent"), query.getIsSent()));
            }
            if (query.getIsRead() != null) {
                predicates.add(cb.equal(root.get("isRead"), query.getIsRead()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable buildPageable(NotificationQuery query) {
        Sort.Direction direction = Sort.Direction.fromString(query.getSortDirection());
        Sort sort = Sort.by(direction, query.getSortBy());
        return PageRequest.of(query.getPage(), query.getSize(), sort);
    }
}
