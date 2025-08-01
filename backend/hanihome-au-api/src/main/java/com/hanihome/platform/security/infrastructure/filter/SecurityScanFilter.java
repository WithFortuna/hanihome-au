package com.hanihome.platform.security.infrastructure.filter;

import com.hanihome.platform.system.infrastructure.security.SecurityScanService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Security Scan Filter
 * Migrated from legacy api package to new DDD structure
 * Provides request scanning and IP blocking functionality
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityScanFilter implements Filter {

    private final SecurityScanService securityScanService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = extractClientIp(httpRequest);
        
        // Check if IP is blocked
        if (securityScanService.isIpBlocked(clientIp)) {
            log.warn("Blocked request from IP: {} to {}", clientIp, httpRequest.getRequestURI());
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().write("{\"error\":\"Access denied\",\"code\":\"IP_BLOCKED\"}");
            httpResponse.setContentType("application/json");
            return;
        }
        
        // Scan request for security threats
        boolean threatDetected = securityScanService.scanRequest(httpRequest);
        
        if (threatDetected) {
            log.warn("Security threat detected from IP: {} to {}", clientIp, httpRequest.getRequestURI());
            
            // Check if this IP should be blocked
            securityScanService.checkAndBlockSuspiciousIp(clientIp);
            
            // Return security error response
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.getWriter().write("{\"error\":\"Security threat detected\",\"code\":\"SECURITY_VIOLATION\"}");
            httpResponse.setContentType("application/json");
            return;
        }
        
        // Continue with the request
        chain.doFilter(request, response);
    }

    /**
     * Extract client IP address from request headers
     * Handles various proxy headers and load balancer configurations
     */
    private String extractClientIp(HttpServletRequest request) {
        // Check X-Forwarded-For header (most common proxy header)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in case of multiple IPs
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Check X-Real-IP header (nginx and other proxies)
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Check X-Forwarded header (less common)
        String xForwarded = request.getHeader("X-Forwarded");
        if (xForwarded != null && !xForwarded.isEmpty()) {
            return xForwarded.split(",")[0].trim();
        }
        
        // Check Proxy-Client-IP header (some proxy servers)
        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty()) {
            return proxyClientIp;
        }
        
        // Check WL-Proxy-Client-IP header (WebLogic proxy)
        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty()) {
            return wlProxyClientIp;
        }
        
        // Check HTTP_CLIENT_IP header
        String httpClientIp = request.getHeader("HTTP_CLIENT_IP");
        if (httpClientIp != null && !httpClientIp.isEmpty()) {
            return httpClientIp;
        }
        
        // Check HTTP_X_FORWARDED_FOR header
        String httpXForwardedFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (httpXForwardedFor != null && !httpXForwardedFor.isEmpty()) {
            return httpXForwardedFor.split(",")[0].trim();
        }
        
        // Fall back to remote address
        return request.getRemoteAddr();
    }
}