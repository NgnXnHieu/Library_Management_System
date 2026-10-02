package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.user.UserResponseDto;
import com.library.requestform.user.UserAdminFilterRequestForm;
import com.library.requestform.user.UserFilterRequestForm;
import com.library.requestform.user.UserUpdateRequestForm;
import com.library.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
     * API tìm kiếm độc giả (chỉ lấy tài khoản CUSTOMER đang hoạt động ACTIVE) phục vụ lập phiếu mượn.
     * Dành riêng cho ADMIN, BRANCHMANAGER và STAFF.
     *
     * @param filter Bộ lọc tìm kiếm người dùng (search: username, fullName, phone, email) nhận qua Query Parameters
     * @return Danh sách DTO độc giả bọc trong chuẩn ApiResponse
     */
    @GetMapping("/users/customer")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCHMANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getCustomers(
            @ModelAttribute UserFilterRequestForm filter) {
        // Bước 1: Gọi xuống tầng Service để xử lý truy vấn danh sách độc giả theo bộ lọc
        List<UserResponseDto> customers = userService.filterCustomers(filter);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP status 200 OK
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách độc giả thành công!", customers));
    }

    /**
     * API lấy danh sách phân trang người dùng kèm theo bộ lọc mở rộng và sắp xếp.
     * Sử dụng JPA Specification kết hợp JOIN FETCH an toàn với account, branch và role để chống N+1 query.
     * Dành riêng cho ADMIN.
     *
     * @param filter Bộ lọc tìm kiếm và phân trang người dùng (searchName, role, branchName, statuses, sortBy, sortDir, page, size)
     * @return Trang kết quả chứa danh sách UserResponseDto bọc trong chuẩn ApiResponse
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAdminUsers(
            @Valid @ModelAttribute UserAdminFilterRequestForm filter) {
        // Bước 1: Gọi xuống tầng Service để xử lý truy vấn danh sách người dùng phân trang
        Page<UserResponseDto> users = userService.getUsersWithFilter(filter);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP status 200 OK
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công!", users));
    }

    /**
     * API cập nhật thông tin tài khoản người dùng theo ma trận phân quyền:
     * - ADMIN: Cập nhật cho mọi role trừ ADMIN.
     * - BRANCHMANAGER: Cập nhật cho mọi role trừ ADMIN và BRANCHMANAGER. Không được nâng role lên ADMIN.
     * - STAFF: Chỉ được cập nhật thông tin tài khoản CUSTOMER.
     * - Ràng buộc: Tài khoản CUSTOMER không được phép đổi sang vai trò khác.
     *
     * @param id   ID người dùng cần cập nhật
     * @param form Dữ liệu cập nhật nhận từ Request Body
     * @return DTO người dùng sau khi cập nhật bọc trong chuẩn ApiResponse
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCHMANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestForm form) {
        // Bước 1: Gọi xuống tầng Service để xử lý cập nhật theo phân quyền
        UserResponseDto updatedUser = userService.updateUser(id, form);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP status 200 OK
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin người dùng thành công!", updatedUser));
    }

    /**
     * API thay đổi trạng thái tài khoản người dùng nhanh chóng (ACTIVE, INACTIVE, LOCKED).
     * Dành riêng cho ADMIN và BRANCHMANAGER.
     *
     * @param id     ID người dùng cần đổi trạng thái
     * @param status Trạng thái tài khoản mới
     * @return Thông báo kết quả bọc trong chuẩn ApiResponse
     */
    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCHMANAGER')")
    public ResponseEntity<ApiResponse<Void>> changeUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        // Bước 1: Gọi xuống tầng Service để thay đổi trạng thái
        userService.changeUserStatus(id, status);

        // Bước 2: Bọc thông báo kết quả trả về
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái người dùng thành công!", null));
    }
}
