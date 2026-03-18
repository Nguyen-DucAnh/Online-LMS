package com.online.lms.component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Component
public class CustomErrorViewResolver implements ErrorViewResolver {


    @Override
    public ModelAndView resolveErrorView(
            HttpServletRequest request,
            HttpStatus status,
            Map<String, Object> model) {

        return switch (status) {
            case NOT_FOUND -> new ModelAndView("error/404", Map.of("message", "Page not found"));
            case FORBIDDEN -> new ModelAndView("error/403", Map.of("message", "Access denied"));
            case INTERNAL_SERVER_ERROR -> new ModelAndView("error/500", Map.of("message", "Internal server error"));
            default -> new ModelAndView("error/generic", Map.of(
                    "message", "An unexpected error occurred",
                    "status", status.value()
            ));
        };
    }
}
