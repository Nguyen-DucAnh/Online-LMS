package com.online.lms.service;

import com.online.lms.dto.UserCreateRequest;
import com.online.lms.dto.UserUpdateRequest;
import org.apache.catalina.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();


    User getUserById(Long id);


    User createUser(UserCreateRequest request);


    User updateUser(Long id, UserUpdateRequest request);


    void toggleUserStatus(Long id);


    void deleteUser(Long id);
}
