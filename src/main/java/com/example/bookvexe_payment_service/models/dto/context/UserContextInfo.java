package com.example.bookvexe_payment_service.models.dto.context;

import java.util.UUID;

public record UserContextInfo(UUID userId,
                              // User table doesn't have email/phone - they're in customer/employee tables
                              UUID customerId, String customerEmail, String customerPhone, UUID employeeId,
                              String employeeEmail, String employeePhone) {
    /**
     * Helper method to get the best available email (priority: customer -> employee)
     */
    public String getBestEmail() {
        if (customerEmail != null && !customerEmail.isBlank()) {
            return customerEmail;
        }
        if (employeeEmail != null && !employeeEmail.isBlank()) {
            return employeeEmail;
        }
        return null;
    }

    /**
     * Helper method to get the best available phone (priority: customer -> employee)
     */
    public String getBestPhone() {
        if (customerPhone != null && !customerPhone.isBlank()) {
            return customerPhone;
        }
        if (employeePhone != null && !employeePhone.isBlank()) {
            return employeePhone;
        }
        return null;
    }
}