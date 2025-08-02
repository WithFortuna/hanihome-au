package com.hanihome.hanihome_au_api.domain.user.exception;

import com.hanihome.hanihome_au_api.domain.shared.exception.DomainException;

public class UserException extends DomainException {
    
    public UserException(String message) {
        super(message);
    }
    
    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static class UserAlreadyExistsException extends UserException {
        public UserAlreadyExistsException(String email) {
            super("User with email already exists: " + email);
        }
    }
    
    public static class UserNotFoundException extends UserException {
        public UserNotFoundException(Long userId) {
            super("User not found with ID: " + userId);
        }
    }
    
    public static class InvalidEmailException extends UserException {
        public InvalidEmailException(String email) {
            super("Invalid email format: " + email);
        }
    }
}