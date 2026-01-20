package com.online.lms.exceptions.user;

public class InvalidUserRoleException extends RuntimeException {
    public InvalidUserRoleException(String message) {
        super(message);
    }
}
