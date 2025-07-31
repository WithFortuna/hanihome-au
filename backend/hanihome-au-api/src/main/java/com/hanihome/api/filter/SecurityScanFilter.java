package com.hanihome.api.filter;

import com.hanihome.api.service.SecurityScanService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

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

    private String extractClientIp(HttpServletRequest request) {
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
}