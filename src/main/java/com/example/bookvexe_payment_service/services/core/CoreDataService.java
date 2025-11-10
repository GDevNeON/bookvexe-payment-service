package com.example.bookvexe_payment_service.services.core;

import com.example.bookvexe_payment_service.models.dto.booking.BookingContextInfo;
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
     * This is the single source of truth for getting Booking-related IDs and contact info.
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
}