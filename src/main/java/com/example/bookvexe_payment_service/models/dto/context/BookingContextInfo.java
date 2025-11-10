package com.example.bookvexe_payment_service.models.dto.context;

import java.util.UUID;

public record BookingContextInfo(
    // Core IDs
    UUID bookingId, UUID tripId,

    // User context information (composition)
    UserContextInfo userContext) {
    /**
     * Convenience getters to maintain backward compatibility
     */
    public UUID userId() {
        return userContext != null ? userContext.userId() : null;
    }

    public UUID customerId() {
        return userContext != null ? userContext.customerId() : null;
    }

    public String customerEmail() {
        return userContext != null ? userContext.customerEmail() : null;
    }

    public String customerPhone() {
        return userContext != null ? userContext.customerPhone() : null;
    }

    public UUID employeeId() {
        return userContext != null ? userContext.employeeId() : null;
    }

    public String employeeEmail() {
        return userContext != null ? userContext.employeeEmail() : null;
    }

    public String employeePhone() {
        return userContext != null ? userContext.employeePhone() : null;
    }

    /**
     * Get best available email from user context
     */
    public String getBestEmail() {
        return userContext != null ? userContext.getBestEmail() : null;
    }

    /**
     * Get best available phone from user context
     */
    public String getBestPhone() {
        return userContext != null ? userContext.getBestPhone() : null;
    }

    /**
     * Constructor for backward compatibility - creates UserContextInfo internally
     */
    public BookingContextInfo(UUID bookingId, UUID tripId, UUID userId, UUID customerId, String customerEmail, String customerPhone, UUID employeeId, String employeeEmail, String employeePhone) {
        this(bookingId, tripId, new UserContextInfo(userId, customerId, customerEmail, customerPhone, employeeId, employeeEmail, employeePhone));
    }
}