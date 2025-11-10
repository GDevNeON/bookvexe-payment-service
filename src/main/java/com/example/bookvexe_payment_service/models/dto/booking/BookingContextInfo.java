package com.example.bookvexe_payment_service.models.dto.booking;

import java.util.UUID;

// Comprehensive DTO to hold all related IDs and contact info
public record BookingContextInfo(
    // Core IDs
    UUID bookingId,
    UUID tripId,
    UUID userId,

    // Customer Info
    UUID customerId,
    String customerEmail,
    String customerPhone,

    // Employee Info (Driver/Agent associated with the User)
    UUID employeeId,
    String employeeEmail,
    String employeePhone
) {}