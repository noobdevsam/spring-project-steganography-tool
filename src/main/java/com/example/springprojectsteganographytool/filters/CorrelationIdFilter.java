package com.example.springprojectsteganographytool.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements Filter {

    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws IOException, ServletException {
        try {
            // Reuse incoming header if already provided, else generate a new one
            var existing = (servletRequest instanceof HttpServletRequest httpServletRequest)
                    ? httpServletRequest.getHeader("X-Trace-Id") : null;
            var traceId = (existing != null && !existing.isBlank())
                    ? existing : UUID.randomUUID().toString();

            MDC.put(TRACE_ID_KEY, traceId);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
