package com.maximorero14.payment.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class RequestLoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        String statusClass = getStatusClass(httpResponse.getStatus());

        // Agregar información al MDC
        MDC.put("requestId", requestId);
        MDC.put("endpoint", httpRequest.getRequestURI());
        MDC.put("httpMethod", httpRequest.getMethod());
        MDC.put("clientIp", getClientIp(httpRequest));
        MDC.put("userAgent", httpRequest.getHeader("User-Agent"));
        MDC.put("httpStatusClass", statusClass); // Para labels

        try {
            // Log del request entrante
            logger.info("Incoming request: {} {}", 
                httpRequest.getMethod(), 
                httpRequest.getRequestURI()
            );
            
            chain.doFilter(request, response);
            
            // Calcular duración
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("durationMs", String.valueOf(duration));
            MDC.put("httpStatus", String.valueOf(httpResponse.getStatus()));
            
            // Log del response
            logger.info("Request completed: {} {} - Status: {} - Duration: {}ms",
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                httpResponse.getStatus(),
                duration
            );
            
        } finally {
            MDC.clear();
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getStatusClass(int status) {
        if (status >= 200 && status < 300) return "2xx";
        if (status >= 300 && status < 400) return "3xx";
        if (status >= 400 && status < 500) return "4xx";
        if (status >= 500) return "5xx";
        return "unknown";
    }
}
