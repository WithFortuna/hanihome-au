package com.hanihome.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * Maximum number of requests allowed
     */
    int value() default 5;
    
    /**
     * Time window in seconds
     */
    int windowSeconds() default 60;
    
    /**
     * Rate limit scope: IP, USER, or CUSTOM
     */
    RateLimitScope scope() default RateLimitScope.IP;
    
    /**
     * Custom key expression for CUSTOM scope
     */
    String key() default "";
    
    /**
     * Error message when rate limit is exceeded
     */
    String message() default "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.";
    
    enum RateLimitScope {
        IP,     // Rate limit by IP address
        USER,   // Rate limit by authenticated user
        CUSTOM  // Rate limit by custom key expression
    }
}