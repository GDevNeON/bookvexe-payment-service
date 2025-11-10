package com.example.bookvexe_payment_service.repositories.core;

import com.example.bookvexe_payment_service.models.dto.context.BookingContextInfo;
import com.example.bookvexe_payment_service.models.dto.context.UserContextInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CoreDataRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Executes a native SQL query to retrieve all linked IDs and contact info for a Booking ID.
     * Uses the same working SQL as before but maps to new structure.
     */
    public Optional<BookingContextInfo> findBookingContextInfo(UUID bookingId) {
        final String sql = """
                SELECT
                    b.uuid AS bookingId,
                    b.trip_id AS tripId,
                    c.uuid AS customerId,
                    c.email AS customerEmail,
                    c.phone AS customerPhone,
                    u.uuid AS userId,
                    e.uuid AS employeeId,
                    e.email AS employeeEmail,
                    e.phone AS employeePhone
                FROM bookings b
                JOIN customer c ON b.customer_id = c.id
                LEFT JOIN users u ON u.customer_id = c.id
                LEFT JOIN employee e ON u.employee_id = e.id
                WHERE b.uuid = ?;
            """;

        try {
            BookingContextInfo info = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                // Create UserContextInfo (without user email/phone since users table doesn't have them)
                UserContextInfo userContext = new UserContextInfo((UUID) rs.getObject("userId"), (UUID) rs.getObject("customerId"), rs.getString("customerEmail"), rs.getString("customerPhone"), (UUID) rs.getObject("employeeId"), rs.getString("employeeEmail"), rs.getString("employeePhone"));

                // Create BookingContextInfo with the user context
                return new BookingContextInfo((UUID) rs.getObject("bookingId"), (UUID) rs.getObject("tripId"), userContext);
            }, bookingId);
            return Optional.ofNullable(info);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Executes a native SQL query to retrieve email and phone information for a User ID.
     */
    public Optional<UserContextInfo> findUserContextInfo(UUID userId) {
        final String sql = """
                SELECT
                    u.uuid AS userId,
                    c.uuid AS customerId,
                    c.email AS customerEmail,
                    c.phone AS customerPhone,
                    e.uuid AS employeeId,
                    e.email AS employeeEmail,
                    e.phone AS employeePhone
                FROM users u
                LEFT JOIN customer c ON u.customer_id = c.uuid     -- Get Customer linked to User
                LEFT JOIN employee e ON u.employee_id = e.uuid     -- Get Employee linked to User
                WHERE u.uuid = ?;
            """;

        try {
            UserContextInfo info = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new UserContextInfo((UUID) rs.getObject("userId"), (UUID) rs.getObject("customerId"), rs.getString("customerEmail"), rs.getString("customerPhone"), (UUID) rs.getObject("employeeId"), rs.getString("employeeEmail"), rs.getString("employeePhone")), userId);
            return Optional.ofNullable(info);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Enhanced method that can fetch context info from either userId or bookingId
     */
    public Optional<UserContextInfo> findUserContextInfo(UUID userId, UUID bookingId) {
        // Priority 1: If we have bookingId, try to get most accurate info
        if (bookingId != null) {
            Optional<BookingContextInfo> bookingContext = findBookingContextInfo(bookingId);
            if (bookingContext.isPresent()) {
                return Optional.of(bookingContext.get().userContext());
            }
        }

        // Priority 2: Fall back to user lookup
        if (userId != null) {
            return findUserContextInfo(userId);
        }

        return Optional.empty();
    }
}