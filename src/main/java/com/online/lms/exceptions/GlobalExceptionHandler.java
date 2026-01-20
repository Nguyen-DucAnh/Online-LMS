package com.online.lms.exceptions;

import com.online.lms.exceptions.user.InvalidUserRoleException;
import com.online.lms.exceptions.user.InvalidUserStatusException;
import jakarta.transaction.InvalidTransactionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.online.lms.constant.AtributeNameConstant.MESSAGE;
import static com.online.lms.constant.ViewNamesConstant.GENERIC_ERROR;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidUserRoleException.class)
    public String handleInvalidUserRoleException(InvalidUserRoleException ex, Model model) {
        log.warn("Invalid user role: {}", ex.getMessage());
        model.addAttribute(MESSAGE, "The specified user role is not valid.");
        return GENERIC_ERROR;
    }

    @ExceptionHandler(InvalidUserStatusException.class)
    public String handleInvalidUserStatusException(InvalidUserStatusException ex, Model model) {
        log.warn("Invalid user status: {}", ex.getMessage());
        model.addAttribute(MESSAGE, "The specified user status is not valid.");
        return GENERIC_ERROR;
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public String handleInvalidTransactionException(InvalidTransactionException ex, Model model) {
        log.warn("Invalid transaction: {}", ex.getMessage());
        model.addAttribute(MESSAGE, "The specified transaction is not valid.");
        return GENERIC_ERROR;
    }
}
