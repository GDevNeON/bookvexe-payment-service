package com.example.bookvexe_payment_service.repositories.core;

// ... (existing imports)

import com.example.bookvexe_payment_service.models.dto.booking.BookingContextInfo;
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
     */
    public Optional<BookingContextInfo> findBookingContextInfo(UUID bookingId) {

        // This SQL query is robust and handles all requested joins in the Core DB schema.
        final String sql = """
                SELECT
                    b.id AS bookingId,
                    b.tripId AS tripId,
                    c.id AS customerId,
                    c.email AS customerEmail,
                    c.phone AS customerPhone,
                    u.id AS userId,
                    e.id AS employeeId,
                    e.email AS employeeEmail,
                    e.phone AS employeePhone
                FROM bookings b
                JOIN customer c ON b.customerId = c.id       -- Get Customer
                LEFT JOIN users u ON u.customerId = c.id     -- Get User linked to Customer (if any)
                LEFT JOIN employee e ON u.employeeId = e.id  -- Get Employee linked to User (if any)
                WHERE b.id = ?;
            """;

        try {
            BookingContextInfo info = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new BookingContextInfo((UUID) rs.getObject("bookingId"), (UUID) rs.getObject("tripId"), (UUID) rs.getObject("userId"), (UUID) rs.getObject("customerId"), rs.getString("customerEmail"), rs.getString("customerPhone"), (UUID) rs.getObject("employeeId"), rs.getString("employeeEmail"), rs.getString("employeePhone")), bookingId);
            return Optional.ofNullable(info);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}