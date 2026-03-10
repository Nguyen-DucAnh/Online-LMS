package com.online.lms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import com.online.lms.enums.UserRole;
import com.online.lms.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "email", unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "PhoneNumber")
    private String phone;

    @Column(name = "Address", length = 255)
    private String address;

    @Column(name = "avatar", length = 500)
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private UserRole role = UserRole.MEMBER;

    @OneToMany(mappedBy = "instructor", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Course> instructedCourses = new ArrayList<>();

    @Column(name = "Otp")
    private String otp;

    @Column(name = "OtpExpiry")
    private java.time.LocalDateTime otpExpiry;

    public boolean isDeletable() {
        return instructedCourses == null || instructedCourses.isEmpty();
    }

}
