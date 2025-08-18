package com.hanihome.hanihome_au_api.common.exception;

import com.hanihome.hanihome_au_api.application.monitoring.MetricsService;
import com.hanihome.hanihome_au_api.config.SentryConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 글로벌 예외 처리 및 모니터링 메트릭 수집
 * 
 * 주요 기능:
 * - 예외별 메트릭 수집
 * - Sentry 에러 리포팅
 * - 표준화된 에러 응답 생성
 */
@RestControllerAdvice
@Order(1) // 다른 ExceptionHandler보다 우선순위를 높게 설정
public class MonitoringExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringExceptionHandler.class);

    private final MetricsService metricsService;
    private final MeterRegistry meterRegistry;
    
    // 에러 카운터들
    private final Counter validationErrorCounter;
    private final Counter authenticationErrorCounter;
    private final Counter authorizationErrorCounter;
    private final Counter businessErrorCounter;
    private final Counter systemErrorCounter;
    private final Counter notFoundErrorCounter;

    @Autowired
    public MonitoringExceptionHandler(MetricsService metricsService, MeterRegistry meterRegistry) {
        this.metricsService = metricsService;
        this.meterRegistry = meterRegistry;
        
        // 에러 카운터 초기화
        this.validationErrorCounter = Counter.builder("hanihome.errors.validation")
            .description("Validation error count")
            .register(meterRegistry);
            
        this.authenticationErrorCounter = Counter.builder("hanihome.errors.authentication")
            .description("Authentication error count")
            .register(meterRegistry);
            
        this.authorizationErrorCounter = Counter.builder("hanihome.errors.authorization")
            .description("Authorization error count")
            .register(meterRegistry);
            
        this.businessErrorCounter = Counter.builder("hanihome.errors.business")
            .description("Business logic error count")
            .register(meterRegistry);
            
        this.systemErrorCounter = Counter.builder("hanihome.errors.system")
            .description("System error count")
            .register(meterRegistry);
            
        this.notFoundErrorCounter = Counter.builder("hanihome.errors.not_found")
            .description("Not found error count")
            .register(meterRegistry);
    }

    /**
     * Validation 에러 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        validationErrorCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "endpoint", getEndpoint(request),
                "method", request.getMethod()
            )
        );
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            validationErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Input validation failed")
            .path(request.getRequestURI())
            .details(validationErrors)
            .build();
            
        // Sentry에 경고 레벨로 리포트
        SentryConfig.captureBusinessException(
            "Validation error at " + getEndpoint(request), 
            ex, 
            Map.of(
                "endpoint", getEndpoint(request),
                "validation_errors", validationErrors.toString()
            )
        );
        
        logger.warn("Validation error at {}: {}", getEndpoint(request), validationErrors);
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Constraint Violation 에러 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
            
        validationErrorCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "endpoint", getEndpoint(request),
                "method", request.getMethod(),
                "type", "constraint_violation"
            )
        );
        
        Map<String, String> constraintErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> 
            constraintErrors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Constraint Violation")
            .message("Request constraint validation failed")
            .path(request.getRequestURI())
            .details(constraintErrors)
            .build();
            
        logger.warn("Constraint violation at {}: {}", getEndpoint(request), constraintErrors);
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 인증 에러 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
            
        authenticationErrorCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "endpoint", getEndpoint(request),
                "method", request.getMethod(),
                "auth_type", ex.getClass().getSimpleName()
            )
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Authentication Failed")
            .message("Authentication required")
            .path(request.getRequestURI())
            .build();
            
        // 보안 이벤트로 Sentry에 리포트
        SentryConfig.captureSecurityEvent(
            "Authentication failure at " + getEndpoint(request),
            getUserId(request),
            "AUTHENTICATION_FAILED"
        );
        
        logger.warn("Authentication failed at {} for user {}: {}", 
                   getEndpoint(request), getUserId(request), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 인가 에러 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
            
        authorizationErrorCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "endpoint", getEndpoint(request),
                "method", request.getMethod(),
                "user_id", getUserId(request)
            )
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error("Access Denied")
            .message("Insufficient permissions")
            .path(request.getRequestURI())
            .build();
            
        // 보안 이벤트로 Sentry에 리포트
        SentryConfig.captureSecurityEvent(
            "Access denied at " + getEndpoint(request),
            getUserId(request),
            "ACCESS_DENIED"
        );
        
        logger.warn("Access denied at {} for user {}: {}", 
                   getEndpoint(request), getUserId(request), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * 404 에러 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
            
        notFoundErrorCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "endpoint", getEndpoint(request),
                "method", request.getMethod()
            )
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message("The requested resource was not found")
            .path(request.getRequestURI())
            .build();
            
        logger.debug("Resource not found: {}", getEndpoint(request));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 비즈니스 로직 에러 처리 (커스텀 예외들)
     */
    @ExceptionHandler({
        IllegalArgumentException.class,
        IllegalStateException.class
    })
    public ResponseEntity<ErrorResponse> handleBusinessException(
            RuntimeException ex, HttpServletRequest request) {
            
        businessErrorCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "endpoint", getEndpoint(request),
                "method", request.getMethod(),
                "exception_type", ex.getClass().getSimpleName()
            )
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Business Logic Error")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();
            
        // 비즈니스 예외로 Sentry에 리포트
        SentryConfig.captureBusinessException(
            "Business error at " + getEndpoint(request), 
            ex,
            Map.of(
                "endpoint", getEndpoint(request),
                "user_id", getUserId(request),
                "exception_type", ex.getClass().getSimpleName()
            )
        );
        
        logger.warn("Business error at {} for user {}: {}", 
                   getEndpoint(request), getUserId(request), ex.getMessage());
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 시스템 에러 처리 (예상치 못한 모든 에러)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleSystemException(
            Exception ex, HttpServletRequest request) {
            
        systemErrorCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "endpoint", getEndpoint(request),
                "method", request.getMethod(),
                "exception_type", ex.getClass().getSimpleName()
            )
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .path(request.getRequestURI())
            .build();
            
        // 시스템 에러는 Sentry에 에러 레벨로 자동 리포트됨
        logger.error("Unexpected error at {} for user {}", 
                    getEndpoint(request), getUserId(request), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // === 유틸리티 메서드들 ===

    private String getEndpoint(HttpServletRequest request) {
        return request.getMethod() + " " + request.getRequestURI();
    }

    private String getUserId(HttpServletRequest request) {
        // SecurityContext에서 사용자 ID 추출
        try {
            return request.getRemoteUser() != null ? request.getRemoteUser() : "anonymous";
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 표준화된 에러 응답 DTO
     */
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private Map<String, Object> details;

        // Builder 패턴 구현
        public static ErrorResponseBuilder builder() {
            return new ErrorResponseBuilder();
        }

        public static class ErrorResponseBuilder {
            private LocalDateTime timestamp;
            private int status;
            private String error;
            private String message;
            private String path;
            private Map<String, Object> details;

            public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public ErrorResponseBuilder status(int status) {
                this.status = status;
                return this;
            }

            public ErrorResponseBuilder error(String error) {
                this.error = error;
                return this;
            }

            public ErrorResponseBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ErrorResponseBuilder path(String path) {
                this.path = path;
                return this;
            }

            public ErrorResponseBuilder details(Map<String, ? extends Object> details) {
                this.details = new HashMap<>(details);
                return this;
            }

            public ErrorResponse build() {
                ErrorResponse response = new ErrorResponse();
                response.timestamp = this.timestamp;
                response.status = this.status;
                response.error = this.error;
                response.message = this.message;
                response.path = this.path;
                response.details = this.details;
                return response;
            }
        }

        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public Map<String, Object> getDetails() { return details; }
    }
}