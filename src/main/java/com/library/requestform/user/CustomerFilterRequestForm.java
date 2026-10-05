package com.library.requestform.user;

import com.library.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form tiếp nhận các tham số tìm kiếm, lọc và phân trang dành riêng cho tài khoản Khách hàng (CUSTOMER).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerFilterRequestForm {

    /**
     * Lọc theo tên đăng nhập của khách hàng (username)
     */
    private String username;

    /**
     * Lọc theo họ và tên đầy đủ của khách hàng (fullName)
     */
    private String fullName;

    /**
     * Lọc theo số điện thoại của khách hàng (phone)
     */
    private String phone;

    /**
     * Lọc theo địa chỉ email của khách hàng (email)
     */
    private String email;

    /**
     * Lọc theo trạng thái tài khoản (ACTIVE, INACTIVE, LOCKED)
     */
    private AccountStatus status;

    /**
     * Chỉ số trang hiện tại (0-indexed, mặc định 0)
     */
    @Builder.Default
    private int page = 0;

    /**
     * Số lượng bản ghi trên mỗi trang (mặc định 10)
     */
    @Builder.Default
    private int size = 10;

    /**
     * Trường dữ liệu dùng để sắp xếp (Hỗ trợ: createdAt, fullName, username, phone, email...). Mặc định "createdAt".
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * Hướng sắp xếp: "desc" (mới nhất lên đầu) hoặc "asc" (cũ nhất lên đầu). Mặc định "desc".
     */
    @Builder.Default
    private String sortDir = "desc";
}
