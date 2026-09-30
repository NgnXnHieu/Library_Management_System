package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.user.UserResponseDto;
import com.library.requestform.user.UserFilterRequestForm;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller tiếp nhận và xử lý các yêu cầu liên quan đến quản lý người dùng (User).
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * API lấy danh sách người dùng kèm theo bộ lọc tìm kiếm và sắp xếp cũ đến mới theo createdAt.
     * Sử dụng JPA Specification và JOIN FETCH account để tối ưu hiệu năng.
     * Dành riêng cho ADMIN, BRANCHMANAGER và STAFF.
     *
     * @param filter Bộ lọc tìm kiếm người dùng (username, fullName, phone, email) nhận qua Query Parameters
     * @return Danh sách DTO người dùng bọc trong chuẩn ApiResponse
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCHMANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> filterUsers(
            @ModelAttribute UserFilterRequestForm filter) {
        // Bước 1: Gọi xuống tầng Service để xử lý truy vấn danh sách người dùng theo bộ lọc
        List<UserResponseDto> users = userService.filterUsers(filter);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP status 200 OK
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công!", users));
    }
}
