package com.library.service;

import com.library.dto.user.UserResponseDto;
import com.library.requestform.user.UserUpdateRequestForm;

import java.util.List;

public interface UserService {

    UserResponseDto getUserById(Long id);

    UserResponseDto getUserByEmail(String email);

    List<UserResponseDto> getAllUsers();

    UserResponseDto updateUser(Long id, UserUpdateRequestForm form);

    void changeUserStatus(Long id, String status);

    void deleteUser(Long id);
}
