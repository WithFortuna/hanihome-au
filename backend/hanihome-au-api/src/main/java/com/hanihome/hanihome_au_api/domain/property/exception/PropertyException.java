package com.hanihome.hanihome_au_api.domain.property.exception;

import com.hanihome.hanihome_au_api.domain.shared.exception.DomainException;

public class PropertyException extends DomainException {
    
    public PropertyException(String message) {
        super(message);
    }
    
    public PropertyException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static class PropertyNotFoundException extends PropertyException {
        public PropertyNotFoundException(Long propertyId) {
            super("Property not found with ID: " + propertyId);
        }
    }
    
    public static class InvalidPropertyStatusTransitionException extends PropertyException {
        public InvalidPropertyStatusTransitionException(String from, String to) {
            super(String.format("Cannot transition property status from %s to %s", from, to));
        }
    }
    
    public static class PropertyValidationException extends PropertyException {
        public PropertyValidationException(String message) {
            super("Property validation failed: " + message);
        }
    }
    
    public static class UnauthorizedPropertyAccessException extends PropertyException {
        public UnauthorizedPropertyAccessException(Long userId, Long propertyId) {
            super(String.format("User %d is not authorized to access property %d", userId, propertyId));
        }
    }
    
    public static class PropertyStatusException extends PropertyException {
        public PropertyStatusException(String message) {
            super(message);
        }
    }
    
    public static class PropertyAccessDeniedException extends PropertyException {
        public PropertyAccessDeniedException(Long propertyId) {
            super("Access denied to property with ID: " + propertyId);
        }
    }
}