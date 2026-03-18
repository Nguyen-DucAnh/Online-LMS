package com.online.lms.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class DiagnosticsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().contains("/edit") && "POST".equalsIgnoreCase(request.getMethod())) {
            int contentLength = request.getContentLength();
            String contentType = request.getContentType();
            log.info("DIAGNOSTICS - Request to {} | Method: {} | Content-Type: {} | Content-Length: {} bytes ({} KB)",
                    request.getRequestURI(), request.getMethod(), contentType, contentLength, (contentLength / 1024));
        }
        filterChain.doFilter(request, response);
    }
}
