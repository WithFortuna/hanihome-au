package com.hanihome.api.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanihome.api.annotation.RateLimit;
import com.hanihome.api.service.RateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        
        if (rateLimit == null) {
            return true;
        }

        String identifier = buildIdentifier(request, rateLimit);
        Duration window = Duration.ofSeconds(rateLimit.windowSeconds());
        
        if (rateLimitingService.isRateLimited(identifier, rateLimit.value(), window)) {
            handleRateLimitExceeded(response, rateLimit.message());
            return false;
        }

        return true;
    }

    private String buildIdentifier(HttpServletRequest request, RateLimit rateLimit) {
        switch (rateLimit.scope()) {
            case IP:
                return getClientIpAddress(request);
            case USER:
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()) {
                    return "user:" + auth.getName();
                }
                return getClientIpAddress(request); // Fallback to IP for unauthenticated users
            case CUSTOM:
                if (!rateLimit.key().isEmpty()) {
                    return rateLimit.key();
                }
                return getClientIpAddress(request); // Fallback to IP
            default:
                return getClientIpAddress(request);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private void handleRateLimitExceeded(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> errorResponse = Map.of(
            "error", "RATE_LIMIT_EXCEEDED",
            "message", message,
            "status", HttpStatus.TOO_MANY_REQUESTS.value(),
            "timestamp", System.currentTimeMillis()
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
        
        log.warn("Rate limit exceeded: {}", message);
    }
}