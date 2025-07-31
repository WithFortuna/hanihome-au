package com.hanihome.api.interceptor;

import com.hanihome.api.service.PerformanceMonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PerformanceMonitoringInterceptor implements HandlerInterceptor {

    private final PerformanceMonitoringService performanceMonitoringService;
    
    private static final String REQUEST_ID_ATTR = "requestId";
    private static final String ENDPOINT_ATTR = "endpoint";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestId = UUID.randomUUID().toString();
        String endpoint = request.getRequestURI();
        
        request.setAttribute(REQUEST_ID_ATTR, requestId);
        request.setAttribute(ENDPOINT_ATTR, endpoint);
        
        performanceMonitoringService.recordRequestStart(endpoint, requestId);
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestId = (String) request.getAttribute(REQUEST_ID_ATTR);
        String endpoint = (String) request.getAttribute(ENDPOINT_ATTR);
        
        if (requestId != null && endpoint != null) {
            boolean isError = response.getStatus() >= 400 || ex != null;
            performanceMonitoringService.recordRequestEnd(endpoint, requestId, isError);
        }
    }
}