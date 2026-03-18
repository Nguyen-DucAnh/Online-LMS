package com.online.lms.enums;

public enum LessonType {
    VIDEO("Video"),
    PDF("PDF Document"),
    DOCX("Word Document"),
    IMAGE("Image/Photo"),
    TEXT("Text Content");

    private final String displayName;

    LessonType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
