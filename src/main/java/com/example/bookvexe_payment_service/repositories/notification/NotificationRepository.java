package org.example.bookvexebej2e.repositories.notification;


import org.example.bookvexebej2e.models.db.NotificationDbModel;
import org.example.bookvexebej2e.repositories.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends BaseRepository<NotificationDbModel> {
    @Query("""
    SELECT n FROM NotificationDbModel n
    WHERE n.user.id = :userId
      AND (:bookingId IS NULL OR n.booking.id = :bookingId)
      AND (:tripId IS NULL OR n.trip.id = :tripId)
      AND n.type.code = :typeCode
    """) // NO isDeleted check, finds any record matching the criteria
    Optional<NotificationDbModel> findExistingReminder(
        @Param("userId") UUID userId,
        @Param("bookingId") UUID bookingId,
        @Param("tripId") UUID tripId,
        @Param("typeCode") String typeCode
    );
}
