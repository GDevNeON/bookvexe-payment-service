package com.example.bookvexe_payment_service.services.core;

import com.example.bookvexe_payment_service.models.dto.context.BookingContextInfo;
import com.example.bookvexe_payment_service.models.dto.context.UserContextInfo;
import com.example.bookvexe_payment_service.repositories.core.CoreDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoreDataService {

    private final CoreDataRepository coreDataRepository;

    /**
     * Retrieves all comprehensive context information for a given booking ID
     * by querying the external Core database.
     */
    public Optional<BookingContextInfo> getBookingContextInfo(UUID bookingId) {
        if (bookingId == null) {
            log.warn("Attempted to query context info with null booking ID.");
            return Optional.empty();
        }

        Optional<BookingContextInfo> info = coreDataRepository.findBookingContextInfo(bookingId);

        if (info.isEmpty()) {
            log.warn("Could not retrieve context info for booking ID: {}. Booking not found in Core DB.", bookingId);
        }

        return info;
    }

    /**
     * Retrieves user context information (email, phone, etc.) for a given user ID.
     */
    public Optional<UserContextInfo> getUserContextInfo(UUID userId) {
        if (userId == null) {
            log.warn("Attempted to query user context info with null user ID.");
            return Optional.empty();
        }

        Optional<UserContextInfo> info = coreDataRepository.findUserContextInfo(userId);

        if (info.isEmpty()) {
            log.warn("Could not retrieve user context info for user ID: {}. User not found in Core DB.", userId);
        }

        return info;
    }

    /**
     * Retrieves user context information using either userId or bookingId as reference.
     * Priority: bookingId > userId
     */
    public Optional<UserContextInfo> getUserContextInfo(UUID userId, UUID bookingId) {
        if (bookingId == null && userId == null) {
            log.warn("Attempted to query user context info with both null user ID and booking ID.");
            return Optional.empty();
        }

        Optional<UserContextInfo> info = coreDataRepository.findUserContextInfo(userId, bookingId);

        if (info.isEmpty()) {
            log.warn("Could not retrieve user context info for user ID: {} and booking ID: {}", userId, bookingId);
        }

        return info;
    }
}