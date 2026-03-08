package com.online.lms.dto.request.user;

import lombok.Data;

@Data
public class UpdateProfileRequestDTO {
    private String fullname;
    private String phone;
    private String address;
    private String avatar;
}
