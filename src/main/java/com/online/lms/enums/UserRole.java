package com.online.lms.enums;

import com.online.lms.exceptions.user.InvalidUserRoleException;

public enum UserRole {

    ADMIN,
    INSTRUCTOR,
    CUSTOMER,
    GUEST;

    public static UserRole fromString(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidUserRoleException(e.getMessage());
        }
    }
}
