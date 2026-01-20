package com.online.lms.enums;

import com.online.lms.exceptions.transaction.InvalidTransactionTypeException;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
public enum TransactionType {
    PENDING("PENDING", "The payment has been created but not completed yet."),
    SUCCESS("SUCCESS", "The payment was completed successfully."),
    FAILED("FAILED", "The payment failed or was cancelled by the user or payment gateway."),;

    private String displayName;
    private String description;

    TransactionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public static TransactionType fromString(String type) {
        if(type == null || type.trim().isEmpty()) {
            return null;
        }
        try {
            return TransactionType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTransactionTypeException("Invalid transaction type: " + type);
        }
    }


    public static List<Map<String, String>> getTypeList() {
        return Arrays.stream(TransactionType.values())
                .map(type -> Map.of(
                        "code", type.name(),
                        "displayName", type.getDisplayName(),
                        "description", type.getDescription()
                ))
                .toList();
    }

    public static List<String> getAllTypes() {
        return Arrays.stream(TransactionType.values())
                .map(TransactionType::getDisplayName)
                .toList();
    }
}
