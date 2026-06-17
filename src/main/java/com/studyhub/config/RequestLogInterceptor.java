package com.studyhub.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import com.studyhub.common.TraceIdHolder;
import java.util.UUID;

public class RequestLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLogInterceptor.class);

    private static final String START_TIME = "startTime";
    private static final long SLOW_REQUEST_TIME = 1000;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());

        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        TraceIdHolder.setTraceId(traceId);
        response.setHeader("X-Trace-Id", traceId);
        log.info("Request start: method={}, uri={}", request.getMethod(), request.getRequestURI());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        Object startTimeObject = request.getAttribute(START_TIME);
        String traceId = TraceIdHolder.getTraceId();

        try {
            if (startTimeObject == null) {
                return;
            }

            long startTime = (Long) startTimeObject;
            long cost = System.currentTimeMillis() - startTime;

            if (cost > SLOW_REQUEST_TIME) {
                log.warn("Slow request: traceId={}, method={}, uri={}, status={}, cost={}ms",
                        traceId,
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        cost);
            } else {
                log.info("Request end: traceId={}, method={}, uri={}, status={}, cost={}ms",
                        traceId,
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        cost);
            }

            if (ex != null) {
                log.error("Request error: traceId={}, method={}, uri={}, message={}",
                        traceId,
                        request.getMethod(),
                        request.getRequestURI(),
                        ex.getMessage(),
                        ex);
            }
        } finally {
            TraceIdHolder.clear();
        }
    }
}