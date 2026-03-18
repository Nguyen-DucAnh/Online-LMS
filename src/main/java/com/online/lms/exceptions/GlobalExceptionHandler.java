package com.online.lms.exceptions;

import com.online.lms.exceptions.user.InvalidUserRoleException;
import com.online.lms.exceptions.user.InvalidUserStatusException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.InvalidTransactionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidUserRoleException.class)
    public String handleInvalidUserRoleException(InvalidUserRoleException ex, Model model) {
        log.warn("Invalid user role: {}", ex.getMessage());
        model.addAttribute("message", "The specified user role is not valid.");
        return "error/generic";
    }

    @ExceptionHandler(InvalidUserStatusException.class)
    public String handleInvalidUserStatusException(InvalidUserStatusException ex, Model model) {
        log.warn("Invalid user status: {}", ex.getMessage());
        model.addAttribute("message", "The specified user status is not valid.");
        return "error/generic";
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public String handleInvalidTransactionException(InvalidTransactionException ex, Model model) {
        log.warn("Invalid transaction: {}", ex.getMessage());
        model.addAttribute("message", "The specified transaction is not valid.");
        return "error/generic";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, Model model,
                                              HttpServletRequest request) {
        log.error("Max upload size exceeded: {} | URL: {}", ex.getMessage(), request.getRequestURI());
        model.addAttribute("message", "Nội dung hoặc file tải lên quá lớn! Vui lòng giới hạn dưới 500MB.");
        model.addAttribute("status", 413);
        return "error/generic";
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFoundException(Exception ex, HttpServletRequest request) {
        log.warn("Resource not found: {} | URL: {}", ex.getMessage(), request.getRequestURI());
        ModelAndView mav = new ModelAndView("error/404");
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.addObject("message", "Trang bạn tìm kiếm không tồn tại hoặc đã bị xóa.");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(Exception ex, HttpServletRequest request) {
        log.error("UNHANDLED EXCEPTION [{}]: {} | URL: {} | Method: {}",
                ex.getClass().getName(), ex.getMessage(), request.getRequestURI(), request.getMethod(), ex);
        ModelAndView mav = new ModelAndView("error/generic");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        mav.addObject("message", "Lỗi: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
        mav.addObject("status", 500);
        return mav;
    }
}

