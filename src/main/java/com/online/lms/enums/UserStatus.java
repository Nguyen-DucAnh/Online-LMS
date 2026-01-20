package com.online.lms.enums;

import com.online.lms.exceptions.user.InvalidUserStatusException;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
public enum UserStatus {
    ACTIVE("Active", "The user is active and can use the system normally."),
    INACTIVE("InActive", "The user is inactive and can't use the system normally.");

    private String displayName;
    private String description;
    UserStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public static UserStatus fromString(String status) {
        try {
            return UserStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidUserStatusException("Invalid user status: " + status);
        }
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public static List<Map<String, String>> getStatusList() {
        return Arrays.stream(UserStatus.values())
                .map(status -> Map.of(
                        "code", status.name(),
                        "displayName", status.getDisplayName(),
                        "description", status.getDescription()
                ))
                .toList();
    }
}
