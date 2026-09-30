package com.library.service;

import com.library.dto.user.UserResponseDto;
import com.library.requestform.user.UserFilterRequestForm;
import com.library.requestform.user.UserUpdateRequestForm;

import java.util.List;

public interface UserService {

    UserResponseDto getUserById(Long id);

    UserResponseDto getUserByEmail(String email);

    List<UserResponseDto> getAllUsers();

    UserResponseDto updateUser(Long id, UserUpdateRequestForm form);

    void changeUserStatus(Long id, String status);

    void deleteUser(Long id);

    /**
     * Lấy danh sách người dùng theo bộ lọc tìm kiếm và sắp xếp cũ đến mới theo createdAt.
     * Sử dụng JPA Specification và JOIN FETCH account để tối ưu hiệu năng.
     *
     * @param filter Bộ lọc tìm kiếm người dùng (username, fullName, phone, email)
     * @return Danh sách DTO người dùng thỏa mãn điều kiện
     */
    List<UserResponseDto> filterUsers(UserFilterRequestForm filter);
}
