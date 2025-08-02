package com.hanihome.hanihome_au_api.presentation.web;

import com.hanihome.hanihome_au_api.domain.property.exception.PropertyException;
import com.hanihome.hanihome_au_api.domain.shared.exception.DomainException;
import com.hanihome.hanihome_au_api.domain.user.exception.UserException;
import com.hanihome.hanihome_au_api.presentation.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserException(UserException e) {
        logger.warn("User domain exception: {}", e.getMessage());
        ApiResponse<Void> response = ApiResponse.error(e.getMessage());
        
        if (e instanceof UserException.UserNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (e instanceof UserException.UserAlreadyExistsException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @ExceptionHandler(PropertyException.class)
    public ResponseEntity<ApiResponse<Void>> handlePropertyException(PropertyException e) {
        logger.warn("Property domain exception: {}", e.getMessage());
        ApiResponse<Void> response = ApiResponse.error(e.getMessage());
        
        if (e instanceof PropertyException.PropertyNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (e instanceof PropertyException.UnauthorizedPropertyAccessException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException e) {
        logger.warn("Domain exception: {}", e.getMessage());
        ApiResponse<Void> response = ApiResponse.error(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.error("Validation failed", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Illegal argument: {}", e.getMessage());
        ApiResponse<Void> response = ApiResponse.error(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        logger.error("Unexpected error occurred", e);
        ApiResponse<Void> response = ApiResponse.error("Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}