package com.example.bookvexe_payment_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(Class<?> clazz, Object id) {
        super(getResourceName(clazz) + " not found with id: " + id);
    }

    private static String getResourceName(Class<?> clazz) {
        String simpleName = clazz.getSimpleName();
        if (simpleName.endsWith("DbModel")) {
            return simpleName.substring(0, simpleName.length() - "DbModel".length());
        }
        return simpleName;
    }
}
