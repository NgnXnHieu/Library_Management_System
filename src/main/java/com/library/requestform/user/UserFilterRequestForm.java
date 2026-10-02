package com.library.requestform.user;

import com.library.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form tiếp nhận các tham số lọc tìm kiếm người dùng (User).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFilterRequestForm {

    /**
     * Từ khóa tìm kiếm chung (tìm kiếm trong: username, fullName, phone, email)
     */
    private String search;

    /**
     * Lọc theo mã vai trò người dùng (ví dụ: "CUSTOMER", "STAFF"...)
     */
    private String role;

    /**
     * Lọc theo trạng thái tài khoản người dùng (ACTIVE, INACTIVE, LOCKED)
     */
    private AccountStatus status;
}
