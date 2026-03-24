package com.online.lms.component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
public class CustomErrorViewResolver implements ErrorController {

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        Object status = request.getAttribute(jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        if (status != null) {
            try {
                httpStatus = HttpStatus.valueOf(Integer.parseInt(status.toString()));
            } catch (Exception e) {}
        }

        return switch (httpStatus) {
            case NOT_FOUND -> new ModelAndView("error/404", Map.of("message", "Page not found"));
            case FORBIDDEN -> new ModelAndView("error/403", Map.of("message", "Access denied"));
            case INTERNAL_SERVER_ERROR -> new ModelAndView("error/500", Map.of("message", "Internal server error"));
            default -> new ModelAndView("error/generic", Map.of(
                    "message", "An unexpected error occurred",
                    "status", httpStatus.value()
            ));
        };
    }
}
