package com.example.springprojectsteganographytool.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * A servlet filter that ensures every request is associated with a unique correlation ID (trace ID).
 * This filter checks for an existing trace ID in the request header and reuses it if present,
 * otherwise generates a new one. The trace ID is stored in the MDC (Mapped Diagnostic Context)
 * for logging and debugging purposes.
 */
@Component
public class CorrelationIdFilter implements Filter {

    private static final String TRACE_ID_KEY = "traceId";

    /**
     * Filters incoming requests to ensure a trace ID is present.
     * If a trace ID is provided in the "X-Trace-Id" header, it is reused; otherwise, a new trace ID is generated.
     * The trace ID is added to the MDC for the duration of the request and removed afterward.
     *
     * @param servletRequest  the incoming servlet request
     * @param servletResponse the outgoing servlet response
     * @param filterChain     the filter chain to pass the request and response to the next filter
     * @throws IOException      if an I/O error occurs during filtering
     * @throws ServletException if a servlet error occurs during filtering
     */
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