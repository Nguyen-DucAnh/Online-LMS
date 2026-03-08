package com.online.lms.exceptions;

import com.online.lms.exceptions.user.InvalidUserRoleException;
import com.online.lms.exceptions.user.InvalidUserStatusException;
import jakarta.transaction.InvalidTransactionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
}
