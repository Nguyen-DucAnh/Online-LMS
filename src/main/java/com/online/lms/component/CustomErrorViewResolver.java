package com.online.lms.component;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static com.online.lms.constant.AtributeNameConstant.MESSAGE;
import static com.online.lms.constant.AtributeNameConstant.STATUS;
import static com.online.lms.constant.ErrorMessageConstant.*;

@Component
public class CustomErrorViewResolver implements ErrorViewResolver {


    @Override
    public ModelAndView resolveErrorView(
            HttpServletRequest request,
            HttpStatus status,
            Map<String, Object> model) {

        return switch (status) {
            case NOT_FOUND -> new ModelAndView("error/404", Map.of(MESSAGE, MSG_404));
            case FORBIDDEN -> new ModelAndView("error/403", Map.of(MESSAGE, MSG_403));
            case INTERNAL_SERVER_ERROR -> new ModelAndView("error/500", Map.of(MESSAGE, MSG_500));
            default -> new ModelAndView("error/generic", Map.of(
                    MESSAGE, MSG_GENERIC,
                    STATUS, status.value()
            ));
        };
    }
}
