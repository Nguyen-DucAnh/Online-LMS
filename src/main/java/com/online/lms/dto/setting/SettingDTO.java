package com.online.lms.dto.setting;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SettingDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 20, message = "Name max 20 characters")
    @Pattern(regexp = "^[^0-9]*$", message = "Name must not contain digits")
    private String name;

    private Long typeId;

    @Size(max = 100, message = "Value max 100 characters")
    private String value;

    @Min(value = 1, message = "Priority must be a positive integer")
    private Integer priority;

    private String status;

    @Size(max = 200, message = "Description max 200 characters")
    private String description;
}
