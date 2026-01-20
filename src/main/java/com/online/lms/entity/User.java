package com.online.lms.entity;

import com.online.lms.enums.UserRole;
import com.online.lms.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "Email", unique = true, nullable = false)
    private String email;

    @Column(name = "Password")
    private String password;

    @Column(name = "FullName", length = 100,columnDefinition = "NVARCHAR(100)", nullable = false)
    private String fullname;

    @Column(name = "PhoneNumber")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    @Builder.Default
    private UserStatus status =  UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role")
    @Builder.Default
    private UserRole role = UserRole.CUSTOMER;
}
